#!/bin/bash
# edit-guard.sh — PreToolUse(Edit|Write). deny=Claude에게 거부, ask=사용자 권한 프롬프트
INPUT=$(cat)
TOOL=$(jq -r '.tool_name' <<<"$INPUT")
FILE=$(jq -r '.tool_input.file_path // empty' <<<"$INPUT" | tr '\\' '/')
if [ "$TOOL" = "Write" ]; then NEW=$(jq -r '.tool_input.content // empty' <<<"$INPUT")
else NEW=$(jq -r '.tool_input.new_string // empty' <<<"$INPUT"); fi

decide() { jq -n --arg d "$1" --arg r "[CLAUDE.md §7] $2" \
  '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:$d,permissionDecisionReason:$r}}'; exit 0; }

case "$FILE" in */gradle/wrapper/*|*/package-lock.json)
  decide deny "빌드·의존성 잠금 파일($FILE) 직접 수정 금지. 필요하면 사용자에게 먼저 확인." ;; esac

if [[ "$FILE" == */build.gradle ]] && grep -qE "(implementation|api|compileOnly|runtimeOnly|annotationProcessor|developmentOnly|testImplementation|testRuntimeOnly|testCompileOnly|testAnnotationProcessor)[[:space:]]*[\('\"]" <<<"$NEW"; then
  decide ask "build.gradle 의존성 변경 감지. 추가 전에 옵션을 제시하고 승인받았는지 확인."
fi

if [[ "$FILE" == *.java && "$FILE" == */src/main/java/* ]]; then
  if sed -E 's#//.*$##' <<<"$NEW" | tr '\n' ' ' | sed -E 's#/\*([^*]|\*[^/])*\*/##g' | grep -qE 'catch[[:space:]]*\([^)]*\)[[:space:]]*\{[[:space:]]*\}'; then
    decide deny "빈 catch 블록 감지. log.error + rethrow 하거나 @RestControllerAdvice에서 명시 처리."
  fi

  OWN=$(sed -nE 's#.*/src/main/java/com/commerce/pagopa/([a-z]+)/.*#\1#p' <<<"$FILE")
  BAD=$(grep -oE '^import (static )?com\.commerce\.pagopa\.[a-z]+\.[a-z]+\.' <<<"$NEW" \
        | sed -E 's/^import (static )?com\.commerce\.pagopa\.//; s/\.$//' \
        | awk -F. -v own="$OWN" '$1!=own && $1!="global" && $2!="api" && $2!="event"' | sort -u | tr '\n' ' ')
  [ -n "$BAD" ] && decide deny "모듈 '$OWN'에서 다른 모듈 internal 패키지 참조: $BAD— api/event 패키지로 공개된 타입만 사용."

  if [[ "$FILE" == *Controller.java ]]; then
    DOMAIN_IMPORTS=$(grep -oE '^import (static )?com\.commerce\.pagopa\.[a-z.]*\.domain\.[A-Za-z0-9_.]+' <<<"$NEW" \
      | sed -E 's/^import (static )?com\.commerce\.pagopa\.//' | sort -u | tr '\n' ' ')
    if [ -n "$DOMAIN_IMPORTS" ]; then
      decide deny "Controller가 domain 계층을 참조: $DOMAIN_IMPORTS— Controller는 Request/Response DTO와 Service만 안다. 필요한 타입은 dto 또는 api 패키지로 옮긴다."
    fi
  fi

  if [[ "$FILE" =~ /dto/response/|Response(Dto)?\.java$ ]] && [ -f "$FILE" ]; then
    decide ask "기존 Response DTO($FILE) 수정. 변경 내용과 pagopa-web 호출부 영향을 보고 후 승인."
  fi
fi

exit 0

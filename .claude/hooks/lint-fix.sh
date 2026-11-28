#!/bin/bash
# lint-fix.sh — Claude Code PostToolUse Hook (파일 수정 직후)
# Write/Edit/MultiEdit 후 수정된 .java 파일 1개만 Spotless로 자동 포맷
# Claude Code는 hook 입력을 stdin으로 JSON 전달: {"tool_input":{"file_path":"..."}}
# 포맷 실패를 Claude에게 되돌려 자동 수정시키려면 exit 2. 의존: jq
# 규칙은 build.gradle의 spotless { java { ... } } 블록을 그대로 따른다.

INPUT=$(cat)
MODIFIED_FILE=$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // empty' 2>/dev/null)
[ -z "$MODIFIED_FILE" ] && exit 0
# Windows 경로(C:\...)를 Git Bash 경로(/c/...)로 변환
command -v cygpath >/dev/null 2>&1 && MODIFIED_FILE=$(cygpath -u "$MODIFIED_FILE")
[ ! -f "$MODIFIED_FILE" ] && exit 0

# Java 파일만 처리 (Spotless target: src/main/java, src/test/java)
echo "$MODIFIED_FILE" | grep -qE "\.java$" || exit 0
MODIFIED_FILE="$(cd "$(dirname "$MODIFIED_FILE")" && pwd)/$(basename "$MODIFIED_FILE")"
echo "🔧 lint-fix.sh: $MODIFIED_FILE"

# 파일에서 가장 가까운 Gradle 루트(gradlew 위치)에서 실행
ROOT_DIR=$(dirname "$MODIFIED_FILE")
while [ "$ROOT_DIR" != "/" ] && [ ! -f "$ROOT_DIR/gradlew" ]; do
  ROOT_DIR=$(dirname "$ROOT_DIR")
done
[ ! -f "$ROOT_DIR/gradlew" ] && exit 0
cd "$ROOT_DIR" || exit 0

# Gradle(java)에는 Windows 경로로 넘긴다
HOOK_PATH="$MODIFIED_FILE"
command -v cygpath >/dev/null 2>&1 && HOOK_PATH=$(cygpath -w "$MODIFIED_FILE")

# Spotless IDE hook: 지정한 파일 하나만 spotlessApply
if ! SPOTLESS_OUT=$(./gradlew spotlessApply -PspotlessIdeHook="$HOOK_PATH" -q 2>&1); then
  echo "$SPOTLESS_OUT" | tail -20 >&2
  echo "  ❌ Spotless 실패 — Claude가 수정해야 함 (문법 오류 등)" >&2
  exit 2
fi
if echo "$SPOTLESS_OUT" | grep -q "DID NOT CONVERGE"; then
  echo "  ❌ Spotless 포맷이 수렴하지 않음 — 수동 확인 필요" >&2
  exit 2
fi
echo "$SPOTLESS_OUT" | grep -q "IS DIRTY" && echo "  ✅ Spotless 포맷 적용" || echo "  ✅ Spotless (변경 없음)"

# Checkstyle (ESLint 역할) — 소스 세트 단위로만 실행 가능하므로 리포트에서 이 파일 위반만 추린다
case "$MODIFIED_FILE" in
  */src/main/java/*) CS_SET=main ;;
  */src/test/java/*) CS_SET=test ;;
  *) CS_SET="" ;;
esac
if [ -n "$CS_SET" ]; then
  CS_TASK="checkstyle$(echo "${CS_SET:0:1}" | tr a-z A-Z)${CS_SET:1}"
  REPORT="build/reports/checkstyle/$CS_SET.xml"
  # 이전 실행의 리포트를 지워, 아래에서 읽는 리포트가 이번 실행 결과임을 보장
  rm -f "$REPORT"
  if ! CS_OUT=$(./gradlew "$CS_TASK" -q 2>&1); then
    # 이번 실행이 리포트를 만들지 못함 (설정 오류, Gradle 실패 등)
    if [ ! -f "$REPORT" ]; then
      echo "$CS_OUT" | tail -20 >&2
      echo "  ❌ Checkstyle 실행 실패 (리포트 없음)" >&2
      exit 2
    fi
    # 리포트의 <file name="C:\...">...</file> 블록 중 수정한 파일의 <error> 줄만 추출
    # (awk -v는 C:\study의 백슬래시를 이스케이프로 해석하므로 ENVIRON으로 전달)
    FILE_ERRORS=$(CS_FILE="$HOOK_PATH" awk '
      index($0, "<file name=\"" ENVIRON["CS_FILE"] "\">") == 1 { inside = 1; next }
      inside && /<\/file>/ { inside = 0 }
      inside && /<error/ { print }' "$REPORT" 2>/dev/null)
    if [ -n "$FILE_ERRORS" ]; then
      echo "$FILE_ERRORS" | sed -E 's/.*line="([0-9]+)".*message="([^"]*)".*/  line \1: \2/' \
      | sed -e "s/&apos;/'/g" -e 's/&quot;/"/g' -e 's/&lt;/</g' -e 's/&gt;/>/g' -e 's/&amp;/\&/g' \
      | head -20 >&2
      echo "  ❌ Checkstyle 위반 — Claude가 수정해야 함" >&2
      exit 2
    fi
    echo "  ⚠️  Checkstyle: 이 파일은 통과, 다른 파일에 기존 위반 있음 ($REPORT)" >&2
  else
    echo "  ✅ Checkstyle"
  fi
fi

# 금지 패턴 감지 (CLAUDE.md STOP 목록, 경고만)
if grep -nE "System\.(out|err)\.print" "$MODIFIED_FILE" 2>/dev/null; then
  echo "  ⚠️  System.out/err 출력 감지 — 로거(log.*) 사용 필요" >&2
fi
if grep -nE "catch\s*\([^)]*\)\s*\{\s*\}" "$MODIFIED_FILE" 2>/dev/null; then
  echo "  ⚠️  빈 catch 블록 감지 — silent fail 위험" >&2
fi

exit 0

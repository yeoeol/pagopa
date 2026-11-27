#!/bin/bash
# stop-check.sh — 엔드포인트가 추가된 Controller에 대응 테스트 변경이 없으면 종료 차단
INPUT=$(cat)
[ "$(jq -r '.stop_hook_active' <<<"$INPUT")" = "true" ] && exit 0   # 무한 루프 방지
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
CHANGED=$( { git diff --name-only HEAD; git ls-files --others --exclude-standard; } 2>/dev/null | sort -u)
MISSING=""
for C in $(grep -E '^src/main/java/.*Controller\.java$' <<<"$CHANGED"); do
  if git ls-files --error-unmatch "$C" >/dev/null 2>&1; then
    git diff HEAD -- "$C" | grep -qE '^\+.*@(Get|Post|Put|Patch|Delete|Request)Mapping' || continue
  fi
  grep -qE "/$(basename "$C" .java)Test\.java$" <<<"$CHANGED" || MISSING="$MISSING $C"
done
if [ -n "$MISSING" ]; then
  echo "[CLAUDE.md §7] 엔드포인트가 추가된 Controller의 테스트가 없음:$MISSING — 대응 <Controller>Test.java 작성 후 종료." >&2
  exit 2
fi
exit 0

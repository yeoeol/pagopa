#!/bin/bash
# guard.sh — Claude Code PreToolUse Hook (Bash 실행 직전 검사)
# Claude Code는 hook 입력을 stdin으로 JSON 전달한다:
#   {"tool_name":"Bash","tool_input":{"command":"..."}}
# 명령을 차단하려면 exit 2 로 종료한다 (stderr가 Claude에게 전달돼 자동 처리됨).
# exit 0 = 통과, exit 1 = 비차단 오류(실행 계속). 차단은 반드시 exit 2.
# 의존: jq (brew install jq)

INPUT=$(cat)
COMMAND=$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty' 2>/dev/null)
# jq가 없거나 평문으로 직접 테스트할 때는 입력 전체를 명령으로 간주
[ -z "$COMMAND" ] && COMMAND="$INPUT"

block() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') BLOCKED: $1 — $COMMAND" >> .claude/guard-blocked.log
  echo "🚫 BLOCKED by guard.sh: $1" >&2
  echo "REASON: $2" >&2
  echo "ACTION: $3" >&2
  exit 2
}

warn() {
  echo "⚠️  WARN by guard.sh: $1 — $2" >&2
}

# 1. 시크릿·환경 파일 노출/커밋 (.env.example은 추적 대상이라 먼저 지우고 검사)
if echo "$COMMAND" | sed 's/\.env\.example//g' | grep -qE "git add.*\.env|git commit.*\.env|cat.*\.env\.production"; then
  block "환경 파일 조작" \
    ".env / .env.production은 시크릿 포함 가능" \
    ".gitignore 확인, .env.example만 추적"
fi

if echo "$COMMAND" | grep -qE "git add.*credentials|git add.*service-account|git add.*\.pem"; then
  block "자격증명 파일 커밋" \
    "credentials.json / service-account.json / *.pem 노출 위험" \
    "Secret Manager 또는 환경변수 사용"
fi

# 2. 시크릿 로그
if echo "$COMMAND" | grep -qE '(^|[;&|[:space:](])(echo|printf)[^;&|]*\$\{?[A-Za-z0-9_]*(SECRET|TOKEN|PASSWORD|API_KEY|PRIVATE_KEY)'; then
  block "시크릿 echo" \
    "환경변수가 stdout/로그에 노출됨" \
    "로컬 디버깅은 임시변수에 마스킹 후 출력"
fi

# 3. DB 직접 조작
if echo "$COMMAND" | grep -qE "DROP TABLE|DROP DATABASE|TRUNCATE.*TABLE"; then
  block "위험한 DDL" \
    "프로덕션 데이터 삭제 위험" \
    "마이그레이션 파일 (prisma migrate / knex migrate) 경유"
fi

# 4. 마이그레이션 파일 조작
if echo "$COMMAND" | grep -qE "rm.*migrations?/|sed.*migrations?/"; then
  block "마이그레이션 파일 조작" \
    "적용된 마이그레이션 수정은 히스토리 깨짐" \
    "새 마이그레이션 추가 (prisma migrate dev --name fix_X 등)"
fi

# 5. main/master 직접 push
if echo "$COMMAND" | grep -qE "git push.*origin (main|master)( |$)"; then
  block "main/master 직접 push" \
    "보호 브랜치는 PR 경유" \
    "feature 브랜치 + PR 생성"
fi

# 6. force push
if echo "$COMMAND" | grep -qE "git push.*(-f|--force)"; then
  block "강제 push" \
    "히스토리 파괴 위험" \
    "팀 리드 승인 후 force-with-lease 사용"
fi

# 7. 빌드·의존성 잠금 파일 조작
if echo "$COMMAND" | grep -qE '(rm|mv|cp|sed -i|tee|truncate|Remove-Item|Set-Content)[^;&|]*(gradle[/\\]wrapper|package-lock\.json)|>[[:space:]]*[^]*(gradle[/\\]wrapper|package-lock\.json)|gradlew(\.bat)?[[:space:]]+wrapper'; then
  block "빌드·의존성 잠금 파일 조작" \
    "gradle/wrapper, package-lock.json 임의 변경은 빌드 재현성 파괴" \
    "사용자에게 변경 이유와 옵션 제시 후 승인"
fi

exit 0

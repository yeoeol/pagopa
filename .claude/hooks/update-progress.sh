#!/bin/bash
# Stop hook — Claude가 응답을 마칠 때마다 claude-progress.txt 자동 갱신
# 세션이 cd pagopa-api 등으로 이동했어도 항상 프로젝트 루트에서 실행 (터미널 직접 실행 시엔 현재 디렉터리)
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

DATE=$(date '+%Y-%m-%d %H:%M')
LAST_COMMIT=$(git log --oneline -1 2>/dev/null)
CHANGED=$(git diff --name-only HEAD~1 HEAD 2>/dev/null | head -10)
TEST=$(npm test --silent 2>&1 | grep -E '^Tests:')
# 수동 메모 보존: "📝 에이전트 메모" 줄부터 파일 끝까지 (없으면 빈 칸)
MEMO=$(sed -n '/^📝 에이전트 메모/,$p' claude-progress.txt 2>/dev/null)

cat > claude-progress.txt << END
# Claude Progress — 자동 업데이트: $DATE

## 마지막 커밋
$LAST_COMMIT

## 최근 변경 파일
$CHANGED

## 테스트 상태
$TEST

## 다음 세션 시작
1. cat AGENTS.md
2. cat claude-progress.txt
3. cat task-list.md
4. ./gradlew test

${MEMO:-📝 에이전트 메모: }
END

echo "✅ claude-progress.txt 갱신됨"

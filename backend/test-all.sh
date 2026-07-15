#!/bin/bash
# 돌봄ON 전체 API 스모크 테스트. 서버 먼저 띄우고 실행: bash test-all.sh
BASE=http://localhost:8080

echo "== 0. 더미 데이터 시딩 =="
curl -s -X POST $BASE/api/dev/seed | jq .

echo "== 1. 기능1 STT =="
curl -s -X POST $BASE/api/stt -F "audioFile=@$(pwd)/test.wav" | jq .

echo "== 1. 기능1 방문일지 생성 (구조화) =="
curl -s -X POST $BASE/api/visit-logs \
  -H "Content-Type: application/json" \
  -d '{"workerId":"worker-001","elderId":"elder-001","rawText":"오늘 어르신 댁 방문해서 혈압 체크하고 점심 식사 도와드렸습니다. 컨디션은 양호하셨어요."}' | jq .

echo "== 2. 기능2 포트폴리오 조회 (신규 명세: stats/attendanceStats/attendanceMonthly/activityTrends/carePerformances/experiences/certificates/timeline) =="
PORTFOLIO=$(curl -s $BASE/api/portfolio/worker-001)
echo "$PORTFOLIO" | jq .
echo "-- 필드 존재 확인 --"
echo "$PORTFOLIO" | jq '{
  workerId,
  stats,
  attendanceStats,
  attendanceMonthlyCount: (.attendanceMonthly | length),
  activityTrendsCount: (.activityTrends | length),
  carePerformancesCount: (.carePerformances | length),
  experiences,
  certificatesCount: (.certificates | length),
  timelineCount: (.timeline | length)
}'

echo "== 3. 기능3 인수인계 카드 생성 =="
CARD_ID=$(curl -s -X POST $BASE/api/handover-cards/elder-001/generate \
  -H "Content-Type: application/json" \
  -d '{"previousWorkerId":"worker-001","newWorkerId":"worker-002"}' | tee /tmp/card.json | jq -r .cardId)
cat /tmp/card.json | jq .
echo "cardId=$CARD_ID"

echo "== 3. 카드 확정 =="
curl -s -X PUT $BASE/api/handover-cards/$CARD_ID/confirm \
  -H "Content-Type: application/json" \
  -d '{"confirmedBy":"admin-001"}' | jq .

echo "== 3. 최신 카드 조회 =="
curl -s $BASE/api/handover-cards/elder-001/latest | jq .

echo "== 3. 카드 이력 조회 =="
curl -s $BASE/api/handover-cards/elder-001/history | jq .

echo "== 3. 근거 로그 조회 =="
curl -s $BASE/api/handover-cards/$CARD_ID/source-logs | jq .

#!/usr/bin/env bash
# Khởi động cả Backend + Frontend (macOS / Linux)
set -e
cd "$(dirname "$0")"

echo "============================================"
echo "  KHỞI ĐỘNG ỨNG DỤNG BỆNH VIỆN"
echo "  Backend : http://localhost:8081"
echo "  Frontend: http://localhost:3000"
echo "============================================"

# --- Backend ---
cd backend
./mvnw spring-boot:run > /tmp/benhvien-backend.log 2>&1 &
BACK_PID=$!
cd ..

# --- Frontend ---
cd frontend
if [ ! -d node_modules ]; then
  echo "Đang cài dependencies frontend (vài phút)..."
  npm install
fi
npm run dev > /tmp/benhvien-frontend.log 2>&1 &
FRONT_PID=$!
cd ..

trap "kill $BACK_PID $FRONT_PID 2>/dev/null" EXIT
echo ""
echo "Backend PID: $BACK_PID  (log: /tmp/benhvien-backend.log)"
echo "Frontend PID: $FRONT_PID (log: /tmp/benhvien-frontend.log)"
echo "Chờ 30-60 giây rồi mở: http://localhost:3000"
echo "Nhấn Ctrl+C để dừng cả hai."
wait

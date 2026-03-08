#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
RUNTIME_DIR="$ROOT_DIR/.runtime"
LOG_DIR="$RUNTIME_DIR/logs"
PID_DIR="$RUNTIME_DIR/pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

if [[ -f "$PID_DIR/backend.pid" ]] && kill -0 "$(cat "$PID_DIR/backend.pid")" >/dev/null 2>&1; then
  echo "backend 已启动，跳过"
else
  nohup bash -lc "cd '$BACKEND_DIR' && mvn spring-boot:run" > "$LOG_DIR/backend.log" 2>&1 &
  echo $! > "$PID_DIR/backend.pid"
  echo "backend 启动中..."
fi

if [[ -f "$PID_DIR/frontend.pid" ]] && kill -0 "$(cat "$PID_DIR/frontend.pid")" >/dev/null 2>&1; then
  echo "frontend 已启动，跳过"
else
  nohup bash -lc "cd '$FRONTEND_DIR' && python3 -m http.server 4173" > "$LOG_DIR/frontend.log" 2>&1 &
  echo $! > "$PID_DIR/frontend.pid"
  echo "frontend 启动中..."
fi

echo "前端地址: http://localhost:4173/"
echo "运营后台: http://localhost:4173/admin.html"
echo "后端地址: http://localhost:8080/api/idols"
echo "查看日志: $LOG_DIR"

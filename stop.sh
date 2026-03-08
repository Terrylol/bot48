#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$ROOT_DIR/.runtime/pids"

stop_one() {
  local name="$1"
  local file="$PID_DIR/$2"
  if [[ -f "$file" ]]; then
    local pid
    pid="$(cat "$file")"
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid"
      echo "已停止 $name ($pid)"
    fi
    rm -f "$file"
  fi
}

stop_one "backend" "backend.pid"
stop_one "frontend" "frontend.pid"

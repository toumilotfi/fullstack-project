#!/bin/sh
set -eu

mkdir -p /app/dist/app/browser

cat >/app/dist/app/browser/app-config.js <<EOF
window.__appConfig = {
  apiUrl: "${API_URL:-http://localhost:8080/api/v1}",
  wsUrl: "${WS_URL:-http://localhost:8080/chat}"
};
EOF

exec node dist/app/server/server.mjs

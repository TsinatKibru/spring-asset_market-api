#!/bin/bash

# Configuration
PROJECT_ROOT="/home/calm/springApp/asset_market"
FRONTEND_DIR="$PROJECT_ROOT/tma-frontend"
BACKEND_LOG="/tmp/backend.log"
FRONTEND_LOG="/tmp/frontend.log"
NGROK_LOG="/tmp/ngrok_frontend.log"

# Default Token (from history)
DEFAULT_BOT_TOKEN="8517291046:AAEM9wx7X_zHkASsJ2FBZy_uFg_ebkClHKQ"
BOT_TOKEN=${TELEGRAM_BOT_TOKEN:-$DEFAULT_BOT_TOKEN}

function check_status() {
    echo "--- Service Status ---"
    
    # 1. Database
    if docker ps --filter "name=asset-market-db" | grep -q "Up"; then
        echo "[ OK ] Database (Docker: asset-market-db) is running."
    else
        echo "[FAIL] Database (Docker: asset-market-db) is STOPPED."
    fi

    # 2. Backend
    if pgrep -f "spring-boot" > /dev/null; then
        echo "[ OK ] Backend (Spring Boot) is running."
    else
        echo "[FAIL] Backend (Spring Boot) is STOPPED."
    fi

    # 3. Frontend
    if pgrep -f "next-dev" > /dev/null; then
        echo "[ OK ] Frontend (Next.js) is running."
    else
        echo "[FAIL] Frontend (Next.js) is STOPPED."
    fi

    # 4. Ngrok
    if pgrep -f "ngrok" > /dev/null; then
        NGROK_URL=$(curl -s http://localhost:4040/api/tunnels | python3 -c "import json, sys; print(json.load(sys.stdin)['tunnels'][0]['public_url'])" 2>/dev/null)
        echo "[ OK ] Ngrok is running at: $NGROK_URL"
    else
        echo "[FAIL] Ngrok is STOPPED."
    fi
    echo "----------------------"
}

function start_services() {
    echo "Starting services..."

    # 1. Start Database
    echo "-> Starting Database (Docker)..."
    cd "$PROJECT_ROOT" && docker compose up -d

    # 2. Start Backend
    echo "-> Starting Backend (Spring Boot)..."
    export TELEGRAM_BOT_TOKEN=$BOT_TOKEN
    cd "$PROJECT_ROOT" && mvn spring-boot:run -q > "$BACKEND_LOG" 2>&1 &

    # 3. Start Frontend
    echo "-> Starting Frontend (Next.js)..."
    cd "$FRONTEND_DIR" && npm run dev > "$FRONTEND_LOG" 2>&1 &

    # 4. Start Ngrok
    echo "-> Starting Ngrok tunnel..."
    ngrok http 3000 --log=stdout > "$NGROK_LOG" 2>&1 &

    echo "Services are starting in background. Use './manage.sh status' to check."
}

function stop_services() {
    echo "Stopping services..."

    # 1. Stop Ngrok
    echo "-> Killing Ngrok..."
    pkill -9 -f ngrok

    # 2. Stop Frontend
    echo "-> Killing Frontend (Node/Next)..."
    pkill -9 -f "node.*next"
    pkill -9 -f "next-server"

    # 3. Stop Backend
    echo "-> Killing Backend (Spring Boot)..."
    pkill -9 -f "spring-boot"
    pkill -9 -f "AssetMarketApplication"

    # 4. Stop Database
    echo "-> Stopping Database..."
    cd "$PROJECT_ROOT" && docker compose stop

    echo "All services stopped."
}

function clean_services() {
    echo "Cleaning services..."
    stop_services
    echo "-> Removing containers..."
    cd "$PROJECT_ROOT" && docker compose down -v
    echo "-> Deleting logs..."
    rm -f "$BACKEND_LOG" "$FRONTEND_LOG" "$NGROK_LOG"
    echo "Clean complete."
}

function show_logs() {
    echo "Tail of logs (last 20 lines):"
    echo "--- BACKEND LOG ($BACKEND_LOG) ---"
    tail -n 20 "$BACKEND_LOG" 2>/dev/null || echo "No logs found."
    echo ""
    echo "--- FRONTEND LOG ($FRONTEND_LOG) ---"
    tail -n 20 "$FRONTEND_LOG" 2>/dev/null || echo "No logs found."
    echo ""
    echo "--- NGROK LOG ($NGROK_LOG) ---"
    tail -n 20 "$NGROK_LOG" 2>/dev/null || echo "No logs found."
}

case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    status)
        check_status
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_services
        ;;
    *)
        echo "Usage: $0 {start|stop|status|logs|clean}"
        exit 1
esac

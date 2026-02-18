# Telegram Integration Guide 🚀

This guide documents the technical setup for bridging the **Asset Market Spring Boot Backend** with a **Telegram Mini App (TMA)**.

## 1. Concepts & Architecture

### Telegram Mini App (TMA)
A TMA is a web application (built with Next.js in our case) that runs inside the Telegram interface. It feels like a native mobile app but is actually an embedded web view.

### Webhooks
A **Webhook** is an HTTP callback. Instead of our server constantly asking Telegram for updates (Polling), we tell Telegram where our server is. When a user interacts with the bot, Telegram "pushes" a JSON payload to our `/api/v1/telegram/webhook` endpoint instantly.

### ngrok (The Bridge)
Since Telegram cannot see your `localhost:8080`, we use **ngrok** to create a secure tunnel. It provides a public HTTPS URL (e.g., `https://optimally-unmunitioned-raeann.ngrok-free.dev`) that forwards all traffic to your local machine.

---

## 2. Steps Taken So Far

### Step 1: Bot Creation
We used `@BotFather` on Telegram to create `@asset_market_bot`. 
- **Bot Name**: Asset Marketplace
- **API Token**: `REDACTED_BOT_TOKEN` (Stored in `application.yml`)

### Step 2: Backend Infrastructure
We implemented several core components in Spring Boot:
- **`TelegramService.java`**: Handles HMAC-SHA256 validation of `initData` sent from the frontend to ensure requests are genuinely from Telegram.
- **`TelegramBotController.java`**: Exposes the Webhook listener and an Auth endpoint.
- **Security Updates**: Whitelisted `/api/v1/telegram/**` in `WebSecurityConfig.java` so Telegram can "talk" to us without being blocked.
- **User Mapping**: Added `telegramId` to the `User` entity to link Telegram accounts to our system.

### Step 3: Webhook Registration
We linked the bot to our ngrok URL using the Telegram API:
```bash
curl -s "https://api.telegram.org/bot<TOKEN>/setWebhook?url=<NGROK_URL>/api/v1/telegram/webhook"
```
Telegram confirmed: `{"ok":true,"result":true,"description":"Webhook was set"}`.

---

## 3. How Authentication Works

1. **Initialization**: The Next.js frontend gets `initData` from the Telegram SDK.
2. **Validation**: The frontend sends this data to our `/api/v1/telegram/auth` endpoint.
3. **Verification**: `TelegramService` uses the **Bot Token** to verify the hash of the data.
4. **Login/Signup**: 
   - If the `telegramId` exists, we log the user in.
   - If not, we automatically create a new user profile prefixed with `tg_`.
5. **Session**: The backend returns a standard **JWT Token**, which the frontend uses for all future marketplace requests.

---

## 4. Maintenance (If you restart)

If you stop `ngrok` and restart it, the URL will change. You must:
1. Copy the new ngrok URL.
2. Re-run the `setWebhook` command to update Telegram.

---

**Next Phase**: Creating the Next.js Frontend inside the `tma-frontend` directory.

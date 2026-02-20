# Telegram Integration Guide 🚀

This guide documents the technical setup for bridging the **Asset Market Spring Boot Backend** with a **Telegram Mini App (TMA)**.

## 1. Concepts & Architecture

### Telegram Mini App (TMA)
A TMA is a web application (built with Next.js) that runs inside the Telegram interface. It provides a native-like experience while leveraging standard web technologies.

### Webhooks & Tunnels
- **Webhook**: Telegram "pushes" updates to our `/api/v1/telegram/webhook` endpoint.
- **ngrok**: Provides a public HTTPS URL that tunnels traffic to `localhost:8080`, allowing Telegram to reach our local development environment.

---

## 2. Infrastructure & Auth

### Bot Setup
- **Bot**: `@asset_market_bot` (Managed via `@BotFather`).
- **Token Security**: The bot token is stored in `application.yml` and used for HMAC-SHA256 signature validation.

### Authentication Flow
1. **Frontend**: Extracts `initData` from the Telegram WebApp SDK.
2. **Verification**: Backend validates the `initData` hash against the Bot Token.
3. **Provisioning**: 
   - New Telegram users are automatically registered with a `tg_` prefix.
   - Users are assigned `ROLE_USER` (Client) and can be upgraded to `ROLE_ADMIN` (Merchant).
4. **Session**: A JWT token is issued for all subsequent API requests.

---

## 3. Marketplace Features in TMA

### Merchant Dashboard
Accessible to users with sufficient privileges, allowing them to:
- **Add Property**: Step-by-step listing creation with dynamic category schemas.
- **Image Management**: High-performance multi-image upload with local preview and removal.
- **Listing Management**: Update prices, descriptions, and dynamic specs on the fly.

### Discovery & Interaction
- **Advanced Search**: Real-time filtering by price, location, and property type.
- **Premium Carousel**: Native-feel swipeable image gallery on property detail pages.
- **Tactile Feedback**: Integrated Telegram Haptic Engine for actions like "Save to Favorites" or "Upload Image".

---

## 4. Maintenance & Operations

### Unified Management
Use the following root scripts for streamlined operations:
- `./manage.sh start/stop`: Manages Backend, Frontend, DB, and Ngrok.
- `./db.sh describe/seed`: Database schema inspection and automated data population.

### Webhook Rotation
If ngrok restarts, update the webhook URL manually or via `./manage.sh`:
```bash
curl -s "https://api.telegram.org/bot<TOKEN>/setWebhook?url=<NEW_URL>/api/v1/telegram/webhook"
```

---

**Current Status**: TMA is fully operational with merchant workflows and multi-tenant image support.

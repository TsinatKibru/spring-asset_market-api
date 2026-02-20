# Documentation: Setup and Execution

This guide provides instructions to get the Asset Market and its Telegram Mini App up and running using the unified management tools.

## 1. Prerequisites
- **Docker & Docker Compose**: For running the PostgreSQL database.
- **JDK 17 & Maven**: For the Spring Boot backend.
- **Node.js & npm**: For the Next.js TMA frontend.
- **ngrok**: To tunnel local traffic for Telegram bot testing.

## 2. Unified Management (Recommended)
The project includes a unified script `manage.sh` to handle all services (DB, Backend, Frontend, and Ngrok).

```bash
# Start all services
./manage.sh start

# Check status of all processes
./manage.sh status

# Stop all services
./manage.sh stop

# Tail logs for all services
./manage.sh logs
```

## 3. Database Operations
Use the `db.sh` script for direct interaction with the PostgreSQL instance.

```bash
# Describe table schemas
./db.sh describe categories
./db.sh describe properties

# Seed the database with premium sample data
./db.sh seed

# Run raw SQL queries
./db.sh query "SELECT count(*) FROM properties;"
```

## 4. Manual Execution
If you prefer to run services individually:

1. **Database**: `docker-compose up -d`
2. **Backend**: `mvn spring-boot:run`
3. **Frontend**: `cd tma-frontend && npm run dev`
4. **Ngrok**: `ngrok http 8080`

## 5. First Steps
1. **Reset Data**: `./manage.sh stop && ./manage.sh start`
2. **Seed Market**: `./db.sh seed`
3. **Launch TMA**: Open the bot link in Telegram (after updating the webhook if ngrok URL changed).

---
**Note**: Always ensure your `application.yml` has the correct `botToken` and `uploadDir` configured.

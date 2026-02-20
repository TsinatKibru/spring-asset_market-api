#!/bin/bash

# Configuration
DB_NAME="asset_market"
DB_USER="postgres"
DB_PASS="password"
DB_HOST="localhost"
DB_PORT="5432"

# Export password for psql
export PGPASSWORD=$DB_PASS

function show_usage() {
    echo "Usage: $0 {shell|tables|list <table_name>|describe <table_name>|exec \"<sql>\"|users|properties|categories}"
    echo ""
    echo "Commands:"
    echo "  shell               Enter interactive psql shell"
    echo "  tables              List all tables"
    echo "  list <table>        Select all from <table>"
    echo "  describe <table>    Show table structure"
    echo "  exec \"<sql>\"        Execute arbitrary SQL"
    echo "  users               Show all registered users"
    echo "  make-admin <user>   Grant ROLE_ADMIN to <username>"
    echo "  properties          Show all property listings"
    echo "  categories          Show all property categories"
}

case "$1" in
    shell)
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME
        ;;
    tables)
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\dt"
        ;;
    list)
        if [ -z "$2" ]; then
            echo "Error: Table name required"
            exit 1
        fi
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT * FROM $2;"
        ;;
    describe)
        if [ -z "$2" ]; then
            echo "Error: Table name required"
            exit 1
        fi
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\d $2"
        ;;
    exec)
        if [ -z "$2" ]; then
            echo "Error: SQL command required"
            exit 1
        fi
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "$2"
        ;;
    users)
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT id, username, email, tenant_id FROM users;"
        ;;
    make-admin)
        if [ -z "$2" ]; then
            echo "Error: Username required"
            exit 1
        fi
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "INSERT INTO user_roles (user_id, roles) SELECT id, 'ROLE_ADMIN' FROM users WHERE username = '$2' AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = users.id AND roles = 'ROLE_ADMIN');"
        ;;
    properties)
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT id, title, price, location, category_id, status FROM properties;"
        ;;
    categories)
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT id, name, description FROM categories;"
        ;;
    *)
        show_usage
        exit 1
        ;;
esac

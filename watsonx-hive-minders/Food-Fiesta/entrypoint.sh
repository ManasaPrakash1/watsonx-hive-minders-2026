#!/bin/sh

# Professional entrypoint script for Food Fiesta
echo "🚀 Starting Food Fiesta Application..."

# You can add logic here to wait for DB, run migrations, etc.
# Example: sleep 5

exec java -jar app.jar

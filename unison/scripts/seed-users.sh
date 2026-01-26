#!/bin/bash

# Seed users from user.json to the Users API
# Usage: ./seed-users.sh [base_url]
# Default base_url: http://localhost:8080/s/users-api

BASE_URL="${1:-http://localhost:8080/s/users-api}"
JSON_FILE="$(dirname "$0")/../../composeApp/src/webMain/resources/user.json"

if [ ! -f "$JSON_FILE" ]; then
    echo "Error: JSON file not found at $JSON_FILE"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

echo "Seeding user from $JSON_FILE to $BASE_URL/users"
echo "---------------------------------------------------"

# Read user data and POST it
user=$(cat "$JSON_FILE")
id=$(echo "$user" | jq -r '.id')
name=$(echo "$user" | jq -r '.name')

echo "Creating user: $name (ID: $id)"

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users" \
    -H "Content-Type: application/json" \
    -d "$user")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
    echo "  Success (HTTP $http_code)"
else
    echo "  Failed (HTTP $http_code): $body"
fi

echo "---------------------------------------------------"
echo "Done! Fetching all users to verify:"
echo ""
curl -s "$BASE_URL/users" | jq '.'
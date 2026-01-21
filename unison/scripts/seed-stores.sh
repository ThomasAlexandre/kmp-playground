#!/bin/bash

# Seed stores from stores-minimal.json to the local Stores API
# Usage: ./seed-stores.sh [base_url]
# Default base_url: http://localhost:8080/s/stores-api

BASE_URL="${1:-http://localhost:8080/s/stores-api}"
JSON_FILE="$(dirname "$0")/../../composeApp/src/webMain/resources/stores-minimal.json"

if [ ! -f "$JSON_FILE" ]; then
    echo "Error: JSON file not found at $JSON_FILE"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

echo "Seeding stores from $JSON_FILE to $BASE_URL/stores"
echo "---------------------------------------------------"

# Read each store from the JSON array and POST it
jq -c '.stores[]' "$JSON_FILE" | while read -r store; do
    id=$(echo "$store" | jq -r '.id')
    name=$(echo "$store" | jq -r '.name')

    echo "Creating store: $name (ID: $id)"

    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/stores" \
        -H "Content-Type: application/json" \
        -d "$store")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        echo "  Success (HTTP $http_code)"
    else
        echo "  Failed (HTTP $http_code): $body"
    fi
done

echo "---------------------------------------------------"
echo "Done! Fetching all stores to verify:"
echo ""
curl -s "$BASE_URL/stores" | jq '.'
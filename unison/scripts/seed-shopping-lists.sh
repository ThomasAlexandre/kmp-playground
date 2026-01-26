#!/bin/bash

# Seed shopping lists from shopping-lists.json to the Shopping Lists API
# Usage: ./seed-shopping-lists.sh [base_url]
# Default base_url: http://localhost:8080/s/shopping-lists-api

BASE_URL="${1:-http://localhost:8080/s/shopping-lists-api}"
JSON_FILE="$(dirname "$0")/../../composeApp/src/webMain/resources/shopping-lists.json"

if [ ! -f "$JSON_FILE" ]; then
    echo "Error: JSON file not found at $JSON_FILE"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

echo "Seeding shopping lists from $JSON_FILE to $BASE_URL/lists"
echo "---------------------------------------------------"

# Read each shopping list from the JSON array and POST it
jq -c '.shoppingLists[]' "$JSON_FILE" | while read -r list; do
    id=$(echo "$list" | jq -r '.id')
    name=$(echo "$list" | jq -r '.name')

    echo "Creating shopping list: $name (ID: $id)"

    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/lists" \
        -H "Content-Type: application/json" \
        -d "$list")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        echo "  Success (HTTP $http_code)"
    else
        echo "  Failed (HTTP $http_code): $body"
    fi
done

echo "---------------------------------------------------"
echo "Done! Fetching all shopping lists to verify:"
echo ""
curl -s "$BASE_URL/lists" | jq '.'
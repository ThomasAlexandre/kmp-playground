#!/bin/bash

# Seed products from converted JSON files to the local Products API
# Usage: ./seed-products.sh [base_url]
# Default base_url: http://localhost:8080/s/products-api

BASE_URL="${1:-http://localhost:8080/s/products-api}"
CONVERTED_DIR="$(dirname "$0")/../../composeApp/src/webMain/resources/converted"

if [ ! -d "$CONVERTED_DIR" ]; then
    echo "Error: Converted directory not found at $CONVERTED_DIR"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

# Count product files
product_files=("$CONVERTED_DIR"/*-min.json)
if [ ! -e "${product_files[0]}" ]; then
    echo "Error: No product JSON files found in $CONVERTED_DIR"
    exit 1
fi

echo "Seeding products from $CONVERTED_DIR to $BASE_URL/products"
echo "-----------------------------------------------------------"

# Iterate over each product JSON file and POST it
for product_file in "$CONVERTED_DIR"/*-min.json; do
    if [ -f "$product_file" ]; then
        product=$(cat "$product_file")
        code=$(echo "$product" | jq -r '.code')
        name=$(echo "$product" | jq -r '.product_name')

        echo "Creating product: $name (Code: $code)"

        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/products" \
            -H "Content-Type: application/json" \
            -d "$product")

        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')

        if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
            echo "  Success (HTTP $http_code)"
        else
            echo "  Failed (HTTP $http_code): $body"
        fi
    fi
done

echo "-----------------------------------------------------------"
echo "Done! Fetching all products to verify:"
echo ""
curl -s "$BASE_URL/products" | jq '.'
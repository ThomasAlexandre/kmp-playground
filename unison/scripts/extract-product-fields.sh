#!/bin/bash

# Fetch and extract minimal product fields from Open Food Facts
#
# Usage:
#   Fetch mode:   ./extract-product-fields.sh --fetch <code1> [code2] [code3] ...
#   Convert mode: ./extract-product-fields.sh <input.json> [output.json]
#
# Extracts: code, product_name, brands, image_url, stores
#
# Examples:
#   ./extract-product-fields.sh --fetch 7310865005168 7318690182900
#   ./extract-product-fields.sh product-7310865005168.json

API_BASE_URL="https://world.openfoodfacts.org/api/v0/product"
OUTPUT_DIR="."

# Check for jq
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

# Function to extract minimal fields from a JSON file
extract_fields() {
    local input_file="$1"
    local output_file="$2"

    jq '{
      code: .product.code,
      product_name: .product.product_name,
      brands: .product.brands,
      image_url: .product.image_url,
      stores: .product.stores
    }' "$input_file" > "$output_file"

    echo "  Extracted to: $output_file"
    cat "$output_file"
    echo ""
}

# Function to fetch product from API
fetch_product() {
    local code="$1"
    local output_file="${OUTPUT_DIR}/product-${code}.json"
    local min_file="${OUTPUT_DIR}/product-${code}-min.json"

    echo "Fetching product: $code"

    http_code=$(curl -s -w "%{http_code}" -o "$output_file" "${API_BASE_URL}/${code}.json")

    if [ "$http_code" -eq 200 ]; then
        # Check if product was found
        status=$(jq -r '.status' "$output_file")
        if [ "$status" -eq 1 ]; then
            echo "  Saved to: $output_file"
            extract_fields "$output_file" "$min_file"
        else
            echo "  Product not found (status: $status)"
            rm -f "$output_file"
        fi
    else
        echo "  Failed to fetch (HTTP $http_code)"
        rm -f "$output_file"
    fi
}

# Main logic
if [ "$1" == "--fetch" ]; then
    shift
    if [ $# -eq 0 ]; then
        echo "Usage: $0 --fetch <code1> [code2] [code3] ..."
        echo "Example: $0 --fetch 7310865005168 7318690182900"
        exit 1
    fi

    # Optional: set output directory with --output-dir
    if [ "$1" == "--output-dir" ]; then
        OUTPUT_DIR="$2"
        mkdir -p "$OUTPUT_DIR"
        shift 2
    fi

    echo "Fetching ${#} product(s) from Open Food Facts API..."
    echo "Output directory: $OUTPUT_DIR"
    echo "---------------------------------------------------"

    for code in "$@"; do
        fetch_product "$code"
    done

    echo "---------------------------------------------------"
    echo "Done!"
else
    # Convert mode - process existing file
    INPUT_FILE="$1"
    OUTPUT_FILE="${2:-${INPUT_FILE%.json}-min.json}"

    if [ -z "$INPUT_FILE" ]; then
        echo "Usage:"
        echo "  Fetch mode:   $0 --fetch <code1> [code2] [code3] ..."
        echo "  Convert mode: $0 <input.json> [output.json]"
        echo ""
        echo "Examples:"
        echo "  $0 --fetch 7310865005168 7318690182900"
        echo "  $0 product-7310865005168.json"
        exit 1
    fi

    if [ ! -f "$INPUT_FILE" ]; then
        echo "Error: Input file not found: $INPUT_FILE"
        exit 1
    fi

    echo "Extracting fields from $INPUT_FILE..."
    extract_fields "$INPUT_FILE" "$OUTPUT_FILE"
fi
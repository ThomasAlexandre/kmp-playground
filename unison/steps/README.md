# Testing the Stores API

## Step 1: Add the definitions to UCM

In your UCM terminal (inside the `unison/` directory):

```ucm
update
```

## Step 2: Run the local server

```ucm
run stores.deployLocal
```

This will start the server and print a URL (like `http://localhost:8080`).

## Step 3: Test with curl

### Create a store (POST)

```bash
curl -X POST http://localhost:8080/s/stores-api/stores \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4933,
    "key": "b2287e43-d194-4619-8408-ba86d231a7df",
    "name": "ICA Maxi Bromma",
    "lon": 17.95541,
    "lat": 59.353082,
    "address": "Köpsvägen 4",
    "postalCode": "16867",
    "city": "Bromma",
    "imageUrl": "https://mpk-app.s3.eu-north-1.amazonaws.com/store-logos/maxi_ica_stormarknad.png"
  }'
```

### Get all stores (GET)

```bash
curl http://localhost:8080/s/stores-api/stores
```

### Get store by ID (GET)

```bash
curl http://localhost:8080/s/stores-api/stores/4933
```

### Update a store (PUT)

```bash
curl -X PUT http://localhost:8080/s/stores-api/stores/4933 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4933,
    "key": "b2287e43-d194-4619-8408-ba86d231a7df",
    "name": "ICA Maxi Bromma Updated",
    "lon": 17.95541,
    "lat": 59.353082,
    "address": "Köpsvägen 4",
    "postalCode": "16867",
    "city": "Bromma",
    "imageUrl": "https://mpk-app.s3.eu-north-1.amazonaws.com/store-logos/maxi_ica_stormarknad.png"
  }'
```

### Delete a store (DELETE)

```bash
curl -X DELETE http://localhost:8080/s/stores-api/stores/4933
```

### Health check

```bash
curl http://localhost:8080/s/stores-api/health
```

> **Note:** The exact port and URL path will depend on what `main.local.serve` returns when you run it.

# Unison Language

Unison is a statically-typed, functional programming language designed for building distributed systems. Its most distinctive feature is **content-addressed code** - definitions are identified by the hash of their syntax tree rather than by name, which fundamentally changes how code is stored, shared, and deployed.

## Key Features

- **Content-addressed codebase**: Code is stored in a database indexed by hash, eliminating dependency conflicts and enabling fearless refactoring
- **Distributed computing primitives**: First-class support for distributed computation with abilities like `Remote`
- **Algebraic effects (Abilities)**: Type-safe effect system that makes side effects explicit and composable
- **Immutable by default**: All data structures are immutable
- **Structural typing**: Types are identified by structure, not name

## Pros

| Advantage | Description |
|-----------|-------------|
| **No dependency hell** | Content-addressing means no version conflicts - if it compiles, it works |
| **Effortless refactoring** | Rename anything without breaking code; the codebase tracks references by hash |
| **Built-in distribution** | Deploy code to cloud with minimal ceremony using Unison Cloud |
| **Type-safe effects** | Abilities make side effects explicit and testable |
| **Instant code sharing** | Push/pull definitions directly without packaging |
| **No builds** | Definitions are typechecked once and cached; no build step needed |

## Cons

| Disadvantage | Description |
|--------------|-------------|
| **Small ecosystem** | Limited libraries compared to mainstream languages |
| **Learning curve** | Novel concepts (content-addressing, abilities) require time to learn |
| **Tooling maturity** | IDE support and debugging tools are still maturing |
| **Different mental model** | No files in the traditional sense; code lives in a codebase database |
| **Young language** | Less production battle-testing than established languages |

## Comparison with Mainstream Languages

| Aspect | Unison | Haskell/Scala | Go/Rust |
|--------|--------|---------------|---------|
| Effect tracking | First-class abilities | Monads / ZIO | Manual |
| Dependencies | Content-addressed | Package managers | Package managers |
| Distribution | Built-in | Libraries needed | Libraries needed |
| Mutability | Immutable | Configurable | Configurable |
| Learning curve | Steep | Steep | Moderate |

## Resources

### Official Documentation
- **Unison Language Docs**: https://www.unison-lang.org/docs/
- **Language Reference**: https://www.unison-lang.org/docs/language-reference/
- **Tour of Unison**: https://www.unison-lang.org/docs/tour/

### Unison Share
- **Unison Share** (package registry): https://share.unison-lang.org/
- **Base library**: https://share.unison-lang.org/@unison/base

### Unison Cloud
- **Cloud Documentation**: https://www.unison-lang.org/docs/unison-cloud/
- **Cloud Quickstart**: https://www.unison-lang.org/docs/unison-cloud/quickstart/

### Community
- **Discord**: https://discord.gg/unison-lang
- **GitHub**: https://github.com/unisonweb/unison

## Project Structure

```
unison/
├── scratch.u          # Working file for new definitions
├── scripts/           # Shell scripts for seeding data
│   ├── seed-stores.sh
│   ├── seed-products.sh
│   ├── seed-users.sh
│   └── seed-shopping-lists.sh
└── steps/             # Step-by-step API implementations
    ├── crud for stores.u
    ├── crud for products.u
    ├── crud for users.u
    └── crud for shopping-lists.u
```

## APIs

### Shopping Lists API

**Base URL:** `https://thomasalexandre.unison-services.cloud/s/shopping-lists-api`

#### Data Model

**ShoppingItem** (simplified - references products by barcode):
```json
{
  "code": "7310865005168",
  "quantity": 2,
  "isChecked": false,
  "notes": "For baking"
}
```

**ShoppingList**:
```json
{
  "id": "list_weekly_001",
  "name": "Weekly Groceries",
  "createdAt": "2024-06-01T08:00:00Z",
  "updatedAt": "2024-06-10T16:45:00Z",
  "isArchived": false,
  "items": [...]
}
```

#### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/lists` | Get all shopping lists |
| GET | `/lists/:id` | Get shopping list by ID |
| POST | `/lists` | Create a new shopping list |
| PUT | `/lists/:id` | Update a shopping list |
| DELETE | `/lists/:id` | Delete a shopping list |
| POST | `/lists/:id/items` | Add item to a shopping list |

#### Example Requests

```bash
# Get all shopping lists
curl https://thomasalexandre.unison-services.cloud/s/shopping-lists-api/lists

# Get a specific list
curl https://thomasalexandre.unison-services.cloud/s/shopping-lists-api/lists/list_weekly_001

# Create a new shopping list
curl -X POST https://thomasalexandre.unison-services.cloud/s/shopping-lists-api/lists \
  -H "Content-Type: application/json" \
  -d '{
    "id": "list_new_001",
    "name": "My New List",
    "createdAt": "2024-06-15T10:00:00Z",
    "updatedAt": "2024-06-15T10:00:00Z",
    "isArchived": false,
    "items": []
  }'

# Add item to a list
curl -X POST https://thomasalexandre.unison-services.cloud/s/shopping-lists-api/lists/list_new_001/items \
  -H "Content-Type: application/json" \
  -d '{
    "code": "7310865005168",
    "quantity": 1,
    "isChecked": false,
    "notes": null
  }'
```

#### Seeding Data

```bash
./unison/scripts/seed-shopping-lists.sh "https://thomasalexandre.unison-services.cloud/s/shopping-lists-api"
```

### Users API

**Base URL:** `https://thomasalexandre.unison-services.cloud/s/users-api`

#### Data Model

**UserProfile**:
```json
{
  "preferredLanguage": "sv",
  "currency": "SEK",
  "notificationsEnabled": true,
  "darkModeEnabled": false
}
```

**SelectedStore** (simplified - storeName resolved via stores API):
```json
{
  "storeId": 4933,
  "addedAt": "2024-03-15T11:00:00Z",
  "isPreferred": true
}
```

**User** (references shopping lists by ID):
```json
{
  "id": "usr_a1b2c3d4",
  "name": "Anna Lindqvist",
  "email": "anna.lindqvist@example.com",
  "avatarUrl": "https://api.dicebear.com/7.x/avataaars/svg?seed=anna",
  "createdAt": "2024-03-15T10:30:00Z",
  "profile": {...},
  "selectedStores": [...],
  "shoppingListIds": ["list_weekly_001", "list_party_002"]
}
```

#### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/users` | Get all users |
| GET | `/users/:id` | Get user by ID |
| POST | `/users` | Create a new user |
| PUT | `/users/:id` | Update a user |
| DELETE | `/users/:id` | Delete a user |

#### Example Requests

```bash
# Get all users
curl https://thomasalexandre.unison-services.cloud/s/users-api/users

# Get a specific user
curl https://thomasalexandre.unison-services.cloud/s/users-api/users/usr_a1b2c3d4

# Create a new user
curl -X POST https://thomasalexandre.unison-services.cloud/s/users-api/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": "usr_a1b2c3d4",
    "name": "Anna Lindqvist",
    "email": "anna.lindqvist@example.com",
    "avatarUrl": "https://api.dicebear.com/7.x/avataaars/svg?seed=anna",
    "createdAt": "2024-03-15T10:30:00Z",
    "profile": {
      "preferredLanguage": "sv",
      "currency": "SEK",
      "notificationsEnabled": true,
      "darkModeEnabled": false
    },
    "selectedStores": [
      {"storeId": 4933, "addedAt": "2024-03-15T11:00:00Z", "isPreferred": true}
    ],
    "shoppingListIds": ["list_weekly_001", "list_party_002"]
  }'
```

### Prices API

**Base URL:** `https://thomasalexandre.unison-services.cloud/s/prices-api`

#### Data Model

**ProductIdentifier** (two variants):
```json
// Universal barcode (EAN/UPC)
{"type": "barcode", "code": "7310865005168"}

// Store-specific code (for fresh food, bakery items, etc.)
{"type": "store_specific", "storeId": "4933", "code": "BREAD-001"}
```

**PriceRecord**:
```json
{
  "price": "24.90",
  "currency": "SEK",
  "unit": "per_item"  // or "per_kg", "per_100g", "per_liter"
}
```

**RecordPriceRequest**:
```json
{
  "product": {"type": "barcode", "code": "7310865005168"},
  "storeId": "4933",
  "price": {
    "price": "24.90",
    "currency": "SEK",
    "unit": "per_item"
  },
  "recordedAt": "2024-06-15T14:30:00Z"
}
```

#### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/prices` | Record a new price observation |
| GET | `/prices/barcode/:code?storeId=X` | Get latest price for a barcode at a store |
| GET | `/prices/barcode/:code/history?storeId=X` | Get price history for a barcode at a store |
| GET | `/prices/barcode/:code/compare` | Compare prices across all stores |
| GET | `/stores/:storeId/prices` | Get all latest prices at a store |

#### Example Requests

```bash
# Record a new price
curl -X POST https://thomasalexandre.unison-services.cloud/s/prices-api/prices \
  -H "Content-Type: application/json" \
  -d '{
    "product": {"type": "barcode", "code": "7310865005168"},
    "storeId": "4933",
    "price": {
      "price": "24.90",
      "currency": "SEK",
      "unit": "per_item"
    },
    "recordedAt": "2024-06-15T14:30:00Z"
  }'

# Get latest price for a product at a specific store
curl "https://thomasalexandre.unison-services.cloud/s/prices-api/prices/barcode/7310865005168?storeId=4933"

# Get price history
curl "https://thomasalexandre.unison-services.cloud/s/prices-api/prices/barcode/7310865005168/history?storeId=4933"

# Compare prices across stores
curl https://thomasalexandre.unison-services.cloud/s/prices-api/prices/barcode/7310865005168/compare

# Get all prices at a store
curl https://thomasalexandre.unison-services.cloud/s/prices-api/stores/4933/prices
```

#### Response Examples

**Latest price response:**
```json
{
  "recordedAt": "2024-06-15T14:30:00Z",
  "price": {
    "price": "24.90",
    "currency": "SEK",
    "unit": "per_item"
  }
}
```

**Compare prices response:**
```json
[
  {
    "storeId": "4933",
    "recordedAt": "2024-06-15T14:30:00Z",
    "price": {"price": "24.90", "currency": "SEK", "unit": "per_item"}
  },
  {
    "storeId": "5012",
    "recordedAt": "2024-06-14T10:15:00Z",
    "price": {"price": "26.50", "currency": "SEK", "unit": "per_item"}
  }
]
```

### Stores API

**Base URL:** `https://thomasalexandre.unison-services.cloud/s/stores-api`

#### Data Model

**Store**:
```json
{
  "id": 1,
  "key": "SE559175008701",
  "name": "ICA Supermarket Brommaplan",
  "lon": 17.9389058,
  "lat": 59.3384163,
  "address": "Tunnlandet 1-3",
  "postalCode": "16836",
  "city": "Bromma",
  "imageUrl": "https://mpk-app.s3.eu-north-1.amazonaws.com/store-logos/ica_supermarket.png"
}
```

#### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/stores` | Get all stores |
| GET | `/stores/:id` | Get store by ID |
| GET | `/stores/key/:key` | Get store by key (org number) |
| POST | `/stores` | Create a new store |
| PUT | `/stores/:id` | Update a store |
| DELETE | `/stores/:id` | Delete a store |

#### Example Requests

```bash
# Get all stores
curl https://thomasalexandre.unison-services.cloud/s/stores-api/stores

# Get store by ID
curl https://thomasalexandre.unison-services.cloud/s/stores-api/stores/1

# Get store by key (org number) - useful for receipt parsing
curl https://thomasalexandre.unison-services.cloud/s/stores-api/stores/key/SE559175008701
```

### Other APIs

| API | Base URL | Description |
|-----|----------|-------------|
| Products | `/s/products-api` | CRUD for products (from Open Food Facts) |

## Quick Example

```unison
-- Define a simple HTTP handler with CORS
myHandler : HttpRequest ->{Exception} HttpResponse
myHandler req =
  response = HttpResponse.ok (Body.fromText "Hello, Unison!")
  response |> HttpResponse.addHeader "Access-Control-Allow-Origin" "*"

-- Deploy to Unison Cloud
deploy : '{IO, Exception} ()
deploy = Cloud.main do
  env = Environment.default()
  deployHttp env myHandler
```
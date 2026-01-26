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

### Other APIs

| API | Base URL | Description |
|-----|----------|-------------|
| Products | `/s/products-api` | CRUD for products (from Open Food Facts) |
| Stores | `/s/stores-api` | CRUD for store locations |
| Users | `/s/users-api` | CRUD for user profiles |

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
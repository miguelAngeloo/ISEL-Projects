# Software Laboratory — House Rentals System

## Phase 1

### Goals

Development of an information system to manage house rentals. The project is divided into 4 incremental phases.

---

### Domain

#### User
| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `name` | User's name |
| `email` | Unique email address |

#### Location
| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `name` | Location name |
| `type` | One of: `COUNTRY`, `REGION`, `DISTRICT`, `MUNICIPALITY`, `LOCALITY` |
| `parentId` | Optional parent location (root locations have none) |

Locations are organized hierarchically. Example:
```
Portugal (COUNTRY)
└── Lisboa (DISTRICT)
    └── Oeiras (MUNICIPALITY)
        └── Paço de Arcos (LOCALITY)
```

#### House
| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `title` | Short name |
| `locationId` | Associated location |
| `areaSqMt` | Area in square meters |
| `pricePerNight` | Price per night in euros |
| `description` | Short description |

> All houses are available every day unless booked for a given period.

#### Booking
| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `startDate` | Start date |
| `endDate` | End date |
| `userId` | Associated user |
| `houseId` | Associated house |

---

### API Requirements

#### User Management
- `POST /users` — Create a new user (`name`, `email`) → returns token + user ID
- `GET /users/{id}` — Get user details

#### Location Management
- `POST /locations` — Create a location (`name`, `type`, `parentId?`) → returns location ID
- `GET /locations/{id}` — Get location details
- `GET /locations/{id}/children` — List child locations
- `GET /locations/{id}/path` — Get full hierarchical path (e.g., Portugal → Lisboa → Oeiras)

#### House Management
- `POST /houses` — Create a house (`title`, `locationId`, `areaSqMt`, `pricePerNight`, `description`) → returns house ID
- `GET /houses/{id}` — Get house details
- `GET /houses` — List all houses

#### Booking Management
- `POST /bookings` — Create a booking (`hid`, `startDate`, `endDate`)
- `GET /bookings/{id}` — Get booking details
- `GET /bookings?hid=&startDate=&endDate=` — List bookings for a house within a date interval
- `GET /houses/available?startDate=&endDate=` — List available houses for a given period

#### Paging
All GET operations returning sequences support:
- `skip` — start position of the subsequence
- `limit` — length of the subsequence

Example: `GET /houses?skip=10&limit=5` returns houses from position 11 to 15.

#### Authentication
- User token generated at creation (UUID via `kotlin.uuid.Uuid`)
- Sent via `Authorization: Bearer <token>` header
- Students decide which endpoints require auth

#### Price Prediction *(Optional)*
- `GET /predict` — Estimate rental price given `areaSqMt`, `locationId`, `nights`
- Returns `predictedPricePerNight` and `predictedTotalPrice`
- Does not create a booking or require an existing house

---

### Non-Functional Requirements

| Requirement | Detail |
|-------------|--------|
| Language | Kotlin |
| HTTP | HTTP4K library |
| Serialization | `kotlinx.serialization` |
| Database | PostgreSQL |
| Tests | Run with in-memory data (not the database) |

---

### Architecture

```
houses.server   → entry point, initialization
houses.webapi   → routes, HTTP request/response handling
houses.services → application logic, coordination layer
houses.data     → data access (storage & retrieval)
houses.domain   → domain entities (shared across all layers)
```

**Dependency flow:** `webapi` → `services` → `data` → `domain`

---

### Deliverables
- Technical report in `docs/` (using provided template)
- HTTP API documentation (OpenAPI recommended)
  - Must not expose internal implementation details

---

## Phase 2

### Additional Backend Operations

- `GET /users/{id}/bookings` — List bookings of a user
- `DELETE /bookings/{id}` — Delete a booking
- `PATCH /bookings/{id}` — Update a booking's date interval (`startDate`, `endDate`)

---

### In-Memory Cache *(Optional)*

Cache for house detail lookups:
- Stores results of the last **N** accesses
- If found in cache → returned directly
- If not found → fetched normally and stored in cache
- `N` is configurable

> Must be discussed with teacher before implementation.

---

### Single Page Application (SPA)

The main deliverable for Phase 2 is a **SPA** providing a Web UI for all GET operations from Phase 1.

#### Navigation Graph
Views must follow the defined navigation graph and all views must include a link back to **Home**.

#### JavaScript DSL for HTML Construction

Instead of raw DOM APIs:
```javascript
const div = document.createElement("div")
div.appendChild(document.createTextNode("Hello"))
```

Students must implement a small DSL:
```javascript
div("Hello")

// or more complex:
div(
  h1("Title"),
  p("Some text")
)
```

Goals of the DSL:
- Improve readability
- Reduce boilerplate
- Make view construction more declarative and composable

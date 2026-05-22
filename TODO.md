# Revo Coffee Microservices TODO

## Current Architecture

- `apps/customer-web`: customer-facing React app
- `apps/admin-web`: admin React app
- `services/api-gateway-dotnet`: .NET API gateway
- `services/identity-service-dotnet`: .NET identity API
- `services/product-catalog-service`: PHP product/catalog API
- `services/inventory-service`: .NET inventory API
- `services/order-service`: Spring Boot order API
- `services/batch-service`: Spring Boot batch/QC API
- `infra/mysql/init`: database-per-service schemas and seed data

## Next Priorities

- [x] Replace remaining mock Zustand data in both frontends with API clients that call `http://localhost:8080/api/...`.
- [x] Implement subscription endpoints in Java order/payment service using the `subscriptions` table.
- [x] Standardize response shape across services, preferably `{ "items": [], "total": 0 }` for list endpoints.
- [x] Add service-to-service order flow: order service should reserve stock through inventory service or an event.
- [x] Move auth token handling to a shared frontend API helper.
- [x] Add health endpoints for all services and wire them into Docker healthchecks.
- [x] Remove legacy service folders and old generated build artifacts from the active project tree.
- [ ] Add notification service after order/subscription events are stable.

## Gateway Endpoints

| Service | Endpoint |
| --- | --- |
| Identity | `/api/auth/*` |
| Products | `/api/products` |
| Categories | `/api/categories` |
| Inventory | `/api/inventory` |
| Orders | `/api/orders` |
| Subscriptions | `/api/subscriptions` |
| Batches | `/api/batches` |

## Notes

New backend work should go under the active service folders listed above.

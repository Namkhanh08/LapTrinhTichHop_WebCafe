# Revo Coffee Microservices TODO

## Current Architecture

- `LapTrinhTichHop_WebCafe/KHE_coffe`: unified customer/admin React app
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
- [x] Add product filter metadata and admin category management for product/catalog use cases.
- [x] Add raw material lots, expiry tracking, and inventory alerts for stock management use cases.
- [x] Add order status history for internal order processing use cases.
- [x] Add roast batch packaging counts and validation for 250g/500g/1kg packages.
- [ ] Move gateway/auth policy from per-service checks toward centralized authorization.
- [ ] Replace development schema creation with explicit migrations for .NET and Spring services.
- [ ] Add RabbitMQ publishing for order-created/order-delivered events.
- [ ] Add RabbitMQ consumers for notification or loyalty events.
- [ ] Add integration tests for order + inventory flows.
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
| Dashboard | `/api/dashboard` |
| Vouchers | `/api/vouchers` |
| Carts | `/api/carts` |

## Notes

New backend work should go under the active service folders listed above.
New frontend work should go under `LapTrinhTichHop_WebCafe/KHE_coffe`.

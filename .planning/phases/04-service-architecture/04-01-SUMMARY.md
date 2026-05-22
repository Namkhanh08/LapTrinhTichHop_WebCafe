---
phase: 04-service-architecture
plan: 01
type: summary
requirements:
  - REQ-03
  - REQ-04
  - REQ-05
---

# Service Ownership Migration Summary

Implemented the service split requested for the next architecture direction:

- API Gateway moved from Nginx runtime to `services/api-gateway-dotnet`.
- Identity moved from PHP runtime to `services/identity-service-dotnet`.
- Product/Catalog moved from .NET runtime to `services/product-catalog-service`.
- Order/Payment remains in Java Spring Boot under `services/order-service`.
- Inventory and Batch services remain active because phase 03 already depends on them.
- Notification service is deferred until order/subscription events are stable.

Compatibility decisions:

- Legacy `services/identity-service`, `services/product-service`, `infra/nginx`, and `legacy/` were removed from the active project tree.
- Public routes remain stable through `http://localhost:8080/api/...`.
- Product list responses now use `{ "items": [], "total": 0 }`; customer shop accepts both old array and new wrapped response.
- MySQL init scripts were aligned closer to the Grapuco ERD by adding `addresses`, product flavor/grind tables, and `subscriptions`.

Next work:

- Complete subscription UI and account pages against the Java order service APIs.
- Connect admin product CRUD to the PHP catalog API.
- Add stock reservation from Java order service to the .NET inventory service.
- Add notification service after RabbitMQ event contracts are defined.

# ROADMAP — Revo Coffee Web

Phases

1. Bootstrap & Local Dev
   - Create `.planning` (this artifact)
   - Ensure Docker Compose starts both services
   - Add README dev-start docs

2. Core E-commerce
   - Product listing/detail, cart, checkout (local stubbed payments)
   - Basic authentication and user orders

Plans:
- Phase 02 will contain two plans:
   - `02-01`: Product catalog + cart wiring
   - `02-02`: Authentication + orders endpoints

3. Admin & Inventory
   - Admin pages: products, batches, inventory, orders
   - Secure admin routes

Plans:
- Phase 03 will contain two plans:
   - `03-01`: Admin products CRUD + admin UI
   - `03-02`: Inventory & batches management

4. Internationalization & QA
   - Add translation pipeline and validate en/vi strings
   - Add basic E2E smoke tests

5. Deployment & CI
   - Add CI, build images, push to registry, staging deploy

Architecture migration note
- Active service ownership is now:
  - API Gateway & Identity: C#/.NET
  - Product & Catalog: PHP
  - Order & Payment: Java/Spring Boot
  - Inventory and Batches: kept as existing operational services
  - Notification: deferred until order/subscription events are stable

Notes
- Each phase should produce verifiable acceptance criteria in `REQUIREMENTS.md` updates.

# Revo Coffee Microservices

Revo Coffee is a microservice-based coffee shop system with a unified React frontend, an API gateway, service-specific databases, and Docker Compose orchestration.

## Tech Stack

- Unified web: React + Vite
- API gateway: .NET + YARP reverse proxy
- Identity service: .NET + EF Core
- Product service: PHP + PDO
- Inventory service: .NET + EF Core
- Order service: Spring Boot
- Batch service: Spring Boot
- Database: MySQL 8
- Message broker: RabbitMQ
- Dev orchestration: Docker Compose

## Project Structure

```text
LapTrinhTichHop_WebCafe/
  KHE_coffe/           # Unified customer/admin React app

services/
  api-gateway-dotnet/  # .NET YARP API gateway
  identity-service-dotnet/ # Auth and users
  product-catalog-service/ # Products and categories
  inventory-service/   # Inventory and stock movements
  order-service/       # Orders and order items
  batch-service/       # Coffee batches and quality checks

infra/
  mysql/init/          # MySQL schema and seed scripts

docker-compose.yml
```

## Requirements

Install these before running the project:

- Docker Desktop
- Docker Compose plugin
- Git

Optional for local frontend-only development:

- Node.js 20+
- npm

## Run With Docker

From the repository root:

```bash
docker compose up --build
```

After containers start, open:

| App | URL |
| --- | --- |
| Unified web | `http://localhost:5173` |
| API gateway (.NET) | `http://localhost:8080` |
| phpMyAdmin | `http://localhost:8081` |
| RabbitMQ management | `http://localhost:15672` |

RabbitMQ login:

```text
Username: revo
Password: revo123
```

## Database

MySQL runs in Docker and stores data in the Docker volume `mysql_data`.

Connection from host machine:

```text
Host: localhost
Port: 3307
User: root
Password: root
```

Service databases:

```text
revo_identity
revo_products
revo_inventory
revo_orders
revo_batches
```

Initial schema and seed files are in:

```text
infra/mysql/init/
```

To reset all database data and rerun the init scripts:

```bash
docker compose down -v
docker compose up --build
```

## API Gateway

All external API calls should go through:

```text
http://localhost:8080
```

The gateway is implemented with YARP (`Yarp.ReverseProxy`) in `services/api-gateway-dotnet`.

Main routes:

| Service | Endpoint |
| --- | --- |
| Identity (.NET) | `POST /api/auth/register` |
| Identity (.NET) | `POST /api/auth/login` |
| Identity (.NET) | `GET /api/auth/profile` |
| Products (PHP) | `GET /api/products` |
| Categories (PHP) | `GET /api/categories` |
| Inventory | `GET /api/inventory` |
| Inventory alerts | `GET /api/inventory/alerts` |
| Raw material lots | `GET /api/rawmateriallots` |
| Orders | `GET /api/orders` |
| Order status history | `GET /api/orders/{id}/status-history` |
| Subscriptions | `GET /api/subscriptions` |
| Batches | `GET /api/batches` |
| Batch packaging | `POST /api/batches/{id}/packaging` |

Example:

```bash
curl http://localhost:8080/api/products
```

## Local Frontend Development

Unified web:

```bash
cd LapTrinhTichHop_WebCafe/KHE_coffe
npm install
npm run dev
```

The frontend should call backend APIs through:

```text
http://localhost:8080/api/...
```

## Useful Docker Commands

Start services:

```bash
docker compose up --build
```

Start in background:

```bash
docker compose up --build -d
```

Stop services:

```bash
docker compose down
```

Stop and delete database volume:

```bash
docker compose down -v
```

View logs:

```bash
docker compose logs -f
```

View logs for one service:

```bash
docker compose logs -f api-gateway
```

## Notes

- New identity/gateway work should go under `services/identity-service-dotnet` and `services/api-gateway-dotnet`.
- New catalog work should go under `services/product-catalog-service`.
- New frontend work should go under `LapTrinhTichHop_WebCafe/KHE_coffe`.
- Do not commit generated folders such as `node_modules`, `dist`, `bin`, `obj`, `.gradle`, or `build`.

## Microservice Readiness

Already in place:

- API gateway routes all external API traffic through `http://localhost:8080`.
- Identity, product, inventory, order, and batch domains are separated into service folders.
- MySQL uses separate service databases: `revo_identity`, `revo_products`, `revo_inventory`, `revo_orders`, and `revo_batches`.
- Docker Compose starts all active services on one shared network.
- Order creation reserves inventory through the inventory service.
- Product filtering covers coffee type, origin region, roast level, processing method, flavor note, category, and price.
- Inventory supports raw material lots, expiry tracking, low-stock alerts, and stock movement history.
- Orders record status history for internal processing and reporting.
- Roast batches support quality checks and packaging quantities by 250g, 500g, and 1kg.

Still planned:

- Move authorization rules into the gateway or a shared policy layer.
- Replace development schema creation (`EnsureCreated` / `ddl-auto=update`) with explicit migrations.
- Use RabbitMQ for asynchronous order/subscription events.
- Add integration tests for cross-service flows such as create order, cancel order, and confirm delivery.
- Add centralized logging/tracing so requests can be followed across services.

## Use Case Coverage

| Use case group | Backend service |
| --- | --- |
| UC 0R, UC001, UC00a, UC002, UC003 - auth/profile | `identity-service-dotnet` |
| UC01, UC02, UC09 - product search, detail, categories, grind sizes | `product-catalog-service` |
| UC03, UC04, UC05, UC0V, UC07, UC10, UC11 - orders, subscription, loyalty, voucher, dashboard | `order-service` |
| UC06 - roast batches and quality checks | `batch-service` |
| UC08 - stock, raw material lots, stock movement, expiry/low-stock alerts | `inventory-service` |

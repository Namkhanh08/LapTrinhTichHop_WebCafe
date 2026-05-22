# Revo Coffee Microservices

Revo Coffee is a microservice-based coffee shop system with separate customer and admin frontends, an API gateway, service-specific databases, and Docker Compose orchestration.

## Tech Stack

- Customer web: React + Vite
- Admin web: React + Vite
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
apps/
  customer-web/        # Customer-facing React app
  admin-web/           # Admin React app

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
| Customer web | `http://localhost:5173` |
| Admin web | `http://localhost:5174` |
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
| Orders | `GET /api/orders` |
| Subscriptions | `GET /api/subscriptions` |
| Batches | `GET /api/batches` |

Example:

```bash
curl http://localhost:8080/api/products
```

## Local Frontend Development

Customer web:

```bash
cd apps/customer-web
npm install
npm run dev
```

Admin web:

```bash
cd apps/admin-web
npm install
npm run dev -- --port 5174
```

The frontends should call backend APIs through:

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
- New frontend work should go under `apps/customer-web` or `apps/admin-web`.
- Do not commit generated folders such as `node_modules`, `dist`, `bin`, `obj`, `.gradle`, or `build`.

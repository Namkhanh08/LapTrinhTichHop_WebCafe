# Revo Coffee Microservices

## Structure

```text
apps/
  customer-web/        # React customer storefront
  admin-web/           # React admin console
services/
  api-gateway-dotnet/  # .NET YARP API gateway
  identity-service-dotnet/ # .NET auth/users API
  product-catalog-service/ # PHP product/catalog API
  inventory-service/   # .NET inventory API
  order-service/       # Spring Boot order API
  batch-service/       # Spring Boot coffee batch/QC API
infra/
  mysql/init/          # database-per-service init scripts
legacy/                # old monolith/artifacts kept out of the active path
```

## Run

From repo root:

```bash
docker compose up --build
```

## URLs

- API gateway: `http://localhost:8080`
- Customer web: `http://localhost:5173`
- Admin web: `http://localhost:5174`
- phpMyAdmin: `http://localhost:8081`
- RabbitMQ management: `http://localhost:15672`

## Gateway Endpoints

| Service | Endpoint |
| --- | --- |
| Identity | `POST /api/auth/register` |
| Identity | `POST /api/auth/login` |
| Identity | `GET /api/auth/profile` |
| Products | `GET /api/products` |
| Categories | `GET /api/categories` |
| Inventory | `GET /api/inventory` |
| Orders | `GET /api/orders` |
| Batches | `GET /api/batches` |

## Local Development

```powershell
cd apps/customer-web
npm install
npm run dev

cd ../../apps/admin-web
npm install
npm run dev -- --port 5174
```

The active backend path is `services/*`. The old `legacy/be_revo_coffee` project is retained only for reference and should not receive new microservice work.

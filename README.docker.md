# Revo Coffee Microservices

## Structure

```text
apps/
  # not used in the current active tree
LapTrinhTichHop_WebCafe/
  KHE_coffe/           # React customer/admin frontend
services/
  api-gateway-dotnet/  # .NET YARP API gateway
  identity-service-dotnet/ # .NET auth/users API
  product-catalog-service/ # PHP product/catalog API
  inventory-service/   # .NET inventory API
  order-service/       # Spring Boot order API
  batch-service/       # Spring Boot coffee batch/QC API
infra/
  mysql/init/          # database-per-service init scripts
Backend/               # older Spring Boot backend kept for reference
```

## Run

From repo root:

```bash
docker compose up --build
```

## URLs

- API gateway: `http://localhost:8080`
- Unified web: `http://localhost:5173`
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
cd LapTrinhTichHop_WebCafe/KHE_coffe
npm install
npm run dev
```

The active backend path is `services/*`. Older backend folders are retained only for reference and should not receive new microservice work.

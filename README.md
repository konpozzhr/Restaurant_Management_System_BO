# Restora – Restaurant Management Backend

Spring Boot 4 + Spring Data JPA + PostgreSQL REST API for the Restora React app.

## 1. Create the database (once)

```sql
CREATE DATABASE restaurant_management;
CREATE USER restaurant_user WITH ENCRYPTED PASSWORD 'choose-a-password';
GRANT ALL PRIVILEGES ON DATABASE restaurant_management TO restaurant_user;
\c restaurant_management
GRANT ALL ON SCHEMA public TO restaurant_user;   -- needed on PostgreSQL 15+
```

## 2. Configure

Edit `src/main/resources/application.properties`, or (better) set environment variables:

```
DB_URL=jdbc:postgresql://localhost:5432/restaurant_management
DB_USERNAME=restaurant_user
DB_PASSWORD=choose-a-password
```

## 3. Run

```
mvn spring-boot:run
```

API: http://localhost:8081/api — tables are created automatically (`ddl-auto=update`)
and the starter menu / customers / ingredients are seeded into empty tables.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET/POST | `/api/categories` | `{ "name": "Starters" }` |
| PUT/DELETE | `/api/categories/{id}` | delete is refused (409) while products use it |
| GET/POST | `/api/products` | `{ name, category (name), price, stock, icon, color, photo }` |
| GET/PUT/DELETE | `/api/products/{id}` | |
| PATCH | `/api/products/{id}/cost` | `{ "cost": 2.4 }` – food cost per portion |
| GET/POST | `/api/customers` | `{ name, phone }` |
| GET | `/api/orders` | newest first |
| POST | `/api/orders` | `{ customerId, items: [{ productId, qty }] }` – prices come from the menu |
| GET | `/api/orders/{id}` | |
| PATCH | `/api/orders/{id}/payment-method` | `{ "paymentMethod": "bakong" }` (pending only) |
| POST | `/api/orders/{id}/pay` | optional `{ "paymentMethod": "card" }` |
| POST | `/api/orders/{id}/cancel` | pending only |
| GET/POST | `/api/inventory` | `{ name, category, unit, stock, par, cost, shelfLife }` |
| POST | `/api/inventory/{id}/adjust` | `{ "delta": 5 }` in, `{ "delta": -2 }` out |
| DELETE | `/api/inventory/{id}` | |
| GET | `/api/inventory/log` | latest 30 stock movements |

Errors come back as `{ status, error, message, fieldErrors }`.

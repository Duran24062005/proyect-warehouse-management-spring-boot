# Warehouse Management API

Swagger UI: `http://localhost:8000/swagger-ui.html`

OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Available modules

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `PATCH /api/auth/change-password`
- `GET /api/users`
- `GET /api/users/role?role={role}`
- `GET|POST|PUT|DELETE /api/products`
- `GET /api/products/low-stock`
- `GET|POST|PUT|DELETE /api/warehouses`
- `GET|POST|PUT|DELETE /api/movements`
- `GET /api/movements?productId={id}`
- `GET /api/movements?warehouseId={id}`

## Notes

- La API usa autenticacion `Bearer JWT`.
- `register` es publico pero siempre crea usuarios con rol `USER`.
- `/api/users/**` requiere rol `ADMIN`.
- Los movimientos validan las reglas del esquema SQL:
  `ENTRY` requiere destino y no origen,
  `EXIT` requiere origen y no destino,
  `TRANSFER` requiere ambos y deben ser distintos.
- Los ids relacionados (`employeeUserId`, `managerUserId`, `productId`, `warehouseId`) se validan contra la base de datos.

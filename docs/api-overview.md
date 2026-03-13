# Warehouse Management API

Swagger UI: `http://localhost:8000/swagger-ui.html`

OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Available modules

- `GET|POST|PUT|DELETE /api/products`
- `GET /api/products/low-stock`
- `GET|POST|PUT|DELETE /api/warehouses`
- `GET|POST|PUT|DELETE /api/movements`
- `GET /api/movements?productId={id}`
- `GET /api/movements?warehouseId={id}`

## Notes

- No se implemento autenticacion ni seguridad por solicitud actual.
- Los movimientos validan las reglas del esquema SQL:
  `ENTRY` requiere destino y no origen,
  `EXIT` requiere origen y no destino,
  `TRANSFER` requiere ambos y deben ser distintos.
- Los ids relacionados (`employeeUserId`, `managerUserId`, `productId`, `warehouseId`) se validan contra la base de datos.

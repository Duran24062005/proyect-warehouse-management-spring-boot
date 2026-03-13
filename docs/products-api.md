# Product API

Base URL: `http://localhost:8000/api/products`

Swagger UI: `http://localhost:8000/swagger-ui.html`

OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Endpoints

### `GET /api/products`
Lista todos los productos.

### `GET /api/products/{id}`
Obtiene un producto por su id.

### `GET /api/products/low-stock`
Lista productos con stock bajo. El stock se calcula a partir de `movement` y el umbral actual es `<= 5`.

### `POST /api/products`
Crea un producto.

Request body:

```json
{
  "name": "Teclado Mecanico Redragon",
  "category": "Perifericos",
  "price": 250.00,
  "warehouseId": 1
}
```

### `PUT /api/products/{id}`
Actualiza un producto usando el mismo body del `POST`.

### `DELETE /api/products/{id}`
Elimina un producto por id.

## Response example

```json
{
  "id": 1,
  "name": "Laptop Dell Latitude 5440",
  "category": "Computo",
  "price": 4200.00,
  "warehouseId": 2,
  "warehouseName": "Bodega Norte Medellin"
}
```

## Notes

- Si el producto no existe, la API responde `404`.
- Si el `warehouseId` no existe, la API responde `404`.
- El endpoint de listado funciona en `GET /api/products` sin slash final obligatorio.
- La documentación interactiva queda integrada con Swagger UI usando `springdoc-openapi`.

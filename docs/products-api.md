# Product API

Base URL: `http://localhost:8000/api/products`

Swagger UI: `http://localhost:8000/swagger-ui.html`

OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Seguridad

Todos los endpoints de productos requieren Bearer token JWT.

```http
Authorization: Bearer <token>
```

## Endpoints

### `GET /api/products`

Lista todos los productos.

### `GET /api/products/{id}`

Obtiene un producto por id.

### `POST /api/products`

Crea un producto.

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

## Response Example

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

## Notas

- Si el producto no existe, la API responde `404`.
- Si el `warehouseId` no existe, la API responde `404`.
- Las respuestas no exponen la entidad `Warehouse` completa; solo resumen de relacion.

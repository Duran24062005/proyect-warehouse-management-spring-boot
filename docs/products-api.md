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

### `GET /api/products/low-stock`

Lista productos con stock bajo. El stock se calcula a partir de la tabla `movement` y el umbral actual es `<= 5`.

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
- El endpoint de stock bajo es especialmente usado por el dashboard del frontend.

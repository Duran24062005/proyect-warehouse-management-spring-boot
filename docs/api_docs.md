# API Docs

## Base URLs

- API: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Products

### `GET /api/products`
Lista todos los productos.

### `GET /api/products/{id}`
Obtiene un producto por id.

### `GET /api/products/low-stock`
Lista productos con stock calculado menor o igual a `5`.

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
Actualiza un producto.

### `DELETE /api/products/{id}`
Elimina un producto.

Response example:

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

## Warehouses

### `GET /api/warehouses`
Lista todos los almacenes.

### `GET /api/warehouses/{id}`
Obtiene un almacen por id.

### `POST /api/warehouses`
Crea un almacen.

Request body:

```json
{
  "name": "Bodega Central Bogota",
  "ubication": "Bogota, DC",
  "capacity": 5000.000,
  "managerUserId": 2
}
```

### `PUT /api/warehouses/{id}`
Actualiza un almacen.

### `DELETE /api/warehouses/{id}`
Elimina un almacen.

Response example:

```json
{
  "id": 1,
  "name": "Bodega Central Bogota",
  "ubication": "Bogota, DC",
  "capacity": 5000.000,
  "managerUserId": 2,
  "managerName": "Maria Lopez"
}
```

## Movements

### `GET /api/movements`
Lista todos los movimientos.

### `GET /api/movements/{id}`
Obtiene un movimiento por id.

### `GET /api/movements?productId={id}`
Filtra movimientos por producto.

### `GET /api/movements?warehouseId={id}`
Filtra movimientos por almacen de origen o destino.

### `POST /api/movements`
Crea un movimiento.

Request body example for `TRANSFER`:

```json
{
  "movementType": "TRANSFER",
  "employeeUserId": 2,
  "originWarehouseId": 1,
  "destinationWarehouseId": 2,
  "productId": 1,
  "quantity": 25
}
```

### `PUT /api/movements/{id}`
Actualiza un movimiento.

### `DELETE /api/movements/{id}`
Elimina un movimiento.

Response example:

```json
{
  "id": 4,
  "movementType": "TRANSFER",
  "employeeUserId": 2,
  "employeeName": "Maria Lopez",
  "originWarehouseId": 1,
  "originWarehouseName": "Bodega Central Bogota",
  "destinationWarehouseId": 2,
  "destinationWarehouseName": "Bodega Norte Medellin",
  "productId": 1,
  "productName": "Laptop Dell Latitude 5440",
  "quantity": 25,
  "createdAt": "2026-03-04T14:00:00"
}
```

## Error Contract

Errores controlados retornan este formato:

```json
{
  "timestamp": "2026-03-12T18:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id 99",
  "path": "/api/products/99",
  "details": []
}
```

## Business Rules

- `ENTRY` requiere `destinationWarehouseId` y no permite `originWarehouseId`.
- `EXIT` requiere `originWarehouseId` y no permite `destinationWarehouseId`.
- `TRANSFER` requiere ambos almacenes y deben ser diferentes.
- Si un id relacionado no existe, la API responde `404`.
- Si el payload es invalido o mal formado, la API responde `400`.

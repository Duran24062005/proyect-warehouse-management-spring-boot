# API Docs

## Base URLs

- API: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Authentication

La API usa `Bearer JWT`.

Flujo recomendado en Swagger:

1. Ejecuta `POST /api/auth/login`.
2. Copia `accessToken`.
3. Presiona `Authorize` en Swagger UI.
4. Pega: `Bearer <token>`.

Credenciales seed:

- Admin:
  `admin@logitrack.com` / `Admin123!`
- Users:
  `mlopez@logitrack.com` / `User123!`
  `jgarcia@logitrack.com` / `User123!`
  `cperez@logitrack.com` / `User123!`

## Auth

### `POST /api/auth/register`
Registra un nuevo usuario con rol `USER`.

### `POST /api/auth/login`
Inicia sesion y retorna JWT.

Request body:

```json
{
  "email": "admin@logitrack.com",
  "password": "Admin123!"
}
```

Response example:

```json
{
  "message": "Login successful",
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@logitrack.com",
    "firstName": "Alexi",
    "lastName": "Duran",
    "phoneNumber": "3000000001",
    "role": "ADMIN",
    "enabled": true
  }
}
```

### `GET /api/auth/me`
Retorna el usuario autenticado.

### `PATCH /api/auth/change-password`
Cambia la contrasena del usuario autenticado.

Request body:

```json
{
  "currentPassword": "Admin123!",
  "newPassword": "Admin456!"
}
```

## Users

### `GET /api/users`
Lista todos los usuarios. Requiere rol `ADMIN`.

### `GET /api/users/role?role=ADMIN|USER`
Filtra usuarios por rol. Requiere rol `ADMIN`.

## Products

### `GET /api/products`
Lista todos los productos. Requiere autenticacion.

### `GET /api/products/{id}`
Obtiene un producto por id. Requiere autenticacion.

### `GET /api/products/low-stock`
Lista productos con stock calculado menor o igual a `5`. Requiere autenticacion.

### `POST /api/products`
Crea un producto. Requiere autenticacion.

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
Actualiza un producto. Requiere autenticacion.

### `DELETE /api/products/{id}`
Elimina un producto. Requiere autenticacion.

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
Lista todos los almacenes. Requiere autenticacion.

### `GET /api/warehouses/{id}`
Obtiene un almacen por id. Requiere autenticacion.

### `POST /api/warehouses`
Crea un almacen. Requiere autenticacion.

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
Actualiza un almacen. Requiere autenticacion.

### `DELETE /api/warehouses/{id}`
Elimina un almacen. Requiere autenticacion.

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
Lista todos los movimientos. Requiere autenticacion.

### `GET /api/movements/{id}`
Obtiene un movimiento por id. Requiere autenticacion.

### `GET /api/movements?productId={id}`
Filtra movimientos por producto. Requiere autenticacion.

### `GET /api/movements?warehouseId={id}`
Filtra movimientos por almacen de origen o destino. Requiere autenticacion.

### `POST /api/movements`
Crea un movimiento. Requiere autenticacion.

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
Actualiza un movimiento. Requiere autenticacion.

### `DELETE /api/movements/{id}`
Elimina un movimiento. Requiere autenticacion.

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
- Si falta token o el token es invalido, la API responde `401`.
- Si el usuario no tiene permisos suficientes, la API responde `403`.

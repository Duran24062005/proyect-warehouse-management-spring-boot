# API Docs

## Base URLs

- API: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Authentication

La API usa Bearer token JWT en el header:

```http
Authorization: Bearer <token>
```

Rutas publicas:

- `POST /api/auth/register`
- `POST /api/auth/login`

Rutas protegidas:

- `/api/auth/me`
- `/api/auth/change-password`
- `/api/products/**`
- `/api/warehouses/**`
- `/api/movements/**`

Rutas solo `ADMIN`:

- `/api/users/**`

## Auth

### `POST /api/auth/register`

Registra un usuario nuevo con rol `USER`.

```json
{
  "email": "user@logitrack.com",
  "password": "123456",
  "firstName": "Maria",
  "lastName": "Lopez",
  "phoneNumber": "3000000002"
}
```

### `POST /api/auth/login`

Autentica email y password, y retorna token JWT.

```json
{
  "email": "admin@logitrack.com",
  "password": "123456"
}
```

Respuesta esperada:

```json
{
  "message": "Login successful",
  "token": "<jwt>",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "admin@logitrack.com",
    "firstName": "Admin",
    "lastName": "Principal",
    "phoneNumber": "3000000001",
    "role": "ADMIN",
    "enabled": true
  }
}
```

### `GET /api/auth/me`

Retorna el usuario autenticado a partir del token.

### `PATCH /api/auth/change-password`

Cambia la contrasena del usuario autenticado.

```json
{
  "currentPassword": "123456",
  "newPassword": "654321"
}
```

## Users

### `GET /api/users`

Lista todos los usuarios. Requiere rol `ADMIN`.

### `GET /api/users/role?role=ADMIN`

Filtra usuarios por rol. Requiere rol `ADMIN`.

### `POST /api/users`

Crea usuarios desde administracion. Requiere rol `ADMIN`.

```json
{
  "email": "nuevo.admin@logitrack.com",
  "password": "Admin123!",
  "firstName": "Laura",
  "lastName": "Suarez",
  "phoneNumber": "3000000010",
  "role": "ADMIN",
  "enabled": true
}
```

## Products

### `GET /api/products`

Lista todos los productos.

### `GET /api/products/{id}`

Obtiene un producto por id.

### `GET /api/products/low-stock`

Lista productos con stock calculado menor o igual a `5`.

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

Actualiza un producto.

### `DELETE /api/products/{id}`

Elimina un producto.

## Warehouses

### `GET /api/warehouses`

Lista todas las bodegas.

### `GET /api/warehouses/{id}`

Obtiene una bodega por id.

### `POST /api/warehouses`

Crea una bodega.

```json
{
  "name": "Bodega Central Bogota",
  "ubication": "Bogota, DC",
  "capacity": 5000.000,
  "managerUserId": 2
}
```

### `PUT /api/warehouses/{id}`

Actualiza una bodega. El campo `managerUserId` permite asignar o reemplazar el manager.

### `DELETE /api/warehouses/{id}`

Elimina una bodega.

## Movements

### `GET /api/movements`

Lista todos los movimientos.

### `GET /api/movements?productId={id}`

Filtra movimientos por producto.

### `GET /api/movements?warehouseId={id}`

Filtra movimientos por bodega de origen o destino.

### `GET /api/movements/{id}`

Obtiene un movimiento por id.

### `POST /api/movements`

Crea un movimiento.

Ejemplo `TRANSFER`:

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

## Error Contract

Errores controlados retornan un payload uniforme:

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

## Integraciones Externas

- Registro y login pueden disparar notificaciones por email.
- La URL base de la API de notificaciones se configura en `application.properties`.

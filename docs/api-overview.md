# Warehouse Management API

Swagger UI: `http://localhost:8000/swagger-ui.html`

OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## Modulos Disponibles

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `PATCH /api/auth/change-password`
- `PATCH /api/users/me/profile-photo`
- `GET|POST /api/users`
- `GET /api/users/role?role=ADMIN`
- `GET|POST|PUT|DELETE /api/products`
- `GET|POST|PUT|DELETE /api/warehouses`
- `GET|POST|PUT|DELETE /api/movements`
- `GET /api/movements?productId={id}`
- `GET /api/movements?warehouseId={id}`

## Seguridad

- Publico:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `swagger-ui` y `v3/api-docs`
- Requiere Bearer token:
  - `/api/auth/me`
  - `/api/auth/change-password`
  - `PATCH /api/users/me/profile-photo`
  - `/api/products/**`
  - `/api/warehouses/**`
  - `/api/movements/**`
- Requiere rol `ADMIN`:
  - `/api/users/**` excepto `PATCH /api/users/me/profile-photo`

## Reglas de Negocio Clave

- `ENTRY` requiere bodega destino y no usa bodega origen.
- `EXIT` requiere bodega origen y no usa bodega destino.
- `TRANSFER` requiere ambas bodegas y deben ser distintas.
- En registro y login se dispara una notificacion por email si la integracion esta habilitada.
- Las fotos de perfil se publican bajo `/uploads/profile-images/**`.

## Frontend Integrado

La aplicacion incluye cliente web servido por Spring Boot:

- `/`
- `/register.html`
- `/platform/system.html`
- `/platform/products.html`
- `/platform/profile.html`
- `/platform/admin.html`

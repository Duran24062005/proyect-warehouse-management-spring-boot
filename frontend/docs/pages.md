# Paginas y Modulos

## Publicas

### Login

- HTML: [index.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/index.html)
- Script: [login-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/login-page.js)

Comportamiento:

- redirige al dashboard si ya existe sesion
- envia credenciales a `POST /auth/login`
- guarda token y usuario con `saveLogin()`
- permite cambiar la base URL del backend desde la UI

### Registro

- HTML: [register.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/register.html)
- Script: [register-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/register-page.js)

Comportamiento:

- redirige si el usuario ya esta autenticado
- envia datos a `POST /auth/register`
- muestra confirmacion y vuelve al login

## Privadas

### Dashboard

- HTML: [system.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/system.html)
- Script: [dashboard-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/dashboard-page.js)

Responsabilidad:

- cargar resumen general del sistema
- mostrar conteos de productos, bodegas, movimientos y usuarios
- mostrar una tabla corta de bodegas
- mostrar movimientos recientes

Endpoints usados:

- `GET /products`
- `GET /warehouses`
- `GET /movements`
- `GET /users` solo para admin

### Productos

- HTML: [products.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/products.html)
- Script: [products-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/products-page.js)

Responsabilidad:

- crear, editar y eliminar productos
- listar productos visibles para el usuario
- poblar selector de bodegas

Endpoints usados:

- `GET /products`
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `GET /warehouses`

### Bodegas

- HTML: [warehouses.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/warehouses.html)
- Script: [warehouses-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/warehouses-page.js)

Responsabilidad:

- crear, editar y eliminar bodegas
- asignar manager desde formulario
- ocultar formulario para usuarios no admin
- listar bodegas segun el alcance del usuario

Endpoints usados:

- `GET /warehouses`
- `POST /warehouses`
- `PUT /warehouses/{id}`
- `DELETE /warehouses/{id}`
- `GET /users` solo para admin

### Movimientos

- HTML: [movements.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/movements.html)
- Script: [movements-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/movements-page.js)

Responsabilidad:

- crear, editar y eliminar movimientos
- filtrar movimientos
- adaptar campos de origen/destino segun `ENTRY`, `EXIT` o `TRANSFER`
- mostrar el usuario autenticado como responsable del registro

Endpoints usados:

- `GET /movements`
- `POST /movements`
- `PUT /movements/{id}`
- `DELETE /movements/{id}`
- `GET /products`
- `GET /warehouses`

### Perfil

- HTML: [profile.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/profile.html)
- Script: [profile-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/profile-page.js)

Responsabilidad:

- mostrar datos del usuario autenticado
- cambiar contrasena

Endpoints usados:

- `GET /auth/me` por `requireAuth()`
- `PATCH /auth/change-password`

### Administracion de usuarios

- HTML: [admin.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform/admin.html)
- Script: [admin-page.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages/admin-page.js)

Restriccion:

- solo accesible para rol `ADMIN`

Responsabilidad:

- crear usuarios
- filtrar usuarios por rol o estado
- cambiar estado con selector
- aplicar acciones rapidas como aprobar, bloquear y desbloquear

Endpoints usados:

- `GET /users`
- `GET /users/role`
- `GET /users/status`
- `POST /users`
- `PATCH /users/{id}/status`
- `PATCH /users/{id}/approve`
- `PATCH /users/{id}/block`
- `PATCH /users/{id}/unblock`

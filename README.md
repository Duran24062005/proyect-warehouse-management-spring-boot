<div align="center">
    <img src="./logo_repo.png" alt="Repo Logo" width="320">
</div>

# LogiTrack Warehouse Management

Aplicacion monolitica construida con Spring Boot para administrar productos, bodegas, movimientos de inventario, autenticacion JWT y una consola web estatica para operacion interna.

## Estado Actual

- Backend REST con Spring Boot, Spring Security y Spring Data JPA.
- Base de datos MySQL con esquema `logiTrack`.
- Autenticacion JWT para proteger la API.
- Administracion de usuarios con rol `ADMIN`.
- Frontend estatico en HTML, CSS y JavaScript modular.
- Integracion de notificaciones por email a traves de una API externa en FastAPI.
- Documentacion interactiva con Swagger/OpenAPI.

## Modulos Implementados

- `Auth`
  Registro, login, perfil del usuario autenticado y cambio de contrasena.
- `Users`
  Consulta de usuarios y creacion de nuevos usuarios desde administracion.
- `Products`
  CRUD de productos y consulta de stock bajo.
- `Warehouses`
  CRUD de bodegas y asignacion de manager.
- `Movements`
  CRUD de movimientos con reglas para `ENTRY`, `EXIT` y `TRANSFER`.
- `Notifications`
  Envio de emails al registrarse y al iniciar sesion usando provider + factory.

## Seguridad

- `POST /api/auth/register` y `POST /api/auth/login` son publicos.
- Swagger UI y los assets del frontend estatico son publicos.
- `GET /api/auth/me`, `PATCH /api/auth/change-password`, productos, bodegas y movimientos requieren Bearer token.
- `/api/users/**` requiere rol `ADMIN`.

## Frontend

La aplicacion estatica vive en `src/main/resources/static` y se organiza asi:

- `index.html`
  Login.
- `register.html`
  Registro.
- `platform/system.html`
  Dashboard operativo.
- `platform/products.html`
  Gestion de productos.
- `platform/profile.html`
  Perfil y cambio de contrasena.
- `platform/admin.html`
  Consola administrativa para usuarios y asignacion de managers.
- `js/core`
  Sesion, helpers UI y acceso a API.
- `js/pages`
  Logica por pantalla.
- `styles`
  Estilos base, auth y app.

## Configuracion

Las propiedades principales estan en [application.properties](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/src/main/resources/application.properties):

- `server.port`
- `spring.datasource.*`
- `app.security.jwt.secret`
- `app.notifications.email.enabled`
- `app.notifications.email.provider`
- `app.notifications.email.fastapi.base-url`
- `app.notifications.email.fastapi.send-path`

## Ejecucion Local

1. Crear la base de datos MySQL `logiTrack`.
2. Ajustar credenciales y propiedades en `application.properties`.
3. Ejecutar la aplicacion Spring Boot.

```bash
./mvnw spring-boot:run
```

La app quedara disponible en:

- API: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/swagger-ui.html`
- Frontend: `http://localhost:8000/`

## Documentacion del Repositorio

- [docs/architecture.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/architecture.md)
- [docs/api-overview.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/api-overview.md)
- [docs/api_docs.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/api_docs.md)
- [docs/products-api.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/products-api.md)
- [docs/auth-users-api.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/auth-users-api.md)
- [docs/frontend-overview.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/frontend-overview.md)
- [docs/notifications-context.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/docs/notifications-context.md)

## Limitaciones Actuales

- No existe aun un modulo funcional de auditoria automatica conectado a `audit_change`.
- No hay pruebas end-to-end del frontend.
- El stock bajo se calcula desde movimientos, no desde una tabla dedicada de inventario consolidado.

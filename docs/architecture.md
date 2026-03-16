# Architecture

## Overview

`warehouse-management` es una aplicacion monolitica en Spring Boot con frontend estatico integrado. Gestiona autenticacion, usuarios, productos, bodegas, movimientos y notificaciones por email.

## Capas

- `controllers`
  Expone endpoints REST y define el contrato HTTP.
- `services`
  Centraliza reglas de negocio y coordina casos de uso.
- `repositories`
  Acceso a datos con Spring Data JPA.
- `mapper`
  Conversión entre entidades y DTOs.
- `model`
  Entidades persistentes y enums del dominio.
- `security`
  JWT, filtro de autenticacion y configuracion de acceso.
- `notifications`
  Modelos, servicio de notificacion, factory y proveedores.
- `config`
  Configuracion transversal como OpenAPI y `RestClient`.
- `exceptions`
  Manejo global de errores HTTP.

## Modulos Funcionales

- `Auth`
  Registro, login, `me` y cambio de contrasena.
- `Users`
  Consulta y creacion administrativa de usuarios.
- `Products`
  CRUD de catalogo de productos.
- `Warehouses`
  CRUD y asignacion de manager.
- `Movements`
  CRUD y filtros por producto o bodega.
- `Notifications`
  Emails transaccionales en eventos de autenticacion.

## Flujo de Request

1. El cliente web o cualquier consumidor HTTP invoca la API.
2. `SecurityConfig` determina si la ruta es publica o protegida.
3. `JwtFilter` valida el token Bearer y llena el `SecurityContext`.
4. El `Controller` recibe el request y delega al `Service`.
5. El `Service` valida reglas de negocio, resuelve relaciones y usa repositorios.
6. Los `Mapper` y DTOs modelan la respuesta.
7. `GlobalExceptionHandler` normaliza errores cuando aplica.

## Seguridad

- Endpoints publicos:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `swagger-ui`, `v3/api-docs` y assets estaticos
- Endpoints autenticados:
  - `/api/auth/me`
  - `/api/auth/change-password`
  - `/api/products/**`
  - `/api/warehouses/**`
  - `/api/movements/**`
- Endpoints con rol `ADMIN`:
  - `/api/users/**`

## Notificaciones

La aplicacion no invoca la API externa de email de forma directa desde `AuthServiceImpl`. En su lugar:

1. `AuthServiceImpl` dispara un caso de uso de notificacion.
2. `AuthEmailNotificationService` construye el comando de negocio.
3. `EmailNotificationProviderFactory` resuelve el proveedor activo segun `application.properties`.
4. `FastApiEmailNotificationProvider` transforma el comando a `multipart/form-data` y llama la API externa.

Esto permite cambiar de proveedor sin tocar el flujo de autenticacion.

## Frontend Integrado

La UI estatica esta en `src/main/resources/static`:

- `index.html` y `register.html` para acceso.
- `platform/*.html` para dashboard, productos, perfil y administracion.
- `js/core` para autenticacion, sesion y utilidades.
- `js/pages` para logica por pagina.
- `styles` para identidad visual y layout.

## Persistencia

Tablas principales:

- `app_user`
- `warehouse`
- `product`
- `movement`
- `audit_change`
- `entity`

Relaciones relevantes:

- Un `Warehouse` puede tener un manager (`AppUser`).
- Un `Product` puede estar asociado a una bodega.
- Un `Movement` referencia producto, usuario responsable y una o dos bodegas segun el tipo.

## Limitaciones Actuales

- La tabla `audit_change` no esta integrada aun a un flujo funcional de auditoria automatica.
- El frontend no tiene suite automatizada de pruebas visuales o end-to-end.
- Las notificaciones por email dependen de la disponibilidad de la API externa configurada.

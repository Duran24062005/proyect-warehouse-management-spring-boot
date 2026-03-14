# PRD de Acciones Administrativas sobre Estado de Usuario

## Objetivo

Proveer endpoints explicitos solo para administradores que permitan aprobar, bloquear y desbloquear usuarios, preservando la arquitectura actual y el modelo de estados existente.

## Alcance

- Aprobar una cuenta de usuario.
- Bloquear una cuenta de usuario.
- Desbloquear una cuenta bloqueada.
- Reutilizar `AppUser`, `UserStatus`, la capa de servicios y la proteccion admin con JWT.
- Mantener compatibilidad con el endpoint generico de actualizacion de estado.

## Contexto de Negocio

El sistema ya soporta estados del ciclo de vida de usuario:

- `PENDING`
- `ACTIVE`
- `BLOCKED`

Sin embargo, las operaciones administrativas deben exponerse como acciones de negocio claras, no solo como un endpoint generico para mutar estados.

Para la administracion interna, la API debe soportar claramente:

1. Aprobar usuarios pendientes.
2. Bloquear usuarios activos.
3. Desbloquear usuarios bloqueados.

Esto mejora claridad tanto para el frontend como para la intencion de negocio.

## Requisitos Funcionales

- `PATCH /api/users/{id}/approve` debe establecer el usuario en `ACTIVE`.
- `PATCH /api/users/{id}/block` debe establecer el usuario en `BLOCKED`.
- `PATCH /api/users/{id}/unblock` debe establecer el usuario bloqueado en `ACTIVE`.
- Estos endpoints deben requerir autenticacion y rol `ADMIN`.
- Aprobar o desbloquear un usuario debe tambien establecer `enabled = true`.
- Bloquear un usuario debe tambien establecer `enabled = false`.
- Los endpoints deben retornar la representacion segura actualizada del usuario.

## Reglas de Negocio

- Aprobar un usuario ya activo debe retornar un error visible de negocio.
- Bloquear un usuario ya bloqueado debe retornar un error visible de negocio.
- Desbloquear un usuario que no esta bloqueado debe retornar un error visible de negocio.
- Si el id del usuario no existe, la API debe retornar `404`.

## Endpoints de API

### `PATCH /api/users/{id}/approve`

- Requiere Bearer JWT.
- Requiere rol `ADMIN`.
- Activa la cuenta.

### `PATCH /api/users/{id}/block`

- Requiere Bearer JWT.
- Requiere rol `ADMIN`.
- Bloquea la cuenta.

### `PATCH /api/users/{id}/unblock`

- Requiere Bearer JWT.
- Requiere rol `ADMIN`.
- Reactiva una cuenta previamente bloqueada.

## Fuera de Alcance

- Rediseño de roles.
- Permisos por bodega.
- Cambios de notificaciones por email.
- Rediseño de rutas en Spring Security.
- Eliminacion del endpoint generico de estado.

## Criterios de Aceptacion

- Un admin puede aprobar un usuario pendiente mediante un endpoint dedicado.
- Un admin puede bloquear un usuario activo mediante un endpoint dedicado.
- Un admin puede desbloquear un usuario bloqueado mediante un endpoint dedicado.
- Aprobar o desbloquear un usuario deja la cuenta activa y habilitada.
- Bloquear un usuario deja la cuenta bloqueada y deshabilitada.
- Las transiciones invalidas retornan errores de negocio claros.
- Los usuarios sin rol admin no pueden acceder a estos endpoints.

## Notas de Implementacion

- La solucion debe implementarse en `UserController` y `UserService`.
- La logica compartida de transicion de estado debe centralizarse en la capa de servicios.
- Deben preservarse las convenciones actuales de DTOs y responses.

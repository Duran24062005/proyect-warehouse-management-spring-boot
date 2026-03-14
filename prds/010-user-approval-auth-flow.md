# PRD del Flujo de Aprobacion de Usuarios

## Objetivo

Completar el ciclo de autenticacion para usuarios internos aplicando reglas de estado de cuenta desde la logica de servicios y la validacion en base de datos, sin modificar la configuracion global de seguridad.

## Alcance

- El registro publico crea cuentas pendientes.
- Solo las cuentas activas pueden iniciar sesion.
- Las cuentas bloqueadas no pueden iniciar sesion.
- Los administradores pueden consultar usuarios por estado.
- Los administradores pueden actualizar el estado de usuarios.
- La base de datos debe mantener consistencia entre `user_status` y `enable`.

## Contexto de Negocio

El sistema permite auto-registro publico, pero un usuario recien registrado no debe obtener acceso inmediato a recursos protegidos.

El ciclo esperado es:

1. Un usuario se registra.
2. La cuenta se almacena como pendiente.
3. Un administrador revisa la cuenta.
4. El administrador la activa o la bloquea.
5. Solo los usuarios activos pueden autenticarse con exito.

Este flujo debe aplicarse sin rediseñar la arquitectura ni cambiar la configuracion global de seguridad.

## Requisitos Funcionales

- `POST /api/auth/register` debe crear nuevas cuentas publicas con:
  - rol `USER`
  - estado `PENDING`
  - `enabled = false`
- `POST /api/auth/login` debe rechazar cuentas con estado `PENDING`.
- `POST /api/auth/login` debe rechazar cuentas con estado `BLOCKED`.
- `POST /api/auth/login` debe permitir solo cuentas con estado `ACTIVE`.
- Los usuarios creados por administracion deben seguir usando el flujo administrativo existente.
- Los usuarios creados por administracion con `enabled = true` deben almacenarse como `ACTIVE`.
- Los usuarios creados por administracion con `enabled = false` deben almacenarse como `BLOCKED`.
- `GET /api/users/status?status=PENDING` debe listar usuarios por estado para admins autenticados.
- `PATCH /api/users/{id}/status` debe permitir a los admins cambiar el estado logico del usuario.
- Cambiar un usuario a `ACTIVE` debe tambien establecer `enabled = true`.
- Cambiar un usuario a `PENDING` o `BLOCKED` debe tambien establecer `enabled = false`.

## Adiciones de API

### `GET /api/users/status?status=ACTIVE`

- Requiere Bearer JWT.
- Requiere rol `ADMIN`.
- Retorna todos los usuarios con el estado solicitado.

Valores permitidos:

- `PENDING`
- `ACTIVE`
- `BLOCKED`

### `PATCH /api/users/{id}/status`

- Requiere Bearer JWT.
- Requiere rol `ADMIN`.
- Actualiza el estado logico del usuario seleccionado.

Request body:

```json
{
  "status": "ACTIVE"
}
```

## Reglas de Validacion

- Un email duplicado en registro debe seguir retornando un error de negocio.
- Credenciales invalidas deben seguir retornando un error de autenticacion.
- Una cuenta pendiente debe recibir un rechazo visible durante login.
- Una cuenta bloqueada debe recibir un rechazo visible durante login.
- Un cambio de estado debe fallar con `404` si el usuario no existe.

## Reglas de Persistencia

La base de datos debe reforzar la relacion entre estado de cuenta y bandera `enable`:

- `ACTIVE` requiere `enable = true`
- `PENDING` requiere `enable = false`
- `BLOCKED` requiere `enable = false`

Esta regla debe aplicarse mediante restricciones en base de datos, no solo desde Java.

## Fuera de Alcance

- Cambios en la configuracion de rutas de Spring Security.
- Cambios en la estructura del JWT.
- Rediseño de roles de usuario.
- Autorizacion por manager o por bodega.
- Rediseño de notificaciones por correo.

## Criterios de Aceptacion

- Un registro publico crea un usuario que no puede iniciar sesion de inmediato.
- Un usuario pendiente recibe una respuesta prohibida al iniciar sesion.
- Un usuario bloqueado recibe una respuesta prohibida al iniciar sesion.
- Un usuario activo puede iniciar sesion y recibir un Bearer token valido.
- Un administrador puede listar usuarios filtrados por estado.
- Un administrador puede activar un usuario pendiente.
- Un administrador puede bloquear un usuario activo.
- Inserciones o updates que rompan la consistencia entre `status` y `enabled` deben ser rechazados por la base de datos.

## Notas de Implementacion

- El flujo debe implementarse en la capa de servicios existente.
- La regla de persistencia debe reflejarse en el esquema SQL.
- No debe introducirse un nuevo framework ni un subsistema arquitectonico.
- Deben preservarse los patrones actuales de controladores y DTOs.

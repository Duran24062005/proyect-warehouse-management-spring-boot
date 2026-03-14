# PRD de Acceso Solo para Usuarios Activos

## Objetivo

Asegurar que solo los usuarios con estado `ACTIVE` puedan acceder a las areas protegidas del sistema, tanto durante login como al usar endpoints protegidos por JWT.

## Alcance

- Validacion de estado de cuenta en login.
- Validacion de estado de cuenta en la autenticacion por request con JWT.
- Aplicacion de la regla usando la logica de servicios existente y el modelo de persistencia actual.
- Sin cambios en configuracion de rutas ni en la arquitectura general de seguridad.

## Contexto de Negocio

El sistema permite que los usuarios existan en distintos estados del ciclo de vida:

- `PENDING`
- `ACTIVE`
- `BLOCKED`

Desde negocio, solo los usuarios que ya fueron aprobados y permanecen activos deben poder entrar al sistema.

Esta regla no debe aplicarse solo al momento del login. Tambien debe aplicarse cuando llega una solicitud con un token ya emitido, para evitar que usuarios que ya no estan activos sigan accediendo a recursos protegidos.

## Requisitos Funcionales

- `POST /api/auth/login` debe permitir autenticacion solo cuando el estado del usuario sea `ACTIVE`.
- `POST /api/auth/login` debe rechazar usuarios con estado `PENDING`.
- `POST /api/auth/login` debe rechazar usuarios con estado `BLOCKED`.
- La autenticacion JWT para endpoints protegidos solo debe poblar el contexto de seguridad cuando el estado sea `ACTIVE`.
- La autenticacion JWT debe ignorar usuarios que no esten activos, incluso si el token es estructuralmente valido.
- La bandera `enabled` existente debe mantenerse consistente con la regla de `userStatus`.

## Regla de Seguridad

Un usuario se considera valido para acceso protegido solo si:

- `enabled = true`
- `userStatus = ACTIVE`

Si alguna de esas condiciones no se cumple, la request no debe autenticarse para recursos protegidos.

## Comportamiento Esperado

### Login

- Los usuarios `ACTIVE` pueden iniciar sesion y recibir un Bearer token.
- Los usuarios `PENDING` reciben una respuesta prohibida.
- Los usuarios `BLOCKED` reciben una respuesta prohibida.

### Acceso con Bearer Token

- Si un token pertenece a un usuario `ACTIVE`, la request se autentica normalmente.
- Si un token pertenece a un usuario `PENDING`, la request no debe autenticarse.
- Si un token pertenece a un usuario `BLOCKED`, la request no debe autenticarse.
- Si un token pertenece a un usuario deshabilitado, la request no debe autenticarse.

## Fuera de Alcance

- Rediseño del JWT.
- Rediseño de autorizacion por rutas.
- Rediseño del modelo de roles.
- Rediseño del flujo de aprobacion.
- Cambios de notificaciones por email.

## Criterios de Aceptacion

- Un usuario pendiente no puede obtener token mediante login.
- Un usuario bloqueado no puede obtener token mediante login.
- Un usuario activo puede iniciar sesion con exito.
- Una request a endpoint protegido con token de un usuario no activo no debe autenticarse.
- Una request a endpoint protegido con token de un usuario activo debe seguir funcionando normalmente.
- La solucion debe encajar en la arquitectura actual y reutilizar el filtro JWT y el servicio de autenticacion existentes.

## Notas de Implementacion

- La regla de login pertenece al servicio de autenticacion existente.
- La validacion por request pertenece al filtro JWT existente.
- La implementacion debe seguir siendo compatible con el modelo `AppUser` actual y el enum `UserStatus`.

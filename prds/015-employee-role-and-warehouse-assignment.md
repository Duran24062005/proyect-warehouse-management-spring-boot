# PRD de Rol Employee y Asignacion de Bodega

## Objetivo

Implementar una primera version formal del concepto de `EMPLOYEE` dentro del sistema, permitiendo:

- agregar el rol `EMPLOYEE` al modelo de usuarios
- asignar una bodega a un empleado mediante `warehouse_id` nullable en `app_user`
- permitir que el admin cree y actualice usuarios con rol `ADMIN`, `USER` o `EMPLOYEE`
- permitir que el manager vea en su panel a los empleados de la bodega que administra

Este PRD **cierra decisiones de la fase 1** y deja fuera de alcance la correccion completa del modelo de movimientos.

## Problema que se Busca Resolver

Hoy el sistema modela bien:

- admins globales
- managers de bodega usando `warehouse.manager`

Pero no modela bien:

- empleados operativos por bodega
- asignacion de personal a una bodega concreta
- visibilidad del manager sobre su equipo

Ademas, el proyecto tiene una ambiguedad importante:

- en movimientos existe un campo llamado `employee`
- pero hoy representa al usuario que registra el movimiento
- no necesariamente a la persona que ejecuto la operacion fisica

Por eso, esta fase se enfoca en **introducir empleados al dominio** sin reestructurar todavia el significado de los movimientos.

## Decision de Diseño Aprobada

La implementacion de fase 1 se hara asi:

- agregar `EMPLOYEE` al enum `UserRole`
- agregar `warehouse_id` nullable en `app_user`
- mapear `AppUser.warehouse` como relacion opcional a `Warehouse`
- usar `warehouse.manager` para managers
- usar `app_user.warehouse_id` para empleados

## Interpretacion del Modelo

### `ADMIN`

- acceso global
- no pertenece operativamente a una bodega
- `warehouse_id` debe ser `null`

### `USER`

- representa manager de bodega
- su relacion principal con bodega sigue siendo `warehouse.manager`
- `warehouse_id` debe mantenerse `null`

### `EMPLOYEE`

- representa trabajador operativo
- debe pertenecer exactamente a una bodega
- `warehouse_id` es obligatorio

## Alcance de la Fase 1

- Nuevo rol `EMPLOYEE`.
- Nueva asignacion opcional `warehouse_id` en `app_user`.
- Validaciones de consistencia entre rol y bodega asignada.
- Ajustes en DTOs de usuario.
- Ajustes en endpoints de administracion de usuarios.
- Ajustes en frontend admin para crear y visualizar empleados con bodega.
- Nueva vista o bloque para managers con listado de empleados a cargo.
- Ajustes en `auth/me` y respuestas de usuario para reflejar la bodega asignada cuando exista.

## Fuera de Alcance de la Fase 1

- Rediseño completo del modelo de movimientos.
- Soporte para que un empleado pertenezca a varias bodegas.
- Historial de cambios de asignacion.
- Jerarquias laborales adicionales.
- Permisos avanzados especificos para empleados mas alla de autenticacion y visibilidad basica.
- Refactor masivo del control de acceso existente.

## Reglas de Negocio

## Regla 1. Roles soportados

El sistema debe soportar estos roles:

- `ADMIN`
- `USER`
- `EMPLOYEE`

## Regla 2. Consistencia entre rol y bodega

Las reglas definitivas de fase 1 seran:

- si `role = ADMIN`, entonces `warehouse_id = null`
- si `role = USER`, entonces `warehouse_id = null`
- si `role = EMPLOYEE`, entonces `warehouse_id` es obligatorio

Si la combinacion enviada no cumple estas reglas, la API debe responder `400`.

## Regla 3. Cambio de rol

Cuando un usuario cambie de rol:

- si cambia a `EMPLOYEE`, debe seleccionarse una bodega valida
- si cambia desde `EMPLOYEE` a `ADMIN` o `USER`, `warehouse_id` debe limpiarse automaticamente

## Regla 4. Manager y empleados

Un manager debe poder ver a los usuarios con rol `EMPLOYEE` cuya `warehouse_id` pertenezca a una bodega donde el manager es responsable.

## Regla 5. Admin y asignacion de empleados

Solo `ADMIN` puede:

- crear usuarios con rol `EMPLOYEE`
- cambiar el rol de un usuario
- asignar o cambiar la bodega de un empleado

## Regla 6. Movimientos en fase 1

En esta fase no se cambia la persistencia de movimientos.

Decision explicita:

- el sistema sigue guardando al usuario autenticado como responsable del registro
- no se introduce todavia `performedByEmployee`
- no se cambia aun la tabla `movement`

Esto se deja para una fase posterior.

## Cambios Backend Requeridos

## Modelo

Se debe actualizar:

- `UserRole` para incluir `EMPLOYEE`
- `AppUser` para agregar relacion opcional con `Warehouse`

Resultado esperado en `AppUser`:

- campo `warehouse`
- `@ManyToOne(fetch = FetchType.LAZY)`
- `@JoinColumn(name = "warehouse_id")`

## Base de datos

Se debe agregar:

- columna `warehouse_id` nullable en tabla `app_user`

Restricciones esperadas:

- FK hacia `warehouse(id)`
- nullable para soportar `ADMIN` y `USER`

## DTOs

Se deben extender los DTOs administrativos y de respuesta de usuario.

### `AdminUserRequestDTO`

Debe incluir:

- `warehouseId`

Uso esperado:

- opcional a nivel de payload
- validado segun el `role`

### `UserResponseDTO`

Debe incluir:

- `warehouseId`
- `warehouseName`

Esto aplica tanto para listados admin como para la respuesta consumida por frontend.

## Servicios

Se debe actualizar al menos:

- `UserService`
- `UserServiceImpl`
- `UserMapper`
- `AuthService` o la ruta `/auth/me` si usa `UserResponseDTO` o estructura equivalente

Comportamiento esperado:

- resolver bodega al crear o actualizar usuarios
- validar reglas de rol contra `warehouseId`
- limpiar bodega cuando el rol ya no sea `EMPLOYEE`

## Endpoints Requeridos

## Endpoints existentes a extender

### `POST /api/users`

Debe aceptar:

- `role`
- `warehouseId`

Debe validar:

- `warehouseId` requerido para `EMPLOYEE`
- `warehouseId` prohibido para `ADMIN` y `USER`

### Nuevo endpoint recomendado

Se aprueba agregar:

- `PUT /api/users/{id}`

Objetivo:

- permitir actualizar rol, datos basicos y bodega asignada desde administracion

Campos esperados:

- `firstName`
- `lastName`
- `phoneNumber`
- `role`
- `warehouseId`
- opcionalmente `enabled` si se quiere mantener la capacidad actual

### Endpoints de consulta

Se deben extender o agregar consultas para soportar visibilidad operativa.

Recomendacion aprobada:

- mantener `GET /api/users`
- mantener `GET /api/users/role`
- mantener `GET /api/users/status`
- agregar `GET /api/users/employees/my-warehouses`

Comportamiento de este nuevo endpoint:

- para `ADMIN`, puede retornar `403` o lista vacia
- para `USER`, retorna empleados de sus bodegas gestionadas
- para `EMPLOYEE`, retorna `403`

Decision recomendada:

- `403` para roles no autorizados

## Cambios Frontend Requeridos

## Panel admin

La pantalla de administracion debe permitir:

- seleccionar rol `ADMIN`, `USER` o `EMPLOYEE`
- mostrar selector de bodega solo cuando el rol sea `EMPLOYEE`
- ocultar o limpiar selector cuando el rol no sea `EMPLOYEE`
- mostrar columna o dato adicional de bodega asignada
- editar usuarios existentes para cambiar rol y bodega

### Reglas visuales en admin

- si el rol seleccionado es `EMPLOYEE`, el selector de bodega es obligatorio
- si el rol es `ADMIN` o `USER`, el selector debe quedar vacio y deshabilitado u oculto

## Panel del manager

Se debe agregar una vista o seccion visible solo para managers.

Objetivo:

- listar empleados de sus bodegas

La decision aprobada para fase 1 es:

- agregar este bloque en el dashboard del manager o en una seccion simple reutilizando el frontend actual

No se requiere crear una pantalla completamente nueva si una seccion es suficiente.

## `auth/me` y sesion frontend

El frontend debe conocer si el usuario autenticado:

- es `EMPLOYEE`
- tiene `warehouseId`
- tiene `warehouseName`

Esto permite personalizar vistas futuras sin volver a rediseñar la sesion.

## Permisos de Empleado en Fase 1

La decision aprobada para `EMPLOYEE` es conservadora.

En fase 1:

- puede autenticarse
- puede consultar su perfil y sesion
- no accede a administracion
- no gestiona bodegas
- no gestiona productos
- no registra movimientos

Objetivo:

- introducir correctamente al actor de dominio sin abrir todavia flujos operativos ambiguos

## Impacto sobre Control de Acceso

No se reemplaza el modelo actual de acceso por bodega.

Se mantiene:

- `ADMIN` acceso global
- `USER` acceso por `warehouse.manager`

Y se agrega:

- `EMPLOYEE` con acceso muy limitado en fase 1

Esto evita mezclar demasiado rapido el modelo operativo con los permisos existentes.

## Fase 2 Implementada

La fase 2 queda aprobada e implementada con esta definicion:

- el movimiento guarda quien registra
- el movimiento guarda que empleado ejecuto la operacion fisica

Modelo aprobado:

- `registeredByUser`
  Usuario autenticado que registra el movimiento en el sistema
- `performedByEmployee`
  Empleado con rol `EMPLOYEE` que realizo la operacion fisica

## Reglas de fase 2 para movimientos

- `performedByEmployeeId` es obligatorio en creacion y actualizacion
- el usuario seleccionado debe tener rol `EMPLOYEE`
- el empleado debe tener `warehouse_id`
- en `ENTRY`, el empleado debe pertenecer a la bodega destino
- en `EXIT`, el empleado debe pertenecer a la bodega origen
- en `TRANSFER`, el empleado debe pertenecer a la bodega origen o destino
- el usuario autenticado sigue siendo quien queda como `registeredByUser`

## Impacto aprobado de fase 2

- el backend deja de usar `movement.employee` con significado ambiguo
- la tabla `movement` pasa a usar:
  - `registered_by_user_id`
  - `performed_by_employee_id`
- el frontend de movimientos mantiene el bloque "Registrado por"
- el frontend de movimientos agrega selector obligatorio para el empleado ejecutor

## Riesgos Identificados

- Confundir `USER` con `EMPLOYEE` si no se comunica bien en frontend.
- Introducir el nuevo rol sin reforzar validaciones de `warehouseId`.
- Intentar incluir movimientos en esta misma fase y mezclar demasiados cambios.
- No exponer `warehouseName` en respuestas y dejar al frontend sin contexto suficiente.

## Criterios de Aceptacion

- El enum `UserRole` incluye `EMPLOYEE`.
- `AppUser` soporta `warehouse_id` nullable.
- La API valida correctamente las combinaciones:
  - `ADMIN` sin bodega
  - `USER` sin bodega
  - `EMPLOYEE` con bodega obligatoria
- El admin puede crear usuarios `EMPLOYEE` y asignarles bodega.
- El admin puede actualizar rol y bodega de un usuario existente.
- La respuesta de usuario expone `warehouseId` y `warehouseName`.
- El manager puede consultar empleados de sus bodegas.
- El frontend admin muestra y gestiona el nuevo rol y la bodega asignada.
- El frontend del manager muestra empleados a cargo.
- Los movimientos no se modifican estructuralmente en esta fase.

## Resumen Ejecutivo

La implementacion aprobada es:

1. introducir `EMPLOYEE`
2. agregar `warehouse_id` en `app_user`
3. permitir asignacion de empleados a una bodega desde admin
4. permitir al manager ver su personal
5. dejar movimientos para una segunda fase semantica

Esta es la version recomendada para construir primero porque resuelve la necesidad real del negocio sin meter en el mismo cambio una refactorizacion grande del modulo de movimientos.

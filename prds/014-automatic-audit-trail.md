# PRD de Auditoria Automatica con Listeners

## Objetivo

Migrar la auditoria actual basada en llamadas manuales desde servicios a un enfoque hibrido con listeners JPA, manteniendo `audit_change` y `entity` como almacenamiento oficial del historial.

El resultado esperado es que las mutaciones sobre usuarios, productos, bodegas y movimientos se auditen automaticamente durante el ciclo de vida de persistencia, sin duplicar llamadas en cada servicio.

## Como funcionan los listeners

Un listener de JPA es un componente que reacciona automaticamente cuando una entidad pasa por eventos como:

- `@PostLoad`
- `@PostPersist`
- `@PostUpdate`
- `@PostRemove`

En este proyecto, cada entidad auditable registra el listener `AuditEntityListener`. Cuando Hibernate carga, crea, actualiza o elimina una entidad, el listener:

1. captura un snapshot seguro del estado,
2. identifica el tipo de operacion,
3. publica un evento de auditoria,
4. y delega al servicio tecnico la persistencia final en `audit_change`.

## Beneficios de usar listeners aqui

- eliminan la duplicacion de `logInsert`, `logUpdate` y `logDelete` en la capa de servicios;
- reducen el riesgo de olvidar auditoria en nuevos flujos;
- centralizan reglas sensibles como exclusion de `hashPassword`;
- hacen que distintas rutas de negocio auditen de forma consistente;
- desacoplan la auditoria del controlador o del servicio puntual que realiza la mutacion.

## Alcance

- Auditoria automatica para `INSERT`, `UPDATE` y `DELETE`.
- Modulos auditados:
  - `app_user`
  - `product`
  - `warehouse`
  - `movement`
- Persistencia de actor autenticado cuando exista.
- Fallback de actor al usuario creado en autoregistro.
- Snapshots JSON seguros para `old_values` y `new_values`.

## Requisitos funcionales

- Crear usuario por registro debe generar auditoria `INSERT`.
- Crear usuario por admin debe generar auditoria `INSERT`.
- Actualizar usuario debe generar auditoria `UPDATE`.
- Aprobar, bloquear y desbloquear usuario debe generar auditoria `UPDATE`.
- Cambiar la contrasena debe generar auditoria `UPDATE` sin exponer contrasena ni hash.
- Subir foto de perfil debe generar auditoria `UPDATE`.
- Crear, actualizar y eliminar productos debe generar auditoria.
- Crear, actualizar y eliminar bodegas debe generar auditoria.
- Crear, actualizar y eliminar movimientos debe generar auditoria.

## Reglas del payload

- `INSERT`
  - `old_values = null`
  - `new_values = snapshot seguro`
- `UPDATE`
  - `old_values = snapshot original`
  - `new_values = snapshot actualizado`
- `DELETE`
  - `old_values = snapshot previo a eliminar`
  - `new_values = null`

### Snapshots por entidad

#### `AppUser`

Debe incluir:

- `id`
- `email`
- `firstName`
- `lastName`
- `phoneNumber`
- `role`
- `userStatus`
- `enabled`
- `warehouseId`
- `profilePhotoFilename`

Debe excluir:

- `hashPassword`

Regla especial:

- Si el hash cambia entre snapshot previo y nuevo, `new_values` debe incluir `passwordChanged = true`.

#### `Product`

- `id`
- `name`
- `category`
- `price`
- `warehouseId`

#### `Warehouse`

- `id`
- `name`
- `ubication`
- `capacity`
- `managerUserId`

#### `Movement`

- `id`
- `movementType`
- `registeredByUserId`
- `performedByEmployeeId`
- `originWarehouseId`
- `destinationWarehouseId`
- `productId`
- `quantity`

## Reglas del actor

- Si existe usuario autenticado en `SecurityContextHolder`, ese usuario debe guardarse como `actor_user_id`.
- Si no existe autenticacion y la operacion es autoregistro, debe usarse el usuario creado como actor.
- Si no existe autenticacion y no hay fallback valido, `actor_user_id` puede quedar `null`.

## Enfoque tecnico

- Las entidades auditables implementan `AuditableEntity`.
- El listener `AuditEntityListener` se registra con `@EntityListeners(...)`.
- `@PostLoad` captura snapshot original en memoria.
- `@PostPersist`, `@PostUpdate` y `@PostRemove` publican eventos internos.
- `AuditService` construye snapshots seguros, resuelve actor, serializa JSON y persiste `AuditChange`.
- `AuditEventHandler` consume el evento en fase `BEFORE_COMMIT` para evitar persistencia directa dentro del callback JPA.

## Fuera de alcance

- Endpoints de consulta de auditoria.
- UI para historial de auditoria.
- Filtros avanzados por actor o fecha.
- Rollback de cambios historicos.
- Integracion con Hibernate Envers.

## Criterios de aceptacion

- Las mutaciones de `app_user`, `product`, `warehouse` y `movement` crean registros automaticos en `audit_change`.
- Los servicios dejan de llamar manualmente a la auditoria.
- Los snapshots de `AppUser` no exponen `hashPassword`.
- Los cambios de contrasena quedan trazados con `passwordChanged = true`.
- La tabla `entity` sigue resolviendo o creando el tipo auditable faltante.
- La solucion compila y no cambia los contratos REST existentes.

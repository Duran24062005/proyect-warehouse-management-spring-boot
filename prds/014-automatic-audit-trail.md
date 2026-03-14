# PRD de Auditoria Automatica

## Objetivo

Integrar registro automatico de auditoria para usuarios, productos, bodegas y movimientos usando la capa de servicios actual y las tablas de auditoria ya existentes.

## Alcance

- Auditoria automatica para `INSERT`, `UPDATE` y `DELETE`.
- Modulos auditados:
  - usuarios
  - productos
  - bodegas
  - movimientos
- Registro del actor usando el usuario autenticado cuando exista.
- Snapshots JSON de valores anteriores y nuevos.
- Reutilizacion de las tablas `audit_change` y `entity`.

## Contexto de Negocio

El sistema ya contiene el modelo de dominio para auditoria:

- `AuditChange`
- `TrackedEntity`
- `OperationType`

Tambien ya existe el esquema de persistencia para almacenar la auditoria.

Lo que faltaba era integrarlo automaticamente con los flujos de negocio que mutan el sistema.

El objetivo de esta iteracion es asegurar que las acciones de negocio relevantes creen entradas de auditoria automaticamente sin rediseñar la arquitectura.

## Requisitos Funcionales

- Crear un usuario debe generar una auditoria `INSERT`.
- Actualizar estado de usuario debe generar una auditoria `UPDATE`.
- Aprobar, bloquear y desbloquear un usuario debe generar una auditoria `UPDATE`.
- Cambiar la contrasena debe generar una auditoria `UPDATE` sin exponer la contrasena en texto plano.
- Crear un producto debe generar una auditoria `INSERT`.
- Actualizar un producto debe generar una auditoria `UPDATE`.
- Eliminar un producto debe generar una auditoria `DELETE`.
- Crear una bodega debe generar una auditoria `INSERT`.
- Actualizar una bodega debe generar una auditoria `UPDATE`.
- Eliminar una bodega debe generar una auditoria `DELETE`.
- Crear un movimiento debe generar una auditoria `INSERT`.
- Actualizar un movimiento debe generar una auditoria `UPDATE`.
- Eliminar un movimiento debe generar una auditoria `DELETE`.

## Reglas del Payload de Auditoria

- `old_values` debe almacenar el estado anterior cuando aplique.
- `new_values` debe almacenar el estado resultante cuando aplique.
- `INSERT`:
  - `old_values = null`
  - `new_values = snapshot creado`
- `UPDATE`:
  - `old_values = snapshot anterior`
  - `new_values = snapshot actualizado`
- `DELETE`:
  - `old_values = snapshot eliminado`
  - `new_values = null`

## Reglas del Actor

- Si la operacion es ejecutada por un usuario autenticado, ese usuario debe guardarse en `actor_user_id`.
- En auto-registro, el usuario creado puede usarse como actor porque la accion es auto-iniciada.
- El mecanismo de auditoria no debe depender de logica especifica de controladores.

## Reglas de Seguridad de Datos

- Los snapshots de usuarios deben evitar almacenar contrasenas en texto plano.
- Los cambios de contrasena deben representarse con metadata segura y no con valores sensibles.
- Los snapshots de productos, bodegas y movimientos pueden apoyarse en sus representaciones seguras actuales.

## Enfoque Tecnico

La implementacion debe:

- reutilizar la capa de servicios como punto de integracion
- usar un servicio compartido de auditoria para evitar duplicacion
- serializar snapshots a JSON para `old_values` y `new_values`
- resolver la entidad afectada mediante el catalogo `entity`
- crear registros faltantes de entidades trazables si todavia no existen

## Fuera de Alcance

- Endpoints de consulta de auditoria.
- Filtros de auditoria por API.
- Pantallas UI para auditoria.
- Rediseño con AOP o reemplazo de framework.
- Funcionalidades de rollback o undo historico.

## Criterios de Aceptacion

- Los flujos mutativos de usuarios crean registros de auditoria automaticamente.
- Los flujos mutativos de productos crean registros de auditoria automaticamente.
- Los flujos mutativos de bodegas crean registros de auditoria automaticamente.
- Los flujos mutativos de movimientos crean registros de auditoria automaticamente.
- Cada registro guarda tipo de operacion, actor, entidad afectada y snapshots JSON.
- Los datos sensibles de autenticacion no se exponen en auditoria.
- La solucion compila y encaja en la arquitectura por capas actual.

## Notas de Implementacion

- La integracion de auditoria debe permanecer dentro de los servicios de negocio del backend.
- Un servicio compartido debe encapsular persistencia y serializacion JSON.
- Los mapeadores y DTOs seguros existentes deben reutilizarse tanto como sea posible para construir snapshots.

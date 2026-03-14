# PRD de API de Bodegas

## Objetivo

Proveer una API REST protegida para gestionar registros de bodegas y asignar managers.

## Alcance

- Operaciones CRUD para bodegas.
- Consulta por id.
- Asignacion de manager mediante `managerUserId`.
- Documentacion Swagger de modelos de request y response.
- Proteccion Bearer JWT para todos los endpoints de bodegas.

## Requisitos Funcionales

- `GET /api/warehouses` debe listar todas las bodegas para usuarios autenticados.
- `GET /api/warehouses/{id}` debe retornar una bodega para usuarios autenticados.
- `POST /api/warehouses` debe crear una bodega para usuarios autenticados.
- `PUT /api/warehouses/{id}` debe actualizar una bodega para usuarios autenticados.
- `DELETE /api/warehouses/{id}` debe eliminar una bodega para usuarios autenticados.
- Actualizar `managerUserId` debe permitir asignar o reemplazar el manager de una bodega.

## Requisitos de Datos

- Las solicitudes de bodega deben aceptar `name`, `ubication`, `capacity` y `managerUserId` opcional.
- Las respuestas de bodega deben incluir datos resumidos del manager en lugar de la entidad completa.

## Criterios de Aceptacion

- Las operaciones CRUD persisten sobre la tabla `warehouse`.
- Un `managerUserId` invalido retorna `404`.
- Una bodega inexistente retorna `404`.
- Las solicitudes anonimas a `/api/warehouses/**` retornan `401`.
- El frontend administrativo puede asignar un manager enviando `managerUserId` en `PUT /api/warehouses/{id}`.

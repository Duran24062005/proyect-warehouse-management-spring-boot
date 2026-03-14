# PRD de API de Movimientos

## Objetivo

Proveer una API REST protegida para registrar y consultar movimientos de inventario.

## Alcance

- Operaciones CRUD para movimientos.
- Consulta por id.
- Filtros por producto y bodega.
- Validacion de reglas de movimiento segun las restricciones del esquema.
- Proteccion Bearer JWT para todos los endpoints de movimientos.

## Requisitos Funcionales

- `GET /api/movements` debe listar todos los movimientos para usuarios autenticados.
- `GET /api/movements/{id}` debe retornar un movimiento para usuarios autenticados.
- `GET /api/movements?productId={id}` debe filtrar por producto.
- `GET /api/movements?warehouseId={id}` debe filtrar por bodega.
- `POST /api/movements` debe crear un movimiento para usuarios autenticados.
- `PUT /api/movements/{id}` debe actualizar un movimiento para usuarios autenticados.
- `DELETE /api/movements/{id}` debe eliminar un movimiento para usuarios autenticados.

## Reglas de Negocio

- `ENTRY` requiere `destinationWarehouseId` y no `originWarehouseId`.
- `EXIT` requiere `originWarehouseId` y no `destinationWarehouseId`.
- `TRANSFER` requiere ambas bodegas y estas deben ser diferentes.
- Los ids relacionados como `employeeUserId`, `productId`, `originWarehouseId` y `destinationWarehouseId` deben existir.

## Criterios de Aceptacion

- Las operaciones CRUD persisten sobre la tabla `movement`.
- Los ids relacionados invalidos retornan `404`.
- Las combinaciones invalidas de bodegas retornan `400`.
- Las solicitudes anonimas a `/api/movements/**` retornan `401`.
- Los calculos de stock bajo pueden apoyarse en movimientos persistidos.

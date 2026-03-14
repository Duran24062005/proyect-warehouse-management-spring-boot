# PRD de API de Productos

## Objetivo

Proveer una API REST completa para gestionar productos y consultar productos con stock bajo.

## Alcance

- Operaciones CRUD para productos.
- Consulta de producto por id.
- Consulta de stock bajo basada en movimientos de inventario.
- Documentacion Swagger de endpoints y payloads.
- Proteccion Bearer JWT para todos los endpoints de productos.

## Requisitos Funcionales

- `GET /api/products` debe listar todos los productos para usuarios autenticados.
- `GET /api/products/{id}` debe retornar un producto para usuarios autenticados.
- `POST /api/products` debe crear un producto para usuarios autenticados.
- `PUT /api/products/{id}` debe actualizar un producto para usuarios autenticados.
- `DELETE /api/products/{id}` debe eliminar un producto para usuarios autenticados.
- `GET /api/products/low-stock` debe retornar productos con stock calculado menor o igual al umbral configurado para usuarios autenticados.

## Requisitos de Datos

- Las solicitudes de producto deben incluir `name`, `category`, `price` y `warehouseId` opcional.
- Las respuestas de producto deben incluir un resumen de bodega en lugar de la entidad relacionada completa.

## Criterios de Aceptacion

- Las operaciones CRUD persisten sobre la tabla `product`.
- Un `warehouseId` invalido retorna `404`.
- Un producto inexistente retorna `404`.
- Los resultados de stock bajo se calculan desde la tabla `movement`.
- Las solicitudes anonimas a `/api/products/**` retornan `401`.

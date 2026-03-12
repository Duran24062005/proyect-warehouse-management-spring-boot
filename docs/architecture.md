# Architecture

## Overview

`warehouse-management` es un backend monolitico en Spring Boot para gestionar productos, almacenes y movimientos de inventario sobre MySQL.

La aplicacion sigue una arquitectura por capas:

- `controllers`
  Expone endpoints REST y define el contrato HTTP.
- `services`
  Contiene reglas de negocio, validaciones y orquestacion.
- `repositories`
  Accede a base de datos usando Spring Data JPA.
- `mapper`
  Convierte entidades JPA a DTOs de request/response.
- `model`
  Define entidades persistentes y enums del dominio.
- `config`
  Contiene configuracion transversal, como OpenAPI/Swagger.
- `exceptions`
  Centraliza manejo global de errores HTTP.

## Current Modules

- `Product`
  CRUD de productos y consulta de stock bajo.
- `Warehouse`
  CRUD de almacenes y asignacion de manager.
- `Movement`
  CRUD de movimientos y validacion de reglas `ENTRY`, `EXIT` y `TRANSFER`.

## Request Flow

1. El cliente llama un endpoint REST.
2. El `Controller` recibe parametros, body y validaciones basicas.
3. El `Service` aplica reglas de negocio y resuelve relaciones.
4. El `Repository` ejecuta operaciones JPA o queries derivadas/custom.
5. El `Mapper` transforma entidades a DTOs de salida.
6. `GlobalExceptionHandler` normaliza errores en JSON consistente.

## Persistence Model

Tablas principales:

- `product`
- `warehouse`
- `movement`
- `app_user`
- `audit_change`
- `entity`

Relaciones relevantes:

- Un `Product` pertenece opcionalmente a un `Warehouse`.
- Un `Warehouse` puede tener un manager (`AppUser`).
- Un `Movement` referencia un `Product`, un empleado (`AppUser`) y una o dos bodegas segun el tipo.

## API Design

- Base URL local: `http://localhost:8000`
- Documentacion interactiva: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`
- DTOs de salida evitan exponer entidades completas relacionadas cuando no es necesario.

## Error Handling

La API usa `@RestControllerAdvice` para devolver un payload uniforme con:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details`

Esto cubre:

- errores de validacion
- ids inexistentes
- requests malformados
- errores internos no controlados

## Transactions

La capa de servicio usa transacciones para:

- mantener consistencia en operaciones de escritura
- evitar problemas de carga `LAZY` al mapear relaciones a DTOs

## Current Limitations

- No hay autenticacion ni autorizacion.
- No hay capa de auditoria automatica conectada a `audit_change`.
- No hay pruebas funcionales o de integracion para los modulos nuevos.
- El calculo de stock bajo se basa en movimientos agregados, no en una tabla de inventario dedicada.

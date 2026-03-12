# Movements API PRD

## Objective
Provide a REST API to register and query inventory movements.

## Scope
- CRUD operations for movements.
- Lookup by id.
- Filters by product and warehouse.
- Validation of movement rules according to the schema constraints.
- Authentication requirement using Bearer JWT.

## Functional Requirements
- `GET /api/movements` must list all movements.
- `GET /api/movements/{id}` must return one movement.
- `GET /api/movements?productId={id}` must filter by product.
- `GET /api/movements?warehouseId={id}` must filter by warehouse.
- `POST /api/movements` must create a movement.
- `PUT /api/movements/{id}` must update a movement.
- `DELETE /api/movements/{id}` must delete a movement.

## Business Rules
- `ENTRY` requires `destinationWarehouseId` and no `originWarehouseId`.
- `EXIT` requires `originWarehouseId` and no `destinationWarehouseId`.
- `TRANSFER` requires both warehouses and they must be different.

## Acceptance Criteria
- CRUD operations persist against the `movement` table.
- Invalid related ids return `404`.
- Invalid warehouse combinations return `400`.
- Requests without valid JWT return `401`.

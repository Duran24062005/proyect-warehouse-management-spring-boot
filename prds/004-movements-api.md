# Movements API PRD

## Objective

Provide a protected REST API to register and query inventory movements.

## Scope

- CRUD operations for movements.
- Lookup by id.
- Filters by product and warehouse.
- Validation of movement rules according to the schema constraints.
- Bearer JWT protection for all movement endpoints.

## Functional Requirements

- `GET /api/movements` must list all movements for authenticated users.
- `GET /api/movements/{id}` must return one movement for authenticated users.
- `GET /api/movements?productId={id}` must filter by product.
- `GET /api/movements?warehouseId={id}` must filter by warehouse.
- `POST /api/movements` must create a movement for authenticated users.
- `PUT /api/movements/{id}` must update a movement for authenticated users.
- `DELETE /api/movements/{id}` must delete a movement for authenticated users.

## Business Rules

- `ENTRY` requires `destinationWarehouseId` and no `originWarehouseId`.
- `EXIT` requires `originWarehouseId` and no `destinationWarehouseId`.
- `TRANSFER` requires both warehouses and they must be different.
- Related ids such as `employeeUserId`, `productId`, `originWarehouseId` and `destinationWarehouseId` must exist.

## Acceptance Criteria

- CRUD operations persist against the `movement` table.
- Invalid related ids return `404`.
- Invalid warehouse combinations return `400`.
- Anonymous requests to `/api/movements/**` return `401`.
- Product low-stock calculations can rely on persisted movements.

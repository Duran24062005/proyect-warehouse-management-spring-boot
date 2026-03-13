# Warehouses API PRD

## Objective
Provide a REST API to manage warehouse records and assign managers.

## Scope
- CRUD operations for warehouses.
- Warehouse lookup by id.
- Swagger documentation for request and response models.

## Functional Requirements
- `GET /api/warehouses` must list all warehouses.
- `GET /api/warehouses/{id}` must return one warehouse.
- `POST /api/warehouses` must create a warehouse.
- `PUT /api/warehouses/{id}` must update a warehouse.
- `DELETE /api/warehouses/{id}` must delete a warehouse.

## Data Requirements
- Warehouse requests must accept `name`, `ubication`, `capacity`, and optional `managerUserId`.
- Warehouse responses must include manager summary data.

## Acceptance Criteria
- CRUD operations persist against the `warehouse` table.
- Invalid `managerUserId` returns `404`.
- Missing warehouses return `404`.

# Warehouses API PRD

## Objective

Provide a protected REST API to manage warehouse records and assign managers.

## Scope

- CRUD operations for warehouses.
- Warehouse lookup by id.
- Manager assignment through `managerUserId`.
- Swagger documentation for request and response models.
- Bearer JWT protection for all warehouse endpoints.

## Functional Requirements

- `GET /api/warehouses` must list all warehouses for authenticated users.
- `GET /api/warehouses/{id}` must return one warehouse for authenticated users.
- `POST /api/warehouses` must create a warehouse for authenticated users.
- `PUT /api/warehouses/{id}` must update a warehouse for authenticated users.
- `DELETE /api/warehouses/{id}` must delete a warehouse for authenticated users.
- Updating `managerUserId` must allow assigning or replacing the manager of a warehouse.

## Data Requirements

- Warehouse requests must accept `name`, `ubication`, `capacity`, and optional `managerUserId`.
- Warehouse responses must include manager summary data instead of the full related entity.

## Acceptance Criteria

- CRUD operations persist against the `warehouse` table.
- Invalid `managerUserId` returns `404`.
- Missing warehouses return `404`.
- Anonymous requests to `/api/warehouses/**` return `401`.
- The admin frontend can assign a manager by sending `managerUserId` in `PUT /api/warehouses/{id}`.

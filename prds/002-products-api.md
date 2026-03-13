# Products API PRD

## Objective
Provide a complete REST API to manage products and inspect low-stock products.

## Scope
- CRUD operations for products.
- Product lookup by id.
- Low-stock query based on inventory movements.
- Swagger documentation for endpoints and payloads.
- Bearer JWT protection for all product endpoints.

## Functional Requirements
- `GET /api/products` must list all products for authenticated users.
- `GET /api/products/{id}` must return one product for authenticated users.
- `POST /api/products` must create a product for authenticated users.
- `PUT /api/products/{id}` must update a product for authenticated users.
- `DELETE /api/products/{id}` must delete a product for authenticated users.
- `GET /api/products/low-stock` must return products with computed stock lower than or equal to the configured threshold for authenticated users.

## Data Requirements
- Product requests must include `name`, `category`, `price`, and optional `warehouseId`.
- Product responses must include warehouse summary fields instead of the full related entity.

## Acceptance Criteria
- CRUD operations persist against the `product` table.
- Invalid `warehouseId` returns `404`.
- Missing products return `404`.
- Low-stock results are computed from the `movement` table.
- Anonymous requests to `/api/products/**` return `401`.

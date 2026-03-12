# Products API PRD

## Objective
Provide a complete REST API to manage products and inspect low-stock products.

## Scope
- CRUD operations for products.
- Product lookup by id.
- Low-stock query based on inventory movements.
- Swagger documentation for endpoints and payloads.
- Authentication requirement using Bearer JWT.

## Functional Requirements
- `GET /api/products` must list all products.
- `GET /api/products/{id}` must return one product.
- `POST /api/products` must create a product.
- `PUT /api/products/{id}` must update a product.
- `DELETE /api/products/{id}` must delete a product.
- `GET /api/products/low-stock` must return products with computed stock lower than or equal to the configured threshold.

## Data Requirements
- Product requests must include `name`, `category`, `price`, and optional `warehouseId`.
- Product responses must include warehouse summary fields instead of the full related entity.

## Acceptance Criteria
- CRUD operations persist against the `product` table.
- Invalid `warehouseId` returns `404`.
- Missing products return `404`.
- Low-stock results are computed from the `movement` table.
- Requests without valid JWT return `401`.

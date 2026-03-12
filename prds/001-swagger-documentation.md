# Swagger Documentation PRD

## Objective
Expose interactive API documentation for the warehouse management backend through Swagger UI and OpenAPI JSON.

## Scope
- Integrate `springdoc-openapi` in the Spring Boot application.
- Publish Swagger UI under a stable local URL.
- Define global API metadata such as title, description, version, and server.
- Document Bearer JWT authentication in Swagger UI.

## Functional Requirements
- The application must expose Swagger UI at `/swagger-ui.html`.
- The application must expose OpenAPI JSON at `/v3/api-docs`.
- Product, warehouse, and movement endpoints must appear automatically in Swagger.

## Non-Goals
- Custom Swagger themes.

## Acceptance Criteria
- Opening `http://localhost:8000/swagger-ui.html` shows interactive docs.
- Opening `http://localhost:8000/v3/api-docs` returns OpenAPI JSON.
- Controllers annotated with OpenAPI metadata are rendered in Swagger UI.
- Swagger UI exposes an `Authorize` action for Bearer tokens.

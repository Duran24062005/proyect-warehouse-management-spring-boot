# Swagger Documentation PRD

## Objective

Expose interactive API documentation for the current warehouse management backend, including JWT-protected routes and OpenAPI metadata.

## Scope

- Integrate `springdoc-openapi` in the Spring Boot application.
- Publish Swagger UI and OpenAPI JSON under stable local URLs.
- Define global API metadata and Bearer security scheme.
- Reflect auth, users, products, warehouses and movements in Swagger.

## Functional Requirements

- The application must expose Swagger UI at `/swagger-ui.html`.
- The application must expose OpenAPI JSON at `/v3/api-docs`.
- Controllers annotated with OpenAPI metadata must appear automatically in Swagger.
- Protected endpoints must declare Bearer authentication requirements.
- Swagger UI must remain publicly reachable in local development.

## Non-Goals

- Custom Swagger themes.
- Runtime role-based restriction for Swagger UI access.

## Acceptance Criteria

- Opening `http://localhost:8000/swagger-ui.html` shows interactive docs.
- Opening `http://localhost:8000/v3/api-docs` returns OpenAPI JSON.
- Authenticated endpoints display the Bearer security requirement.
- Auth, users, products, warehouses and movements are discoverable from Swagger UI.

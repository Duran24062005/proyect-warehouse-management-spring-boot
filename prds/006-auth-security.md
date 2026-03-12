# Auth Security PRD

## Objective
Protect the API with stateless authentication and role-based authorization while keeping Swagger usable for manual testing.

## Scope
- Bearer JWT authentication.
- Public registration and login.
- Authenticated profile and password change endpoints.
- Admin-only user listing endpoints.
- Protection for products, warehouses, and movements endpoints.

## Functional Requirements
- `POST /api/auth/register` must create `USER` accounts only.
- `POST /api/auth/login` must validate credentials and return a JWT.
- `GET /api/auth/me` must return the authenticated user from the token.
- `PATCH /api/auth/change-password` must require a valid token and current password.
- `GET /api/users` and `GET /api/users/role` must require role `ADMIN`.
- Swagger UI and static frontend assets must remain publicly accessible.

## Security Rules
- JWT must be required for protected endpoints.
- Invalid or missing token must return `401`.
- Insufficient role must return `403`.
- Passwords must not be returned in API responses.

## Acceptance Criteria
- Login returns a usable Bearer token.
- Protected endpoints reject anonymous requests.
- Admin-only endpoints reject authenticated non-admin users.
- Swagger UI can authorize using the returned token.

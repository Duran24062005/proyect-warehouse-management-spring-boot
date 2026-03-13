# Auth Security PRD

## Objective
Protect the API with stateless authentication and role-based authorization while keeping Swagger usable for manual testing.

## Scope
- Bearer JWT authentication.
- Public registration and login.
- Authenticated profile and password change endpoints.
- Admin-only user listing endpoints.
- Protection for products, warehouses, and movements endpoints.
- Swagger and static frontend support for manual authentication testing.

## Functional Requirements
- `POST /api/auth/register` must create `USER` accounts only.
- `POST /api/auth/login` must validate credentials and return a JWT.
- `GET /api/auth/me` must return the authenticated user from the token.
- `PATCH /api/auth/change-password` must require a valid token and current password, without receiving `userId` in the payload.
- `GET /api/users` and `GET /api/users/role` must require role `ADMIN`.
- `/api/products/**`, `/api/warehouses/**`, and `/api/movements/**` must require a valid Bearer token.
- Swagger UI and static frontend assets must remain publicly accessible.

## Security Rules
- JWT must be required for protected endpoints.
- Invalid or missing token must return `401`.
- Insufficient role must return `403`.
- Passwords must not be returned in API responses.
- JWT subject must map to the authenticated user's email.

## Acceptance Criteria
- Login returns a usable Bearer token.
- Public registration works without providing a role in the request body.
- Protected endpoints reject anonymous requests.
- Admin-only endpoints reject authenticated non-admin users.
- Swagger UI can authorize using the returned token.


## Usuario de prueba

admin@logitrack.com / Admin123!
mlopez@logitrack.com / User123!
jgarcia@logitrack.com / User123!
cperez@logitrack.com / User123!

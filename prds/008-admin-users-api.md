# Admin Users API PRD

## Objective

Provide an admin-only API to inspect users and create new accounts from the internal administration console.

## Scope

- List all users.
- Filter users by role.
- Create new users with explicit role and enabled flag.
- Bearer JWT protection with role `ADMIN`.

## Functional Requirements

- `GET /api/users` must list all users for authenticated admins.
- `GET /api/users/role?role=ADMIN` must filter users by role for authenticated admins.
- `POST /api/users` must create a new user for authenticated admins.
- Admin-created users must allow role selection between `USER` and `ADMIN`.
- Passwords must never be returned in the response payload.

## Data Requirements

- Request payload must accept:
  - `email`
  - `password`
  - `firstName`
  - `lastName`
  - `phoneNumber`
  - `role`
  - `enabled`
- Response payload must expose only safe user profile fields.

## Acceptance Criteria

- Anonymous requests return `401`.
- Authenticated non-admin requests return `403`.
- Duplicate emails return a client-visible validation or business error.
- The admin frontend can consume these endpoints to create and list users.

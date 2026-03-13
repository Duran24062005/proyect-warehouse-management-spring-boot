# Static Frontend App PRD

## Objective

Provide a professional static frontend served by Spring Boot to consume the secured warehouse management API.

## Scope

- Public login and register pages.
- Authenticated dashboard, products, profile and admin views.
- Shared JavaScript modules for session and API access.
- Shared CSS for branding and layout.

## Functional Requirements

- `/` must expose the login screen.
- `/register.html` must expose the registration screen.
- `/platform/system.html` must expose the authenticated dashboard.
- `/platform/products.html` must expose product management and low-stock views.
- `/platform/profile.html` must expose current-user data and password change.
- `/platform/admin.html` must expose admin-only operations such as user creation and manager assignment.
- The frontend must store and reuse the JWT returned by login.
- Protected API calls must send `Authorization: Bearer <token>`.

## UX Requirements

- The application must use the project logo to reinforce brand identity.
- Auth screens and platform screens must share a consistent visual language.
- The structure must separate shared code from page-specific code for maintainability.

## Acceptance Criteria

- A user can register, log in and navigate to the internal platform.
- An authenticated user can consume protected product, warehouse and movement endpoints.
- An admin user can access admin flows backed by `/api/users` and warehouse manager assignment.
- The codebase organizes frontend logic into `js/core`, `js/pages` and shared styles.

# Global Exception Handling PRD

## Objective
Provide a uniform API error contract for validation errors, business errors, and unexpected failures.

## Scope
- Global exception interception with `@RestControllerAdvice`.
- Standard JSON error payload.
- Validation and malformed request handling.

## Functional Requirements
- The API must return a consistent JSON structure for handled exceptions.
- Validation errors must include field-level details.
- `ResponseStatusException` must preserve the intended HTTP status.
- Unexpected errors must return `500` without exposing stack traces in the response body.

## Response Contract
- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details`

## Acceptance Criteria
- Invalid payloads return `400` with validation details.
- Not-found scenarios return `404` using the standard error payload.
- Unexpected exceptions return `500` with the same contract.

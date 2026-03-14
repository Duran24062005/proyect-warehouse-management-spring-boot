# PRD de Manejo Global de Excepciones

## Objetivo

Proveer un contrato uniforme de errores de API para errores de validacion, errores de negocio, errores de autenticacion y fallos inesperados.

## Alcance

- Intercepcion global de excepciones con `@RestControllerAdvice`.
- Payload JSON estandar para errores.
- Manejo de validaciones y requests malformados.
- Manejo consistente para escenarios de negocio y no encontrados.

## Requisitos Funcionales

- La API debe retornar una estructura JSON consistente para las excepciones manejadas.
- Los errores de validacion deben incluir detalles por campo cuando existan.
- `ResponseStatusException` debe preservar el codigo HTTP esperado.
- Los errores inesperados deben retornar `500` sin exponer stack traces en el cuerpo de la respuesta.
- Las rutas protegidas deben poder seguir exponiendo estados de seguridad como `401` y `403`.

## Contrato de Respuesta

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details`

## Criterios de Aceptacion

- Payloads invalidos retornan `400` con detalles de validacion.
- Los escenarios de no encontrado retornan `404` usando el payload estandar.
- Las solicitudes no autorizadas a recursos protegidos retornan `401`.
- Las solicitudes prohibidas a recursos solo admin retornan `403`.
- Las excepciones inesperadas retornan `500` con el mismo contrato.

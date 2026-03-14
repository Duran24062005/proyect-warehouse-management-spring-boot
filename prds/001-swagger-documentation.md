# PRD de Documentacion Swagger

## Objetivo

Exponer documentacion interactiva de la API para el backend actual de gestion de bodegas, incluyendo rutas protegidas con JWT y metadatos OpenAPI.

## Alcance

- Integrar `springdoc-openapi` en la aplicacion Spring Boot.
- Publicar Swagger UI y el JSON OpenAPI en URLs locales estables.
- Definir metadatos globales de la API y el esquema de seguridad Bearer.
- Reflejar autenticacion, usuarios, productos, bodegas y movimientos en Swagger.

## Requisitos Funcionales

- La aplicacion debe exponer Swagger UI en `/swagger-ui.html`.
- La aplicacion debe exponer el JSON OpenAPI en `/v3/api-docs`.
- Los controladores anotados con metadatos OpenAPI deben aparecer automaticamente en Swagger.
- Los endpoints protegidos deben declarar el requerimiento de autenticacion Bearer.
- Swagger UI debe seguir siendo accesible publicamente en desarrollo local.

## No Objetivos

- Temas visuales personalizados para Swagger.
- Restriccion dinamica por roles para el acceso a Swagger UI.

## Criterios de Aceptacion

- Abrir `http://localhost:8000/swagger-ui.html` muestra documentacion interactiva.
- Abrir `http://localhost:8000/v3/api-docs` devuelve el JSON OpenAPI.
- Los endpoints autenticados muestran el requerimiento de seguridad Bearer.
- Autenticacion, usuarios, productos, bodegas y movimientos son visibles desde Swagger UI.

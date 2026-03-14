# PRD de Notificaciones por Email en Autenticacion

## Objetivo

Enviar notificaciones por correo a traves del servicio externo FastAPI cuando un usuario se registra o inicia sesion.

## Alcance

- Notificacion por correo en registro.
- Notificacion por correo en login.
- Configuracion de la URL base del proveedor de email desde `application.properties`.
- Resolucion del proveedor mediante patron factory.

## Flujo Funcional

1. Un usuario completa `POST /api/auth/register` o `POST /api/auth/login`.
2. `AuthController` delega la solicitud a `AuthServiceImpl`.
3. `AuthServiceImpl` completa primero la logica principal de autenticacion:
   - register: valida unicidad, hashea la contrasena y persiste el usuario
   - login: valida credenciales y genera el JWT
4. Despues de que la accion de autenticacion finaliza con exito, `AuthServiceImpl` llama a `AuthEmailNotificationService`.
5. `AuthEmailNotificationService` construye un `EmailNotificationCommand` con:
   - `userId`
   - correo del destinatario
   - asunto
   - cuerpo
6. `EmailNotificationProviderFactory` resuelve el proveedor activo usando `app.notifications.email.provider`.
7. El proveedor seleccionado, actualmente `FastApiEmailNotificationProvider`, envia una solicitud `multipart/form-data` a la API externa FastAPI.
8. La API FastAPI procesa `/emails/send` y gestiona entrega y persistencia de su lado.
9. Si la llamada externa falla:
   - cuando `app.notifications.email.fail-on-error=false`, el fallo se registra y autenticacion continua
   - cuando `app.notifications.email.fail-on-error=true`, el flujo de autenticacion falla con `502`

## Puntos de Integracion

- `AuthController`
  Recibe solicitudes de registro y login y las delega al servicio de autenticacion.
- `AuthServiceImpl`
  Punto principal de integracion donde se ejecuta el disparo del email tras eventos exitosos de autenticacion.
- `AuthEmailNotificationService`
  Encapsula los casos de uso de notificacion y prepara el payload de email.
- `EmailNotificationProviderFactory`
  Aplica el patron factory para desacoplar el modulo de autenticacion del proveedor concreto de correo.
- `FastApiEmailNotificationProvider`
  Adaptador responsable de llamar al servicio externo FastAPI.
- `EmailNotificationProperties`
  Lee nombre del proveedor, URL base, path del endpoint y comportamiento ante errores desde `application.properties`.
- `application.properties`
  Punto central para cambiar el dominio de la API externa sin tocar el codigo Java.

## Requisitos Funcionales

- `POST /api/auth/register` debe disparar un email al correo registrado despues de crear el usuario.
- `POST /api/auth/login` debe disparar un email al correo del usuario autenticado despues de validar credenciales.
- El proveedor activo debe resolverse con una factory basada en `app.notifications.email.provider`.
- La integracion con FastAPI debe llamar `POST /emails/send` usando `multipart/form-data`.
- El correo destinatario debe coincidir con el email del usuario autenticado o registrado.

## Requisitos de Configuracion

- `app.notifications.email.fastapi.base-url` debe definir el dominio remoto de la API de correo.
- `app.notifications.email.fastapi.send-path` debe definir el path del endpoint.
- `app.notifications.email.fail-on-error` debe controlar si los flujos de autenticacion fallan cuando falla el envio del correo.

## Mapeo de Request hacia FastAPI

El proveedor actual mapea los eventos de autenticacion al endpoint FastAPI asi:

- URL
  `POST {app.notifications.email.fastapi.base-url}{app.notifications.email.fastapi.send-path}`
- Tipo de contenido
  `multipart/form-data`
- Campos del formulario
  - `user_id`: id interno desde `AppUser`
  - `recipient`: email del usuario autenticado o registrado
  - `subject`: asunto segun el evento
  - `body`: mensaje en texto plano generado por Spring Boot

En esta primera integracion no se envian `html_body`, `template_name`, `template_data` ni `pdf_attachment`.

## Criterios de Aceptacion

- Registrar un usuario llama una vez a la API externa con asunto y cuerpo de registro.
- Hacer login llama una vez a la API externa con asunto y cuerpo de login.
- Cambiar el dominio FastAPI en `application.properties` actualiza la integracion sin cambios de codigo.
- Si `fail-on-error=false`, autenticacion continua y el fallo se registra.
- Si `fail-on-error=true`, autenticacion falla con `502` cuando el envio de correo falla.

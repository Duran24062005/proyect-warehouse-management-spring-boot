# PRD de Seguridad y Autenticacion

## Objetivo

Proteger la API con autenticacion stateless y autorizacion basada en roles, manteniendo Swagger util para pruebas manuales.

## Alcance

- Autenticacion Bearer JWT.
- Registro y login publicos.
- Endpoints autenticados de perfil y cambio de contrasena.
- Endpoints de usuarios solo para administradores.
- Proteccion para endpoints de productos, bodegas y movimientos.
- Soporte de Swagger y frontend estatico para pruebas manuales de autenticacion.

## Requisitos Funcionales

- `POST /api/auth/register` debe crear solo cuentas `USER`.
- `POST /api/auth/login` debe validar credenciales y retornar un JWT.
- `GET /api/auth/me` debe retornar el usuario autenticado a partir del token.
- `PATCH /api/auth/change-password` debe requerir un token valido y la contrasena actual, sin recibir `userId` en el payload.
- `GET /api/users` y `GET /api/users/role` deben requerir rol `ADMIN`.
- `/api/products/**`, `/api/warehouses/**` y `/api/movements/**` deben requerir un Bearer token valido.
- Swagger UI y los assets del frontend estatico deben seguir siendo publicos.

## Reglas de Seguridad

- JWT debe ser obligatorio para endpoints protegidos.
- Un token invalido o ausente debe retornar `401`.
- Un rol insuficiente debe retornar `403`.
- Las contrasenas no deben retornarse en respuestas de API.
- El subject del JWT debe mapear al email del usuario autenticado.

## Criterios de Aceptacion

- El login retorna un Bearer token utilizable.
- El registro publico funciona sin permitir elegir rol en el body.
- Los endpoints protegidos rechazan solicitudes anonimas.
- Los endpoints solo admin rechazan usuarios autenticados sin rol admin.
- Swagger UI puede autorizar usando el token retornado.

## Usuario de Prueba

admin@logitrack.com / Admin123!
mlopez@logitrack.com / User123!
jgarcia@logitrack.com / User123!
cperez@logitrack.com / User123!

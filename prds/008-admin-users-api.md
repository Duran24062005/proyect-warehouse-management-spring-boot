# PRD de API Administrativa de Usuarios

## Objetivo

Proveer una API solo para administradores que permita inspeccionar usuarios y crear nuevas cuentas desde la consola administrativa interna.

## Alcance

- Listar todos los usuarios.
- Filtrar usuarios por rol.
- Crear nuevos usuarios con rol explicito y bandera `enabled`.
- Proteccion Bearer JWT con rol `ADMIN`.

## Requisitos Funcionales

- `GET /api/users` debe listar todos los usuarios para administradores autenticados.
- `GET /api/users/role?role=ADMIN` debe filtrar usuarios por rol para administradores autenticados.
- `POST /api/users` debe crear un nuevo usuario para administradores autenticados.
- Los usuarios creados por administracion deben permitir seleccionar rol entre `USER` y `ADMIN`.
- Las contrasenas nunca deben retornarse en la respuesta.

## Requisitos de Datos

- El payload de request debe aceptar:
  - `email`
  - `password`
  - `firstName`
  - `lastName`
  - `phoneNumber`
  - `role`
  - `enabled`
- El payload de response debe exponer solo campos seguros del perfil.

## Criterios de Aceptacion

- Las solicitudes anonimas retornan `401`.
- Las solicitudes autenticadas sin rol admin retornan `403`.
- Los emails duplicados retornan un error visible de validacion o negocio.
- El frontend administrativo puede consumir estos endpoints para crear y listar usuarios.

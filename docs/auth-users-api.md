# Auth and Users API

## Objetivo

Documentar los endpoints de autenticacion y administracion de usuarios que hoy soportan el login del frontend, el control de roles y la consola admin.

## Auth

### `POST /api/auth/register`

- Publico.
- Crea un usuario nuevo con rol `USER`.
- Dispara notificacion por email si la integracion esta habilitada.

Request:

```json
{
  "email": "user@logitrack.com",
  "password": "123456",
  "firstName": "Maria",
  "lastName": "Lopez",
  "phoneNumber": "3000000002"
}
```

### `POST /api/auth/login`

- Publico.
- Valida credenciales y retorna Bearer token.
- Dispara notificacion por email si la integracion esta habilitada.

Request:

```json
{
  "email": "admin@logitrack.com",
  "password": "123456"
}
```

### `GET /api/auth/me`

- Requiere Bearer token.
- Retorna el usuario autenticado.

### `PATCH /api/auth/change-password`

- Requiere Bearer token.
- Cambia la contrasena del usuario autenticado.

Request:

```json
{
  "currentPassword": "123456",
  "newPassword": "654321"
}
```

## Profile Photo

### `PATCH /api/users/me/profile-photo`

- Requiere Bearer token.
- Permite al usuario autenticado subir o reemplazar su foto de perfil.
- Acepta `multipart/form-data` con el campo `file`.
- Tipos permitidos: `image/jpeg`, `image/png`, `image/webp`.
- Tamano maximo: `5 MB`.
- Retorna `UserResponseDTO` actualizado con `profilePhotoUrl`.

## Users

Todos los endpoints de `Users` requieren rol `ADMIN`, excepto `PATCH /api/users/me/profile-photo`, que solo requiere autenticacion.

### `GET /api/users`

Lista todos los usuarios.

### `GET /api/users/role?role=ADMIN`

Filtra usuarios por rol.

### `POST /api/users`

Crea un usuario desde administracion.

```json
{
  "email": "nuevo.admin@logitrack.com",
  "password": "Admin123!",
  "firstName": "Laura",
  "lastName": "Suarez",
  "phoneNumber": "3000000010",
  "role": "ADMIN",
  "enabled": true
}
```

## Integracion con el Frontend

- `index.html` consume `register` y `login`.
- `platform/profile.html` consume `me`, `change-password` y `PATCH /api/users/me/profile-photo`.
- `platform/admin.html` consume `GET /api/users`, `GET /api/users/role` y `POST /api/users`.

## Regla de Roles

- El registro publico no permite elegir rol; siempre crea `USER`.
- La creacion administrativa si permite definir `USER` o `ADMIN`.
- La foto de perfil siempre la carga el propio usuario autenticado.

# PRD de Foto de Perfil de Usuario

## Objetivo

Permitir que el usuario autenticado cargue y reemplace su propia foto de perfil, con almacenamiento en disco, publicacion como recurso estatico y exposicion directa al frontend mediante una URL publica.

## Alcance

- Agregar un endpoint autenticado para subir foto de perfil.
- Guardar una sola foto vigente por usuario.
- Publicar las imagenes bajo una ruta estatica estable.
- Exponer `profilePhotoUrl` en la respuesta de usuario.
- Mostrar y actualizar la foto desde la pantalla de perfil y el sidebar del frontend.

## Requisitos Funcionales

- `PATCH /api/users/me/profile-photo` debe aceptar `multipart/form-data` con el campo `file`.
- El endpoint debe operar sobre el usuario autenticado, sin requerir `userId` en la URL ni en el body.
- La carga debe permitir solo archivos `JPG`, `PNG` y `WEBP`.
- El tamaño maximo permitido debe ser `5 MB`.
- Si el usuario ya tiene una foto previa, la nueva carga debe reemplazarla.
- La aplicacion debe guardar solo el nombre del archivo en base de datos.
- La URL publica de la foto debe armarse en backend y exponerse como `profilePhotoUrl`.
- `GET /api/auth/me` y las respuestas administrativas de usuarios deben incluir `profilePhotoUrl`.
- El frontend debe mostrar placeholder cuando no haya foto y la imagen real cuando exista `profilePhotoUrl`.

## Requisitos Tecnicos

- La ruta fisica de almacenamiento debe ser configurable con `app.storage.profile-photos-dir`.
- El valor por defecto para desarrollo puede ser `./uploads/profile-images`.
- Las imagenes deben servirse mediante `ResourceHandler` en `/uploads/profile-images/**`.
- La solucion no debe depender de escribir en `src/main/resources/static` para funcionar en produccion.
- Debe existir script SQL manual para agregar la columna de foto a `app_user`.

## Requisitos de Datos

- La tabla `app_user` debe incorporar `profile_photo_filename`.
- `UserResponseDTO` debe incluir:
  - `profilePhotoUrl`

## Criterios de Aceptacion

- Un usuario autenticado puede subir una imagen valida y recibir su perfil actualizado.
- Un archivo vacio retorna `400`.
- Un tipo de archivo no permitido retorna `400`.
- Un archivo mayor a `5 MB` retorna `400`.
- Un usuario anonimo recibe `401`.
- La foto cargada se puede abrir por URL publica bajo `/uploads/profile-images/...`.
- El frontend de perfil actualiza la foto sin requerir recarga manual.
- El sidebar muestra la foto del usuario cuando esta disponible.

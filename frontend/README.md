# Frontend

Frontend multi pagina hecho con:

- HTML por pagina
- Tailwind CSS por CDN
- JavaScript vanilla por modulo

## Estructura

- `index.html`
  Login.
- `register.html`
  Registro.
- `platform/system.html`
  Panel general.
- `platform/products.html`
  CRUD de productos y stock bajo.
- `platform/warehouses.html`
  CRUD de bodegas.
- `platform/movements.html`
  CRUD de movimientos con filtros.
- `platform/profile.html`
  Perfil y cambio de contrasena.
- `platform/admin.html`
  Gestion administrativa de usuarios.

## JavaScript

- `js/core/`
  Sesion, API, auth, UI compartida y tema Tailwind.
- `js/pages/`
  Un archivo por pagina para que sea facil de leer y modificar.

## Como probarlo

1. Levanta el backend Spring Boot en `http://localhost:8000`.
2. Sirve esta carpeta con un servidor estatico.
3. Entra a `frontend/index.html`.
4. Si tu backend usa otra URL, cambiala en el bloque "Configuracion rapida".

Ejemplo con Live Server:

- `http://127.0.0.1:5500/frontend/index.html`

## Idea de mantenimiento

Si quieres cambiar una pantalla, normalmente solo necesitas mirar:

- su archivo HTML
- su archivo en `js/pages/`
- y, si hace falta, algun helper corto de `js/core/`

La idea es que no dependas de una SPA ni de un archivo gigante dificil de seguir.

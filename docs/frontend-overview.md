# Frontend Overview

## Objetivo

Describir la estructura del frontend estatico integrado en Spring Boot y como se conecta con la API protegida por JWT.

## Estructura

- `frontend/index.html`
  Login.
- `frontend/register.html`
  Registro.
- `frontend/platform/system.html`
  Dashboard general y analitica visual de movimientos.
- `frontend/platform/reports.html`
  Reportes descargables y analitica visual sincronizada.
- `frontend/platform/products.html`
  Gestion y consulta de productos.
- `frontend/platform/profile.html`
  Perfil del usuario autenticado, foto de perfil y cambio de contrasena.
- `frontend/platform/admin.html`
  Consola administrativa.

## JavaScript

### `js/core`

- `auth.js`
  Maneja sesion, token Bearer y llamadas base autenticadas.
- `charts.js`
  Inicializa y destruye las graficas de Lightweight Charts.
- `ui.js`
  Utilidades compartidas de interfaz.

### `js/pages`

- `auth-page.js`
  Flujo de login y registro.
- `dashboard-page.js`
  Dashboard principal y bloque de analitica.
- `reports-page.js`
  Centro de reportes y analitica visual de movimientos.
- `products-page.js`
  Catalogo y operaciones de productos.
- `profile-page.js`
  Datos del usuario, foto de perfil y cambio de contrasena.
- `admin-page.js`
  Alta de usuarios y flujos administrativos.

## CSS

- `styles/base.css`
  Variables, reset y base visual.
- `styles/auth.css`
  Layout de login y registro.
- `styles/app.css`
  Layout de plataforma, sidebar, cards y formularios internos.

## Integracion con Seguridad

- El cliente guarda el JWT retornado por `POST /api/auth/login`.
- Las vistas privadas consumen la API enviando `Authorization: Bearer <token>`.
- La consola administrativa solo debe exponerse para usuarios con rol `ADMIN`.

## Identidad Visual

- El asset principal es `static/img/appLogo_backgroundless.png`.
- Se utiliza en login, registro y pantallas internas para mantener una identidad consistente.

## Flujo General

1. El usuario entra a `/` o `/register.html`.
2. El frontend consume `register` o `login`.
3. Al iniciar sesion, el token se almacena localmente.
4. Las paginas `platform/*` reutilizan ese token para llamar productos, bodegas, movimientos, reportes, perfil y usuarios.
5. `Dashboard` y `Reportes` consumen analiticas de movimientos y renderizan graficas con Lightweight Charts.
6. La pantalla de perfil tambien consume la subida de foto y reutiliza `profilePhotoUrl` para pintar avatar en perfil y sidebar.

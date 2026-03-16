# Frontend Overview

## Objetivo

Describir la estructura del frontend estatico integrado en Spring Boot y como se conecta con la API protegida por JWT.

## Estructura

- `src/main/resources/static/index.html`
  Login.
- `src/main/resources/static/register.html`
  Registro.
- `src/main/resources/static/platform/system.html`
  Dashboard general.
- `src/main/resources/static/platform/products.html`
  Gestion y consulta de productos.
- `src/main/resources/static/platform/profile.html`
  Perfil del usuario autenticado, foto de perfil y cambio de contrasena.
- `src/main/resources/static/platform/admin.html`
  Consola administrativa.

## JavaScript

### `js/core`

- `auth.js`
  Maneja sesion, token Bearer y llamadas base autenticadas.
- `demo-users.js`
  Soporte para usuarios demo o multiusuario del cliente.
- `ui.js`
  Utilidades compartidas de interfaz.

### `js/pages`

- `auth-page.js`
  Flujo de login y registro.
- `dashboard-page.js`
  Dashboard principal.
- `products-page.js`
  Catalogo y operaciones de productos.
- `profile-page.js`
  Datos del usuario, foto de perfil y cambio de contrasena.
- `admin-page.js`
  Alta de usuarios y flujos administrativos.
- `app-common.js`
  Helpers de pagina reutilizables.

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
4. Las paginas `platform/*` reutilizan ese token para llamar productos, bodegas, movimientos, perfil y usuarios.
5. La pantalla de perfil tambien consume la subida de foto y reutiliza `profilePhotoUrl` para pintar avatar en perfil y sidebar.

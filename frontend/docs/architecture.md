# Arquitectura

## Enfoque general

El frontend sigue una arquitectura multipagina:

- cada pantalla tiene su HTML
- cada pantalla tiene un script dedicado en `js/pages/`
- la logica reutilizable vive en `js/core/`

Esto reduce acoplamiento y hace que los cambios sean locales.

## Estructura de carpetas

```text
frontend/
  index.html
  register.html
  platform/
  js/
    core/
    pages/
  styles/
  img/
  docs/
```

## Responsabilidades por carpeta

- [frontend/js/core](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core)
  Autenticacion, sesion, llamadas HTTP, helpers de UI y tema.
- [frontend/js/pages](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/pages)
  Logica especifica de cada pantalla.
- [frontend/platform](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform)
  HTML de pantallas autenticadas.
- [frontend/styles](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/styles)
  Estilos base, auth y layout general.
- [frontend/img](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/img)
  Assets visuales.

## Patron de una pagina

Cada modulo de `js/pages/` suele seguir este esquema:

1. Busca nodos del DOM al inicio.
2. Valida autenticacion con `requireAuth()` si aplica.
3. Ejecuta `setupLayout()` para pintar sidebar, usuario y acciones comunes.
4. Carga datos iniciales desde la API.
5. Registra listeners para formularios, tablas y botones.
6. Usa `showNotice()` para feedback.

## Estado compartido

El frontend no usa un store global.

El estado compartido se apoya en:

- `localStorage` para token, usuario y base URL
- variables locales por pagina para caches simples como listas cargadas

## Navegacion

Las redirecciones principales se resuelven desde [auth.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/auth.js):

- login publico
- registro publico
- dashboard autenticado
- proteccion de modulos admin

## Sesion

La sesion vive en `localStorage`:

- `logitrack-token`
- `logitrack-user`
- `logitrack-api-base`

Si una solicitud autenticada recibe `401`, el frontend limpia sesion automaticamente.

## Estilo visual

La base visual combina:

- Tailwind por CDN
- clases utilitarias escritas en HTML
- componentes CSS reutilizables en [app.css](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/styles/app.css)
- fondo y tokens base en [base.css](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/styles/base.css)

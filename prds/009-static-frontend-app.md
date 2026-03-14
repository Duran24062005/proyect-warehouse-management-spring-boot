# PRD de Frontend Multipagina

## Objetivo

Proveer un frontend ligero en HTML, Tailwind y JavaScript vanilla para consumir la API protegida del sistema de bodegas sin usar una SPA.

## Alcance

- Pantallas publicas de login y registro.
- Vistas autenticadas de dashboard, productos, bodegas, movimientos, perfil y administracion.
- Modulos JavaScript compartidos para sesion, autenticacion, llamadas a API y utilidades visuales.
- Estilos compartidos para identidad visual, layout y componentes base.
- Organizacion del frontend por paginas y archivos pequenos para facilitar lectura y mantenimiento.

## Requisitos Funcionales

- `frontend/index.html` debe exponer la pantalla de login.
- `frontend/register.html` debe exponer la pantalla de registro.
- `frontend/platform/system.html` debe exponer el dashboard autenticado.
- `frontend/platform/products.html` debe exponer gestion de productos y vistas de stock bajo.
- `frontend/platform/warehouses.html` debe exponer gestion de bodegas.
- `frontend/platform/movements.html` debe exponer gestion y filtrado de movimientos.
- `frontend/platform/profile.html` debe exponer datos del usuario actual y cambio de contrasena.
- `frontend/platform/admin.html` debe exponer operaciones solo admin como creacion de usuarios y cambio de estados.
- El frontend debe almacenar y reutilizar el JWT retornado por login.
- Las llamadas protegidas a la API deben enviar `Authorization: Bearer <token>`.
- El frontend debe poder redirigir a login si el token no existe o deja de ser valido.
- El frontend debe ocultar o restringir la pantalla administrativa para usuarios que no sean `ADMIN`.

## Requisitos de UX

- La aplicacion debe usar el logo sin fondo del proyecto para reforzar identidad visual.
- Las pantallas de autenticacion y de plataforma deben compartir un lenguaje visual consistente.
- La interfaz debe mantener un flujo claro y facil de seguir para un desarrollador junior.
- El codigo debe evitar archivos gigantes o arquitectura innecesariamente compleja.
- La tipografia debe verse moderna y legible.
- La estructura debe separar codigo compartido del codigo especifico por pagina para facilitar mantenimiento.

## Estructura Esperada

- `frontend/js/core/`
  Helpers compartidos de sesion, autenticacion, consumo de API, UI y tema Tailwind.
- `frontend/js/pages/`
  Un archivo JavaScript por pagina.
- `frontend/styles/`
  Estilos base, auth y aplicacion.
- `frontend/platform/`
  Vistas privadas.

## Criterios de Aceptacion

- Un usuario puede registrarse, iniciar sesion y navegar a la plataforma interna.
- Un usuario autenticado puede consumir endpoints protegidos de productos, bodegas, movimientos y perfil.
- Un usuario admin puede acceder a flujos administrativos respaldados por `/api/users`.
- El frontend no depende de enrutamiento SPA ni de un archivo unico de logica.
- El codigo organiza la logica frontend en `frontend/js/core`, `frontend/js/pages` y estilos compartidos.
- El frontend incluye el logo sin fondo en las vistas principales.

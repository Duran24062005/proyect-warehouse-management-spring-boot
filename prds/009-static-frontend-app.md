# PRD de Aplicacion Frontend Estatica

## Objetivo

Proveer un frontend estatico profesional servido por Spring Boot para consumir la API protegida del sistema de bodegas.

## Alcance

- Pantallas publicas de login y registro.
- Vistas autenticadas de dashboard, productos, perfil y administracion.
- Modulos JavaScript compartidos para sesion y acceso a API.
- CSS compartido para identidad visual y layout.

## Requisitos Funcionales

- `/` debe exponer la pantalla de login.
- `/register.html` debe exponer la pantalla de registro.
- `/platform/system.html` debe exponer el dashboard autenticado.
- `/platform/products.html` debe exponer gestion de productos y vistas de stock bajo.
- `/platform/profile.html` debe exponer datos del usuario actual y cambio de contrasena.
- `/platform/admin.html` debe exponer operaciones solo admin como creacion de usuarios y asignacion de managers.
- El frontend debe almacenar y reutilizar el JWT retornado por login.
- Las llamadas protegidas a la API deben enviar `Authorization: Bearer <token>`.

## Requisitos de UX

- La aplicacion debe usar el logo del proyecto para reforzar identidad visual.
- Las pantallas de autenticacion y de plataforma deben compartir un lenguaje visual consistente.
- La estructura debe separar codigo compartido del codigo especifico por pagina para facilitar mantenimiento.

## Criterios de Aceptacion

- Un usuario puede registrarse, iniciar sesion y navegar a la plataforma interna.
- Un usuario autenticado puede consumir endpoints protegidos de productos, bodegas y movimientos.
- Un usuario admin puede acceder a flujos administrativos respaldados por `/api/users` y la asignacion de managers.
- El codigo organiza la logica frontend en `js/core`, `js/pages` y estilos compartidos.

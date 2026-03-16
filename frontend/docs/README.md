# Frontend Docs

Documentacion tecnica del frontend estatico de LogiTrack.

## Objetivo

Este frontend consume la API del backend Spring Boot mediante paginas HTML independientes y modulos de JavaScript vanilla.

El enfoque del proyecto es:

- mantener una estructura simple de entender
- evitar una SPA compleja para una entrega academica o administrativa
- separar cada pantalla en su propio archivo HTML y su propio script

## Mapa rapido

- [architecture.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/docs/architecture.md)
  Estructura general, carpetas y decisiones de arquitectura.
- [modules.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/docs/modules.md)
  Modulos compartidos de `js/core/` y utilidades globales.
- [pages.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/docs/pages.md)
  Pantallas disponibles, comportamiento y endpoints usados.
- [maintenance.md](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/docs/maintenance.md)
  Flujo de trabajo, convenciones y puntos a revisar antes de cambiar algo.

## Stack

- HTML por pagina
- Tailwind CSS via CDN
- CSS propio en `styles/`
- JavaScript vanilla con ES modules
- `fetch` para consumo de API
- `localStorage` para sesion y configuracion

## Punto de entrada

- Login: [index.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/index.html)
- Registro: [register.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/register.html)
- Modulos autenticados: carpeta [platform](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/platform)

## Nota importante

El archivo [app.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/app.js) ya no forma parte del flujo principal. La version vigente del frontend esta en `frontend/js/pages/`.

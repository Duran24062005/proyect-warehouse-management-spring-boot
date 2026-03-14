# PRD de Control de Acceso por Bodega

## Objetivo

Restringir el acceso a datos de negocio relacionados con bodegas desde la capa de servicios, para que los usuarios no admin solo puedan operar sobre las bodegas que gestionan, mientras los administradores conservan acceso total.

## Alcance

- Reglas de visibilidad y mutacion para bodegas.
- Reglas de visibilidad y mutacion para productos.
- Reglas de visibilidad y mutacion para movimientos.
- Aplicacion de reglas en capa de servicios usando el contexto de autenticacion actual.
- Reutilizacion de la relacion existente `warehouse.manager` como ancla de acceso.

## Contexto de Negocio

El sistema necesita control de acceso por bodega, pero el modelo actual no incluye una tabla explicita de asignacion empleado -> bodega.

La relacion que ya existe y que puede aplicarse de forma segura es:

- una bodega puede tener un manager mediante `manager_user_id`

Por eso, la primera implementacion segura del control por bodega debe usar el modelo actual de asignacion de manager:

- los usuarios `ADMIN` tienen acceso global
- los usuarios no admin solo tienen acceso a las bodegas donde son managers asignados

## Requisitos Funcionales

- Los usuarios `ADMIN` deben conservar acceso total a bodegas, productos y movimientos.
- Los usuarios no admin solo deben ver bodegas donde son managers asignados.
- Los usuarios no admin solo deben ver productos asignados a bodegas que gestionan.
- Los usuarios no admin solo deben ver movimientos vinculados a bodegas que gestionan.
- Los usuarios no admin solo deben crear o actualizar productos dentro de bodegas que gestionan.
- Los usuarios no admin solo deben crear o actualizar movimientos cuyas bodegas origen y destino esten dentro de su alcance.
- La creacion, actualizacion y eliminacion de bodegas debe quedar restringida a admins.

## Reglas por Servicio

### Bodegas

- `findAll`:
  - los admins ven todas las bodegas
  - los usuarios no admin ven solo sus bodegas gestionadas
- `findOne`:
  - los admins pueden acceder a cualquier bodega
  - los usuarios no admin solo a las que gestionan
- `save`, `update`, `delete`:
  - solo admin

### Productos

- `findAll`:
  - los admins ven todos los productos
  - los usuarios no admin ven solo productos en sus bodegas gestionadas
- `findOne`:
  - los admins pueden acceder a cualquier producto
  - los usuarios no admin solo a productos dentro de su alcance
- `save`, `update`, `delete`:
  - los admins operan libremente
  - los usuarios no admin solo si la bodega del producto pertenece a su alcance
- `low-stock`:
  - los admins reciben el resultado completo
  - los usuarios no admin solo reciben productos con stock bajo dentro de sus bodegas

### Movimientos

- `findAll`:
  - los admins ven todos los movimientos
  - los usuarios no admin solo movimientos cuya bodega origen o destino pertenece a su alcance
- `findOne`:
  - los admins pueden acceder a cualquier movimiento
  - los usuarios no admin solo a movimientos dentro de su alcance
- `findByWarehouse`:
  - los usuarios no admin solo pueden consultar bodegas que gestionan
- `save`, `update`, `delete`:
  - los admins operan libremente
  - los usuarios no admin solo cuando las bodegas relacionadas estan dentro de su alcance

## Restriccion Tecnica

Esta implementacion no debe rediseñar la arquitectura actual ni introducir nuevos frameworks.

El control de acceso debe aplicarse:

- en la capa de servicios
- usando el usuario autenticado desde el contexto de seguridad actual
- usando la relacion existente `warehouse.manager`

## Suposicion

Como el modelo actual todavia no mapea empleados directamente a una bodega, esta iteracion trata el acceso por bodega para usuarios no admin a traves de la relacion de manager.

Esto significa:

- los managers quedan correctamente restringidos
- los usuarios no admin sin bodegas gestionadas no tendran acceso de negocio por bodega en estos modulos

## Fuera de Alcance

- Rediseño de asignacion empleado -> bodega.
- Rediseño de roles.
- Tablas de membresia por bodega.
- Rediseño del filter chain de seguridad.
- Rediseño del frontend.

## Criterios de Aceptacion

- Los usuarios admin siguen accediendo a todas las bodegas, productos y movimientos.
- Los usuarios no admin solo reciben datos de las bodegas que gestionan.
- Los usuarios no admin no pueden modificar bodegas directamente.
- Los usuarios no admin no pueden crear o mover datos hacia bodegas fuera de su alcance.
- Los intentos no autorizados retornan un error prohibido visible para el cliente.
- La implementacion encaja en la arquitectura por capas actual y reutiliza entidades y repositorios existentes.

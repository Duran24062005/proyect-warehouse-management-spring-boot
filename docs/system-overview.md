# Warehouse Management System - System Overview

## Descripcion General

Este proyecto implementa un sistema backend para la gestion de bodegas y el registro de movimientos entre ellas.

La empresa **LogiTrack S.A.** administra multiples bodegas distribuidas en distintas ciudades y necesita centralizar la informacion operativa relacionada con:

- bodegas
- productos como catalogo
- movimientos de entrada, salida y transferencia
- usuarios con acceso controlado
- auditoria de cambios

El sistema no esta planteado como un POS ni como un modulo de ventas. Su foco principal es la **gestion administrativa y trazable de movimientos entre bodegas**, con autenticacion y control de acceso por rol y por alcance operativo.

---

# Modelo Organizacional del Sistema

El sistema contempla **tres tipos de personas en la operacion**, pero **solo dos usan directamente la plataforma**.

## Administrador General

El **Administrador General** es el usuario con vista global del sistema y rol `ADMIN`.

Entre sus responsabilidades se encuentran:

- crear, actualizar y eliminar bodegas
- asignar managers a una bodega
- crear usuarios dentro del sistema
- aprobar, bloquear o desbloquear usuarios
- consultar todos los productos, movimientos y usuarios
- visualizar toda la informacion del sistema sin restricciones por bodega

## Manager de Bodega

El **Manager de Bodega** es el usuario operativo de plataforma con rol `USER`.

Entre sus funciones se encuentran:

- registrar movimientos de entrada, salida y transferencia
- consultar productos visibles dentro de su alcance
- consultar movimientos relacionados con las bodegas que gestiona
- consultar bodegas y datos operativos del sistema segun su alcance

El manager es quien **registra formalmente los movimientos en la plataforma**.

## Empleado

El **Empleado** existe dentro del modelo organizacional del negocio, pero no como usuario directo de la plataforma en la version actual del sistema.

Es decir:

- puede participar en la operacion fisica de una bodega
- sus acciones son supervisadas por el manager
- el movimiento oficial se registra en el sistema por parte del manager o del administrador

---

# Registro y Aprobacion de Usuarios

El sistema permite que nuevos usuarios se registren mediante el flujo de creacion de cuenta.

Sin embargo, el registro de un usuario **no implica acceso inmediato al sistema**.

Cuando un usuario se registra:

1. su cuenta queda en estado `PENDING`
2. un administrador debe revisar la solicitud
3. el administrador puede aprobar, bloquear o mantener pendiente la cuenta

Mientras un usuario se encuentre en estado `PENDING` o `BLOCKED`, no podra ingresar al sistema.

Ademas del registro publico, los administradores pueden crear usuarios directamente desde la plataforma.

---

# Gestion de Bodegas

El sistema permite administrar bodegas con la siguiente informacion:

- identificador
- nombre
- ubicacion
- capacidad
- manager asignado

Las bodegas representan los espacios fisicos donde se registran los movimientos operativos.

---

# Gestion de Productos

El sistema permite realizar operaciones CRUD sobre los productos registrados.

Cada producto contiene informacion como:

- identificador
- nombre
- categoria
- precio
- bodega asociada

Dentro del alcance actual, cada producto representa un **activo individual**.

Eso implica que:

- cada producto solo puede estar en una bodega a la vez;
- `warehouseId` representa la ubicacion actual del activo;
- la reubicacion operativa debe registrarse mediante movimientos;
- el modulo de productos no maneja stock agregado ni cantidades por lote.

---

# Movimientos entre Bodegas

El sistema permite registrar tres tipos de movimientos:

- **ENTRY**
- **EXIT**
- **TRANSFER**

Cada movimiento almacena informacion como:

- fecha de registro
- tipo de movimiento
- usuario que registra el movimiento
- bodega origen
- bodega destino
- producto

Los movimientos constituyen el eje central del sistema porque dejan trazabilidad sobre el recorrido de cada activo individual.

Regla clave del dominio:

- un producto individual puede quedar sin bodega al salir;
- puede entrar desde fuera a una bodega;
- o puede transferirse entre dos bodegas;
- pero nunca puede estar en dos bodegas al mismo tiempo.

---

# Auditoria de Cambios

El sistema incluye un mecanismo de auditoria que registra las acciones realizadas sobre las entidades principales.

Cada registro de auditoria guarda informacion como:

- tipo de operacion realizada
- fecha y hora de la accion
- usuario responsable
- entidad afectada
- valores anteriores
- valores nuevos

Esto permite mantener historial de cambios sobre usuarios, productos, bodegas y movimientos.

---

# Sistema de Notificaciones

El sistema incluye notificaciones por correo electronico para eventos clave relacionados con la autenticacion.

Actualmente se contemplan eventos como:

- registro de usuario
- creacion administrativa de cuentas
- inicio de sesion

Estas notificaciones ayudan a mantener informado al usuario sobre el estado y uso de su cuenta.

---

# Consultas del Sistema

El sistema permite realizar consultas operativas sobre la informacion almacenada, especialmente relacionadas con:

- productos registrados
- bodegas
- movimientos
- usuarios
- auditoria

El enfoque actual esta en la consulta administrativa y la trazabilidad de movimientos, no en reportes avanzados de inventario o ventas.

---

# Documentacion de la API

El sistema cuenta con documentacion interactiva de la API para explorar y probar los endpoints disponibles.

---

# Estructura del Proyecto

El proyecto esta organizado siguiendo una arquitectura por capas:

`Controller -> Service -> Repository`

Esta organizacion permite separar responsabilidades, mantener el codigo legible y facilitar su evolucion.

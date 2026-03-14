# Warehouse Management System - System Overview

## Descripción General

Este proyecto implementa un sistema backend para la gestión de bodegas e inventarios utilizando **Spring Boot**.

La empresa **LogiTrack S.A.** administra múltiples bodegas distribuidas en distintas ciudades, encargadas de almacenar productos y registrar movimientos de inventario como entradas, salidas y transferencias.

Anteriormente, el control de inventarios y auditorías se realizaba manualmente en hojas de cálculo, lo que generaba problemas de trazabilidad, control de accesos y consistencia de la información.

El objetivo del sistema es centralizar la gestión de inventarios y permitir el registro controlado de movimientos entre bodegas, garantizando seguridad, trazabilidad y acceso controlado mediante autenticación basada en **JWT**.

---

# Modelo Organizacional del Sistema

El sistema contempla **dos tipos de usuarios autenticados**, pero **tres tipos de personas dentro del modelo organizacional**.

## Administrador General

El **Administrador General** es el responsable de la gestión completa del sistema.

Entre sus responsabilidades se encuentran:

* Registrar, actualizar y eliminar bodegas.
* Asignar o remover líderes (managers) de una bodega.
* Asignar empleados a bodegas o cambiarlos entre ellas.
* Crear nuevos usuarios dentro del sistema y asignarles un rol.
* Consultar todos los productos registrados en el sistema.
* Consultar todos los usuarios registrados.
* Visualizar toda la información del sistema con filtros.

El administrador tiene una **vista global del sistema** y no está limitado a una bodega específica.

---

## Manager de Bodega

El **Manager de Bodega** es el responsable de administrar las operaciones dentro de una bodega específica.

Entre sus funciones se encuentran:

* Registrar movimientos de inventario.
* Supervisar las acciones realizadas por los empleados de su bodega.
* Consultar los productos disponibles en su bodega.
* Visualizar las bodegas disponibles en el sistema.
* Consultar los usuarios asignados a su bodega.

El manager es responsable de **registrar formalmente los movimientos realizados dentro de la bodega**, incluso si la acción fue realizada por un empleado.

Por ejemplo:

Si un empleado mueve un producto de una bodega a otra, el manager debe registrar ese movimiento dentro del sistema.

---

## Empleados

Los empleados son los usuarios operativos dentro de una bodega.

Sus acciones son supervisadas por el **manager de la bodega**, quien se encarga de registrar formalmente los movimientos de inventario en el sistema.

Los empleados están asociados a una bodega específica y forman parte del equipo gestionado por el manager.

---

# Registro y Aprobación de Usuarios

El sistema permite que nuevos usuarios se registren utilizando el endpoint:

```
/auth/register
```

Sin embargo, el registro de un usuario **no implica acceso inmediato al sistema**.

Cuando un usuario se registra:

1. Su cuenta queda en estado **pendiente**.
2. Un **Administrador General** debe revisar la solicitud.
3. El administrador puede:

   * **Aceptar** al usuario y asignarle un rol dentro del sistema.
   * **Bloquear o rechazar** la solicitud.

Mientras un usuario se encuentre en estado **pendiente o bloqueado**, no podrá acceder a los endpoints protegidos del sistema.

Esto permite evitar que usuarios no autorizados tengan acceso a la información del sistema inmediatamente después de registrarse.

Además del registro automático, los **Administradores Generales también pueden crear usuarios manualmente desde el sistema y asignarles directamente un rol**.

---

# Gestión de Bodegas

El sistema permite administrar bodegas con la siguiente información:

* id
* nombre
* ubicación
* capacidad
* encargado

Las bodegas representan los centros físicos donde se almacenan los productos.

---

# Gestión de Productos

El sistema permite realizar operaciones CRUD sobre los productos.

Cada producto contiene:

* id
* nombre
* categoría
* stock
* precio

Los productos representan los artículos que se almacenan y se mueven entre bodegas.

---

# Movimientos de Inventario

El sistema permite registrar tres tipos de movimientos de inventario:

* ENTRADA
* SALIDA
* TRANSFERENCIA

Cada movimiento almacena:

* fecha
* tipo de movimiento
* usuario responsable
* bodega origen
* bodega destino
* productos involucrados
* cantidades

Estos movimientos permiten llevar un registro claro de los cambios en el inventario.

---

# Auditoría de Cambios

El sistema implementa un mecanismo de auditoría automática que registra los cambios realizados sobre las entidades del sistema.

Cada registro de auditoría almacena:

* tipo de operación (INSERT, UPDATE, DELETE)
* fecha y hora
* usuario que realizó la acción
* entidad afectada
* valores anteriores
* valores nuevos

Esto permite mantener trazabilidad completa de las operaciones realizadas dentro del sistema.

---

# Seguridad

El sistema utiliza **Spring Security con autenticación basada en JWT**.

Los endpoints disponibles incluyen:

* `/auth/login`
* `/auth/register`

Las rutas relacionadas con:

* bodegas
* productos
* movimientos

se encuentran protegidas y requieren un token válido para su acceso.

El sistema utiliza **roles de usuario** para controlar el acceso a los recursos y garantizar que cada tipo de usuario solo pueda interactuar con la información que le corresponde.

---

# Reportes y Consultas

El sistema permite realizar consultas avanzadas como:

* Productos con bajo stock.
* Movimientos en un rango de fechas.
* Auditorías filtradas por usuario.
* Auditorías filtradas por tipo de operación.

También se genera un reporte general que muestra:

* stock total por bodega
* productos más movidos.

---

# Documentación de la API

La API REST se encuentra documentada utilizando **Swagger / OpenAPI 3**, lo que permite explorar y probar los endpoints de manera interactiva.

---

# Estructura del Proyecto

El proyecto sigue una arquitectura basada en capas:

```
src/
 ├─ controller/
 ├─ service/
 ├─ repository/
 ├─ model/
 ├─ config/
 ├─ security/
 └─ exception/
```

Cada capa tiene responsabilidades claramente separadas para mantener la organización y mantenibilidad del sistema.

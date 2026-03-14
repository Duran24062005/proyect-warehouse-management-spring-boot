# Warehouse Management System - System Overview

## Descripción General

Este proyecto implementa un sistema backend para la gestión de bodegas e inventarios.

La empresa **LogiTrack S.A.** administra múltiples bodegas distribuidas en distintas ciudades, encargadas de almacenar productos y registrar movimientos de inventario como entradas, salidas y transferencias.

Anteriormente, el control de inventarios y auditorías se realizaba manualmente en hojas de cálculo, lo que generaba problemas de trazabilidad, control de accesos y consistencia de la información.

El objetivo del sistema es centralizar la gestión de inventarios y permitir el registro controlado de movimientos entre bodegas, garantizando seguridad, trazabilidad y acceso controlado mediante autenticación.

---

# Modelo Organizacional del Sistema

El sistema contempla **dos tipos de usuarios dentro de la autenticación**, pero **tres tipos de personas dentro del modelo organizacional del sistema**.

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
* Visualizar las bodegas disponibles dentro del sistema.
* Consultar los usuarios asignados a su bodega.

El manager es responsable de **registrar formalmente los movimientos realizados dentro de la bodega**, incluso si la acción fue realizada por un empleado.

Por ejemplo, si un empleado mueve un producto de una bodega a otra, el manager debe registrar ese movimiento dentro del sistema.

---

## Empleados

Los empleados son los usuarios operativos dentro de una bodega.

Sus acciones son supervisadas por el **manager de la bodega**, quien se encarga de registrar formalmente los movimientos de inventario en el sistema.

Los empleados están asociados a una bodega específica y forman parte del equipo gestionado por el manager.

---

# Registro y Aprobación de Usuarios

El sistema permite que nuevos usuarios se registren mediante el proceso de registro de cuenta.

Sin embargo, el registro de un usuario **no implica acceso inmediato al sistema**.

Cuando un usuario se registra:

1. Su cuenta queda en estado **pendiente**.
2. Un **Administrador General** debe revisar la solicitud.
3. El administrador puede:

   * **Aceptar** al usuario y activarlo dentro del sistema.
   * **Bloquear o rechazar** la solicitud.

Mientras un usuario se encuentre en estado **pendiente o bloqueado**, no podrá acceder a la información del sistema.

Esto permite evitar que usuarios no autorizados tengan acceso inmediato a los datos del sistema.

Además del registro automático, los **Administradores Generales también pueden crear usuarios directamente dentro del sistema y asignarles un rol desde el inicio**.

---

# Gestión de Bodegas

El sistema permite administrar bodegas con la siguiente información:

* identificador
* nombre
* ubicación
* capacidad
* encargado

Las bodegas representan los centros físicos donde se almacenan los productos.

---

# Gestión de Productos

El sistema permite realizar operaciones completas de gestión sobre los productos registrados.

Cada producto contiene información como:

* identificador
* nombre
* categoría
* stock
* precio

Los productos representan los artículos que se almacenan y se mueven entre bodegas.

---

# Movimientos de Inventario

El sistema permite registrar tres tipos de movimientos de inventario:

* **Entrada**
* **Salida**
* **Transferencia**

Cada movimiento almacena información como:

* fecha del movimiento
* tipo de movimiento
* usuario responsable
* bodega origen
* bodega destino
* productos involucrados
* cantidades

Estos movimientos permiten llevar un registro claro de los cambios en el inventario y facilitan la trazabilidad de las operaciones realizadas.

---

# Auditoría de Cambios

El sistema incluye un mecanismo de auditoría que registra las acciones realizadas sobre las entidades principales.

Cada registro de auditoría guarda información como:

* tipo de operación realizada
* fecha y hora de la acción
* usuario responsable
* entidad afectada
* valores anteriores
* valores nuevos

Esto permite mantener un historial completo de las modificaciones realizadas dentro del sistema.

---

# Sistema de Notificaciones

El sistema incluye un mecanismo de **notificaciones por correo electrónico** para informar a los usuarios sobre eventos importantes relacionados con su cuenta.

Actualmente se envían notificaciones en tres situaciones principales.

## Registro de Usuario

Cuando un usuario se registra en el sistema, recibe un correo electrónico informando que su registro fue realizado correctamente y que su cuenta se encuentra pendiente de aprobación por parte de un administrador.

## Registro por Administrador

Cuando un administrador crea un usuario directamente dentro del sistema, el usuario recibe un correo indicando que su cuenta ha sido creada y que ya se encuentra activa.

## Inicio de Sesión

Cada vez que un usuario inicia sesión en el sistema, se envía una notificación a su correo electrónico indicando que se ha realizado un inicio de sesión en su cuenta.

Estas notificaciones permiten mantener informados a los usuarios sobre el estado de su cuenta y sobre el uso de la misma.

---

# Reportes y Consultas

El sistema permite realizar consultas y reportes sobre la información almacenada.

Entre ellos se incluyen:

* consulta de productos con bajo stock
* consulta de movimientos en un rango de fechas
* auditorías filtradas por usuario
* auditorías filtradas por tipo de operación

También se genera un reporte general que muestra:

* el stock total por bodega
* los productos con mayor movimiento dentro del sistema

---

# Documentación de la API

El sistema cuenta con documentación interactiva de la API que permite explorar y probar los endpoints disponibles.

Esto facilita la comprensión y el uso de los servicios expuestos por el backend.

---

# Estructura del Proyecto

El proyecto está organizado siguiendo una arquitectura por capas que separa responsabilidades entre diferentes módulos del sistema.

Esta organización permite mantener el código estructurado, facilitar su mantenimiento y mejorar la escalabilidad del proyecto.


# Estado Actual del Backend

## 1. Arquitectura Actual

El estado actual del workspace muestra una aplicacion monolitica en Spring Boot organizada por capas. No es un esqueleto incompleto: ya existe una base funcional con controladores, servicios, repositorios, mapeadores, seguridad, notificaciones, configuracion y manejo global de errores.

La arquitectura sigue el flujo:

`Controller -> Service -> Repository -> Base de datos`

Adicionalmente:

- `mapper` se encarga de convertir entidades a DTOs y viceversa.
- `security` centraliza JWT, filtro de autenticacion y configuracion de acceso.
- `notifications` desacopla el envio de correos mediante un proveedor configurable.
- `exceptions` unifica las respuestas de error de la API.
- `config` contiene Swagger/OpenAPI y configuraciones transversales.

En la practica, el sistema esta compuesto por:

- Backend REST con Spring Boot.
- Persistencia con Spring Data JPA + MySQL.
- Seguridad con Spring Security + JWT.
- Documentacion Swagger/OpenAPI.
- Frontend estatico integrado dentro del mismo proyecto.

## 2. Funcionalidades Implementadas

### Autenticacion y seguridad

Ya existen endpoints y logica para:

- Registro de usuarios.
- Inicio de sesion.
- Consulta del usuario autenticado.
- Cambio de contrasena.
- Validacion para que solo usuarios `ACTIVE` puedan ingresar.

La API usa JWT de forma stateless y protege las rutas privadas. Solo `login`, `register`, Swagger y los archivos estaticos estan expuestos publicamente.

Ademas:

- el registro publico crea cuentas `PENDING`
- los usuarios `PENDING` no pueden iniciar sesion
- los usuarios `BLOCKED` no pueden iniciar sesion
- el filtro JWT solo autentica usuarios `ACTIVE`

### Gestion de usuarios

Ya existe un modulo administrativo para:

- Listar usuarios.
- Filtrar usuarios por rol.
- Filtrar usuarios por estado.
- Crear usuarios desde administracion.
- Actualizar estado de usuario.
- Aprobar usuarios.
- Bloquear usuarios.
- Desbloquear usuarios.

Ademas, el modelo `AppUser` ya incluye:

- `role`
- `enabled`
- `userStatus`

Tambien ya existe el enum de estado de usuario:

- `PENDING`
- `ACTIVE`
- `BLOCKED`

### Gestion de productos

Ya esta implementado:

- CRUD completo de productos.
- Consulta de productos por id.
- Consulta de productos con stock bajo.
- Restriccion por bodega para usuarios no admin segun las bodegas que gestionan.

Tambien existe una consulta custom en repositorio para calcular stock bajo a partir de movimientos.

### Gestion de bodegas

Ya esta implementado:

- CRUD completo de bodegas.
- Asignacion de manager a una bodega mediante relacion con `AppUser`.
- Visibilidad por bodega segun el usuario autenticado.
- Restriccion de escritura administrativa sobre bodegas.

### Gestion de movimientos

Ya existe:

- CRUD completo de movimientos.
- Consulta de movimientos por producto.
- Consulta de movimientos por bodega.
- Restriccion por bodega en consultas y mutaciones para usuarios no admin.

Tambien hay validaciones importantes segun el tipo de movimiento:

- `ENTRY`
- `EXIT`
- `TRANSFER`

Por ejemplo, ya se valida que:

- una entrada tenga bodega destino y no tenga origen
- una salida tenga bodega origen y no tenga destino
- una transferencia tenga origen y destino
- una transferencia no use la misma bodega como origen y destino

### Auditoria

El modelo de auditoria ya existe a nivel de entidad, base de datos y flujo de negocio:

- `AuditChange`
- `TrackedEntity`
- enum `OperationType`
- `AuditService`
- repositorios para auditoria y entidades trazables

Tambien existe integracion automatica de auditoria para:

- usuarios
- productos
- bodegas
- movimientos

Se registran operaciones `INSERT`, `UPDATE` y `DELETE` con actor, entidad afectada y snapshots JSON de valores anteriores y nuevos.

### Notificaciones

Ya existe un modulo desacoplado de notificaciones por correo.

Actualmente se implementa:

- correo al registrarse
- correo al iniciar sesion

El sistema no envia directamente desde autenticacion hacia una API externa de forma acoplada, sino que usa:

- `AuthEmailNotificationService`
- `EmailNotificationProviderFactory`
- `EmailNotificationProvider`
- `FastApiEmailNotificationProvider`

Eso permite cambiar el proveedor sin reescribir el flujo de autenticacion.

### Documentacion y manejo de errores

Ya esta implementado:

- Swagger/OpenAPI
- manejo global de errores con `@RestControllerAdvice`
- respuestas JSON para errores de validacion, requests invalidos y errores inesperados

### Frontend integrado

El proyecto ya incluye frontend estatico dentro de `src/main/resources/static`, con pantallas para:

- login
- registro
- dashboard
- productos
- perfil
- administracion

## 3. Funcionalidades Faltantes

Aunque hay bastante trabajo adelantado, todavia faltan piezas importantes para considerar el sistema como completo.

### Flujo real de aprobacion de usuarios

El flujo principal ya quedo integrado:

- registro publico como `PENDING`
- aprobacion administrativa
- bloqueo y desbloqueo
- acceso solo para usuarios `ACTIVE`

Lo que todavia falta es una politica mas completa de rechazo formal o historial de aprobacion mas detallado si se quiere enriquecer el proceso.

### Modelo organizacional completo

Segun la descripcion funcional del sistema, existen tres tipos de personas:

- Administrador General
- Warehouse Manager
- Empleado

Sin embargo, en el codigo actualmente solo existen dos roles:

- `ADMIN`
- `USER`

Eso significa que el modelo organizacional aun no esta representado completamente a nivel de permisos y reglas de acceso.

### Control de acceso por bodega

Ya existe control de acceso por bodega dentro de servicios y reglas de negocio.

Actualmente:

- `ADMIN` mantiene acceso total
- los usuarios no admin solo acceden a las bodegas que gestionan como manager
- productos y movimientos quedan limitados por ese mismo alcance

La limitacion actual es que todavia no existe una relacion explicita empleado -> bodega, por lo que el control por bodega se apoya en la asignacion de manager.

### Auditoria automatica real

La auditoria automatica ya fue integrada sobre los flujos mutativos principales.

Lo que aun falta si se quiere completar el modulo es:

- endpoints o consultas para auditar por usuario, operacion o entidad
- reportes de auditoria orientados a uso funcional

### Reportes avanzados

Todavia no estan implementados completamente los reportes solicitados, como:

- movimientos por rango de fechas
- auditorias por usuario
- auditorias por tipo de operacion
- stock total por bodega
- productos mas movidos

### Reglas de inventario mas completas

El sistema valida estructura de movimientos, pero aun faltan reglas de negocio mas fuertes, por ejemplo:

- evitar salidas sin stock suficiente
- evitar transferencias sin stock disponible en la bodega origen
- validar si el usuario que registra el movimiento pertenece realmente a la bodega

### Notificacion para usuarios creados por administrador

En la descripcion funcional se espera enviar correo cuando un administrador crea una cuenta activa.

Actualmente solo esta implementado:

- correo por registro
- correo por login

Falta el correo especifico para cuentas creadas por administracion.

### Pruebas automatizadas reales

Actualmente casi no hay cobertura de pruebas.

Solo existe un test de carga de contexto y, ademas, las pruebas fallan si no hay conexion real con MySQL.

Falta implementar:

- perfil de pruebas
- base de datos de pruebas o configuracion aislada
- tests de servicios
- tests de controladores
- pruebas de seguridad

## 4. Siguientes Pasos Sugeridos

Para avanzar sin reescribir la arquitectura, el orden mas conveniente seria:

1. Construir los reportes pendientes sobre la base actual.
2. Exponer consultas de auditoria por usuario, operacion y entidad.
3. Modelar la asignacion explicita de empleados a bodegas para completar el control organizacional.
4. Agregar notificacion para usuarios creados por administrador.
5. Reforzar reglas de inventario como validacion de stock suficiente en salidas y transferencias.
6. Agregar un perfil de pruebas estable y pruebas automatizadas por modulo.

## 5. Mejoras Seguras que Encajan con la Arquitectura Existente

Estas mejoras encajan bien con la base actual y no requieren rediseñar el sistema:

- Extender el modulo de usuarios actual para manejar cambios de estado en vez de crear un subsistema nuevo.
- Mantener JWT, pero reforzar autorizacion dentro de servicios usando el usuario autenticado y su relacion con bodega.
- Implementar auditoria con listeners JPA o hooks de servicio, aprovechando las entidades ya existentes.
- Extender el sistema actual de notificaciones para soportar el correo de cuentas creadas por administracion.
- Reemplazar el hash manual SHA-256 por `PasswordEncoder` de Spring Security sin cambiar la estructura general del proyecto.
- Crear `application-test.properties` y desacoplar las pruebas de la base de datos local.

## Conclusion

El proyecto ya tiene una base funcional bastante solida: autenticacion, usuarios, productos, bodegas, movimientos, Swagger, manejo global de errores, notificaciones y frontend integrado.

Lo que falta no es rehacer el sistema, sino completar las reglas de negocio y cerrar los modulos que ya fueron iniciados, especialmente:

- reportes avanzados
- consultas funcionales de auditoria
- asignacion explicita de empleados a bodegas
- pruebas automatizadas

En resumen, la mejor estrategia no es rediseñar, sino evolucionar cuidadosamente la arquitectura actual.

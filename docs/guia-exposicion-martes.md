# Guia para exponer el proyecto el martes

## Objetivo de este documento

Este documento esta pensado para ayudarte a exponer el proyecto con seguridad aunque todavia haya partes del codigo que no dominas por completo.

La idea no es que memorices todo, sino que tengas claro:

- que hace el sistema
- como esta organizado
- cuales son los modulos importantes
- que mostrar primero
- como explicar piezas tecnicas que suelen confundir

Si el dia de la exposicion te pones nervioso, vuelve a esto:

**tu objetivo no es explicar cada clase del proyecto, sino demostrar que entiendes el flujo general y las decisiones principales.**

---

## 1. Por donde empezar al exponer

Empieza por lo funcional, no por el codigo.

Una introduccion corta que puedes usar:

> Este proyecto es un sistema de gestion de bodegas desarrollado con Spring Boot y un frontend estatico en JavaScript. Permite autenticacion de usuarios, administracion de productos, gestion de bodegas, registro de movimientos de inventario y control de acceso segun el rol del usuario.

Despues de eso, muestra el problema que resuelve:

- centralizar la informacion de bodegas
- controlar quien puede acceder al sistema
- registrar movimientos de entrada, salida y transferencia
- mantener trazabilidad de lo que ocurre

Eso te da una base clara antes de entrar a endpoints, clases o framework.

---

## 2. En que deberias centrarte

Para una exposicion corta o media, concentrate en estas 5 cosas:

### 1. Arquitectura general

Explica que el backend esta organizado por capas:

`Controller -> Service -> Repository`

Forma simple de decirlo:

- `Controller`: recibe las peticiones HTTP
- `Service`: contiene la logica de negocio
- `Repository`: habla con la base de datos

Si quieres ampliar un poco:

- `DTO`: define que entra y que sale por la API
- `Mapper`: convierte entidades a DTOs
- `Security`: protege rutas y valida JWT
- `Exceptions`: unifica respuestas de error

### 2. Seguridad

Explica que el sistema usa:

- Spring Security
- JWT
- autenticacion stateless

Idea simple:

- el usuario hace login
- el backend devuelve un token
- en las siguientes peticiones el frontend envia el token
- Spring valida el token antes de dejar entrar a rutas privadas

### 3. Modulos funcionales

Estos son los modulos que vale la pena mostrar:

- autenticacion
- usuarios
- productos
- bodegas
- movimientos
- auditoria

### 4. Control de acceso

Aqui tienes un punto fuerte del proyecto.

No solo hay autenticacion, tambien hay autorizacion:

- `ADMIN` puede ver y gestionar todo
- `USER` tiene acceso limitado segun su alcance operativo

Eso demuestra que el proyecto no solo guarda datos, sino que aplica reglas de negocio.

### 5. Manejo de errores y validaciones

Este punto suele impresionar si lo explicas bien porque muestra madurez del backend.

Puedes decir:

> El sistema no deja que las excepciones salgan sin control. Se centralizo el manejo de errores para que la API responda siempre en un formato JSON consistente.

---

## 3. Orden recomendado para tu demo

Este orden te ayuda a contar una historia clara:

1. Explica el problema y el objetivo del sistema.
2. Muestra la arquitectura general.
3. Enseña login y registro.
4. Enseña el panel o dashboard.
5. Muestra CRUD de productos.
6. Muestra CRUD de bodegas.
7. Muestra movimientos.
8. Explica seguridad, roles y restricciones.
9. Cierra mostrando Swagger o la estructura del backend.

Si el tiempo es corto, prioriza:

- login
- dashboard
- productos o bodegas
- movimientos
- seguridad

---

## 4. Que decir de cada modulo

## Autenticacion

Que decir:

- el sistema permite registro e inicio de sesion
- el login devuelve JWT
- el backend protege endpoints privados
- el usuario autenticado puede consultar su informacion con `/auth/me`

Que mostrar:

- login funcionando
- que sin token no puedes entrar a rutas privadas

## Usuarios

Que decir:

- existe gestion administrativa de usuarios
- un admin puede listar, crear, aprobar, bloquear y desbloquear usuarios
- hay estados como `PENDING`, `ACTIVE` y `BLOCKED`

Que mostrar:

- modulo admin
- cambio de estado de usuario

## Productos

Que decir:

- hay CRUD de productos
- los productos se asocian a una bodega
- la visibilidad depende del alcance del usuario

## Bodegas

Que decir:

- se pueden crear y administrar bodegas
- una bodega puede tener un manager
- eso soporta el control operativo del sistema

## Movimientos

Este es uno de los puntos mas importantes.

Que decir:

- hay movimientos `ENTRY`, `EXIT` y `TRANSFER`
- cada tipo tiene reglas diferentes
- el sistema valida origen y destino segun el tipo
- el movimiento queda asociado al usuario autenticado que lo registra

Eso demuestra reglas reales de negocio.

## Auditoria

Que decir:

- el sistema registra cambios importantes
- guarda quien hizo la accion
- guarda valores anteriores y nuevos

Aunque no muestres toda la parte de auditoria en vivo, mencionarla suma bastante.

---

## 5. Como explicar el backend sin enredarte

Usa esta explicacion corta:

> El backend esta hecho como una API REST en Spring Boot. Los controladores exponen endpoints, los servicios aplican reglas del negocio, los repositorios usan JPA para persistencia y la seguridad se maneja con Spring Security y JWT.

Si te piden mas detalle:

> Tambien se usan DTOs para no exponer directamente las entidades, mappers para transformar datos y un manejador global de errores para responder de forma consistente.

---

## 6. Explicacion de cosas que pueden confundirte

## 6.1. Que es `@RestControllerAdvice`

Archivo relacionado:

- [GlobalExceptionHandler.java](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/src/main/java/com/proyectS1/warehouse_management/exceptions/GlobalExceptionHandler.java)

`@RestControllerAdvice` es una anotacion de Spring que sirve para capturar excepciones de forma global en todos los controladores REST.

En palabras simples:

- sin esto, cada controlador tendria que manejar sus errores manualmente
- con esto, puedes centralizar el manejo de excepciones en una sola clase
- la respuesta de error sale en JSON de forma consistente

Forma facil de explicarlo en una exposicion:

> `@RestControllerAdvice` funciona como un interceptor global de errores para la API. Si algo falla en cualquier controlador, esta clase decide como responder al cliente.

## 6.2. Para que sirve `GlobalExceptionHandler`

Archivo:

- [GlobalExceptionHandler.java](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/src/main/java/com/proyectS1/warehouse_management/exceptions/GlobalExceptionHandler.java)

Esta clase centraliza la respuesta de errores del backend.

Que hace:

- captura excepciones conocidas
- asigna un codigo HTTP correcto
- construye una respuesta JSON uniforme

Por ejemplo:

- si una validacion falla, responde `400`
- si no existe un recurso, puede responder `404`
- si ocurre un error inesperado, responde `500`

Esto mejora:

- claridad para el frontend
- mantenimiento del backend
- consistencia de la API

## 6.3. Que es `ApiErrorResponse`

Archivo:

- [ApiErrorResponse.java](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/src/main/java/com/proyectS1/warehouse_management/exceptions/ApiErrorResponse.java)

Es un `record` de Java que representa el formato estandar de error de la API.

Campos:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details`

En palabras simples:

> En vez de devolver errores desordenados o diferentes segun el caso, el backend usa una estructura unica para que el cliente siempre sepa como leer un fallo.

## 6.4. Que es `@ExceptionHandler`

Dentro de `GlobalExceptionHandler`, cada metodo con `@ExceptionHandler(...)` le dice a Spring:

- captura este tipo de excepcion
- y respondela de esta forma

Ejemplos reales del proyecto:

- `ResponseStatusException`
- `MethodArgumentNotValidException`
- `ConstraintViolationException`
- `HttpMessageNotReadableException`
- `Exception`

La ultima, `Exception.class`, actua como respaldo general.

## 6.5. Que es `ResponseStatusException`

Es una excepcion de Spring que permite lanzar errores con codigo HTTP desde la capa de servicio o controlador.

Ejemplo mental:

- si buscas un producto y no existe
- puedes lanzar una excepcion con `404`
- luego el `GlobalExceptionHandler` la convierte en JSON

Eso evita tener `if` gigantes en los controladores.

## 6.6. Que son las validaciones como `MethodArgumentNotValidException`

Aparecen cuando el request no cumple reglas de validacion.

Por ejemplo:

- un campo obligatorio viene vacio
- un numero requerido no se envia
- el body no cumple con los DTOs

Entonces Spring lanza esa excepcion y el handler responde con detalles legibles.

## 6.7. Por que esto es bueno en un proyecto real

Porque separa responsabilidades:

- la logica del negocio no se mezcla con el formato de error
- los controladores quedan mas limpios
- el frontend recibe respuestas predecibles

Eso es exactamente el tipo de decision que puedes vender bien en una exposicion.

---

## 7. Como responder si te preguntan “por que usaste esto?”

## Si te preguntan por `@RestControllerAdvice`

Puedes decir:

> Lo use para centralizar el manejo de errores de toda la API y mantener un formato consistente en las respuestas. Asi no tengo que repetir el mismo manejo de errores en cada controlador.

## Si te preguntan por DTOs

> Los DTOs ayudan a controlar el contrato de entrada y salida de la API y evitan exponer directamente las entidades de base de datos.

## Si te preguntan por mappers

> Los mappers separan la representacion interna del modelo respecto a lo que expone la API. Eso hace el codigo mas mantenible.

## Si te preguntan por JWT

> JWT permite autenticacion stateless. El backend no necesita mantener una sesion en memoria para cada usuario, sino validar el token en cada request.

## Si te preguntan por Spring Security

> Se usa para proteger rutas, validar autenticacion y aplicar reglas de autorizacion por roles.

---

## 8. Que no necesitas explicar demasiado

No te desgastes explicando al detalle:

- cada import
- cada anotacion menor
- configuraciones internas de Spring
- detalles de bajo nivel de JPA
- todo el CSS del frontend

Si te preguntan algo muy especifico y no lo sabes, responde asi:

> Esa parte la implemente apoyandome en la arquitectura de Spring, pero el foco principal del proyecto estuvo en el flujo funcional, la seguridad y las reglas de negocio.

Eso suena honesto y profesional.

---

## 9. Guion corto para hablar 3 a 5 minutos

Puedes usar algo parecido a esto:

> Este proyecto es un sistema de gestion de bodegas desarrollado con Spring Boot. El objetivo es administrar usuarios, productos, bodegas y movimientos de inventario con seguridad y trazabilidad.  
>  
> La arquitectura esta organizada por capas: controladores para exponer la API, servicios para la logica de negocio y repositorios para persistencia. Ademas, se usan DTOs y mappers para controlar el contrato de datos.  
>  
> En seguridad se implemento autenticacion con JWT y autorizacion con Spring Security. Los usuarios pueden registrarse e iniciar sesion, y dependiendo de su rol tienen diferentes permisos dentro del sistema.  
>  
> A nivel funcional, el sistema permite CRUD de productos y bodegas, gestion administrativa de usuarios y registro de movimientos tipo entrada, salida y transferencia. Cada movimiento valida reglas segun su tipo.  
>  
> Tambien se incorporo un manejo global de errores usando `@RestControllerAdvice`, lo que permite responder en formato JSON consistente ante validaciones fallidas, recursos no encontrados o errores internos.  
>  
> Finalmente, el proyecto incluye un frontend estatico para consumir la API y una base para auditoria y notificaciones.

---

## 10. Si tuvieras que resumir el valor del proyecto en una frase

Puedes cerrar con esta idea:

> No es solo un CRUD; es un sistema con autenticacion, control de acceso, reglas de negocio, manejo consistente de errores y una estructura preparada para crecer.

---

## 11. Recomendacion final para el martes

Antes de exponer:

1. Prueba login.
2. Prueba una operacion de productos.
3. Prueba una operacion de bodegas.
4. Prueba un movimiento.
5. Ten abierto Swagger por si necesitas mostrar endpoints.
6. Ten clara la explicacion de `Controller -> Service -> Repository`.
7. Ten clara la explicacion de `@RestControllerAdvice`.

Si solo pudieras dominar dos cosas, domina estas:

- el flujo funcional del sistema
- la arquitectura general

Con eso ya puedes defender muy bien el proyecto.

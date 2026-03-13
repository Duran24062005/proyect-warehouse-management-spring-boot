# Notifications Context

## Objetivo

Este documento explica cómo funciona el envío de emails en el proyecto, qué hace cada pieza de código y por qué se introdujo una abstracción con `provider` y `factory`, aunque en apariencia el caso actual podría resolverse solo con una petición HTTP.

## Idea General

Hoy el sistema necesita enviar correos en dos momentos:

- cuando un usuario se registra
- cuando un usuario inicia sesión

La entrega real del correo no la hace Spring Boot directamente. En lugar de eso, este proyecto delega esa responsabilidad a una API externa construida en FastAPI, que ya está en producción.

Por eso, desde este backend Java lo que hacemos realmente es:

1. detectar el evento de negocio (`register` o `login`)
2. construir el mensaje a enviar
3. hacer una petición HTTP hacia la API externa de emails

Sí: técnicamente, el caso actual se puede resumir como "hacer una petición HTTP y enviar datos". Esa intuición es correcta.

## Flujo de Funcionamiento

### Registro

1. El cliente llama `POST /api/auth/register`.
2. `AuthController` delega en `AuthServiceImpl`.
3. `AuthServiceImpl` valida que el email no exista.
4. Se construye el `AppUser`, se hashea la contraseña y se guarda en base de datos.
5. Una vez el usuario queda persistido, `AuthServiceImpl` llama a `AuthEmailNotificationService`.
6. `AuthEmailNotificationService` construye un `EmailNotificationCommand` con:
   - `userId`
   - `recipient`
   - `subject`
   - `body`
7. Ese servicio pide a `EmailNotificationProviderFactory` el proveedor activo.
8. La factory devuelve el proveedor configurado, hoy `FastApiEmailNotificationProvider`.
9. `FastApiEmailNotificationProvider` hace la petición HTTP `multipart/form-data` a la API FastAPI.

### Login

1. El cliente llama `POST /api/auth/login`.
2. `AuthController` delega en `AuthServiceImpl`.
3. `AuthServiceImpl` valida credenciales.
4. Si son correctas, genera el JWT.
5. Luego dispara `AuthEmailNotificationService`.
6. Ese servicio vuelve a construir el comando y delega al proveedor resuelto por la factory.
7. El proveedor envía el request HTTP a la API de FastAPI.

## Clases y Responsabilidades

### `AuthServiceImpl`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/services/impl/AuthServiceImpl.java`

Es el punto donde se integra la notificación con el flujo de autenticación.

Su responsabilidad no es enviar HTTP directamente, sino decidir **cuándo** debe dispararse un email:

- después de un registro exitoso
- después de un login exitoso

Esto es importante porque `AuthServiceImpl` conoce el evento de negocio.

### `AuthEmailNotificationService`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/service/AuthEmailNotificationService.java`

Este servicio encapsula la lógica específica de notificaciones para autenticación.

Se encarga de:

- construir asunto y cuerpo del correo
- generar el `EmailNotificationCommand`
- decidir qué hacer si el envío falla
- delegar el envío real al proveedor seleccionado

En otras palabras:

- `AuthServiceImpl` sabe **cuándo**
- `AuthEmailNotificationService` sabe **qué enviar**

### `EmailNotificationCommand`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/model/EmailNotificationCommand.java`

Es un objeto simple que representa la intención de envío.

Contiene solo los datos necesarios para el proveedor:

- `userId`
- `recipient`
- `subject`
- `body`

Su objetivo es desacoplar la capa de negocio del formato interno del proveedor.

### `EmailNotificationProvider`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/provider/EmailNotificationProvider.java`

Es una interfaz.

Define el contrato común para cualquier mecanismo de envío:

- exponer una clave de proveedor
- enviar un `EmailNotificationCommand`

Ejemplo actual:

- `fastapi`

Ejemplos futuros posibles:

- `sendgrid`
- `smtp`
- `ses`
- `mock`

### `FastApiEmailNotificationProvider`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/provider/FastApiEmailNotificationProvider.java`

Es el adaptador concreto que sabe cómo hablar con tu API externa de FastAPI.

Hace la petición HTTP real:

- método `POST`
- endpoint `/emails/send`
- `Content-Type: multipart/form-data`

Y mapea el comando a los campos esperados por FastAPI:

- `user_id`
- `recipient`
- `subject`
- `body`

### `EmailNotificationProviderFactory`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/provider/EmailNotificationProviderFactory.java`

Su responsabilidad es seleccionar el proveedor activo según configuración.

Hoy revisa:

- `app.notifications.email.provider=fastapi`

Y devuelve la implementación que coincida con esa clave.

### `EmailNotificationProperties`

Archivo:

- `src/main/java/com/proyectS1/warehouse_management/notifications/config/EmailNotificationProperties.java`

Centraliza la configuración leída desde `application.properties`.

Aquí se definen cosas como:

- si el envío está habilitado
- si el flujo debe fallar cuando el email no se puede enviar
- qué proveedor usar
- cuál es la URL base de la API externa
- cuál es el path del endpoint

## Entonces, ¿por qué no hacer solo una petición HTTP?

Sí se podría.

La versión más simple del sistema habría sido:

1. en `AuthServiceImpl`
2. crear un `RestClient`
3. hacer `POST` directo a la API FastAPI

Eso habría funcionado para el caso actual.

## Ventajas de esa versión simple

- menos clases
- menos abstracción
- implementación más rápida
- menor costo mental al inicio

## Desventajas de esa versión simple

Todo quedaría mezclado en una sola clase de negocio:

- validación de auth
- persistencia del usuario
- generación de JWT
- construcción del correo
- detalles HTTP
- manejo de errores del proveedor externo

Eso genera acoplamiento.

`AuthServiceImpl` pasaría a saber demasiado:

- reglas de autenticación
- formato del proveedor externo
- detalles de transporte HTTP
- estructura de payload multipart

## ¿Por qué tiene sentido usar `provider`?

El `provider` existe para encapsular **cómo** se envía el correo.

Eso permite que la capa de negocio no dependa directamente de:

- FastAPI
- multipart/form-data
- nombres específicos de campos como `user_id`
- URLs externas

En este diseño:

- negocio: "hay que enviar un correo"
- provider: "yo sé cómo enviarlo usando FastAPI"

Eso hace el código más mantenible.

## ¿Por qué tiene sentido usar `factory`?

La `factory` evita que el servicio de negocio conozca implementaciones concretas.

Hoy puede parecer excesiva porque solo existe un proveedor.

Pero introduce estas ventajas:

- permite cambiar el proveedor activo por configuración
- evita acoplar `AuthEmailNotificationService` a `FastApiEmailNotificationProvider`
- deja lista la arquitectura para otros canales o implementaciones
- facilita pruebas futuras con un proveedor falso o mock

## ¿Es obligatorio usar factory en este proyecto?

No, no era estrictamente obligatorio para que funcionara.

Era suficiente con hacer una petición HTTP directa.

La decisión de usar `factory` es una decisión de diseño, no una necesidad técnica mínima.

Se eligió porque:

- tú pediste explícitamente aplicar un patrón factory
- el dominio de notificaciones puede crecer
- la URL del proveedor debe poder cambiar sin propagar lógica HTTP en servicios de negocio

## Integración con `application.properties`

La integración depende de estas propiedades:

```properties
app.notifications.email.enabled=true
app.notifications.email.fail-on-error=false
app.notifications.email.provider=fastapi
app.notifications.email.fastapi.base-url=http://127.0.0.1:8000
app.notifications.email.fastapi.send-path=/emails/send
```

Esto permite cambiar rápido:

- el dominio de la API externa
- el path
- el proveedor activo
- la estrategia de error

sin editar clases Java.

## Manejo de Errores

`AuthEmailNotificationService` controla qué pasa si la API externa falla.

### Si `fail-on-error=false`

- se registra el error en logs
- el registro o login continúa normalmente

### Si `fail-on-error=true`

- se lanza `502 Bad Gateway`
- el flujo de auth falla porque el email se considera parte obligatoria del proceso

## Resumen Corto

### Qué hace el sistema hoy

- detecta eventos de auth
- arma un comando de email
- selecciona proveedor por factory
- envía una petición HTTP a FastAPI

### Qué parte hace el envío real

- `FastApiEmailNotificationProvider`

### Qué parte decide cuándo enviar

- `AuthServiceImpl`

### Qué parte decide qué contenido enviar

- `AuthEmailNotificationService`

### Qué parte decide con qué proveedor trabajar

- `EmailNotificationProviderFactory`

## Conclusión

Tu intuición es correcta: el requerimiento actual se podía resolver con una sola petición HTTP.

El `provider` y la `factory` no existen porque el envío lo exija técnicamente, sino porque ayudan a:

- separar responsabilidades
- reducir acoplamiento
- cambiar proveedor sin tocar la lógica de auth
- preparar el código para crecer mejor

Si en algún momento quieres simplificarlo, es posible colapsar esta solución a una versión más directa. Pero para una integración que ya apunta a una API externa en producción y que además quiere ser configurable por propiedades, esta estructura tiene sentido y queda más mantenible.

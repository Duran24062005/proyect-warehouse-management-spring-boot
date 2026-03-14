# Dev Context Notes

Este documento funciona como una bitacora tecnica para aclarar conceptos del proyecto. La idea es ir ampliandolo cada vez que aparezca un tema que no este del todo claro, aterrizandolo al contexto real de `warehouse-management`.

## 1. `@Transactional`

### Que es

`@Transactional` es una anotacion de Spring que indica que un metodo debe ejecutarse dentro de una transaccion de base de datos.

Una transaccion agrupa operaciones para que se comporten como una sola unidad de trabajo:

- si todo sale bien, se confirma
- si algo falla, se revierte

### Para que sirve

Se usa para mantener consistencia cuando un metodo:

- guarda datos
- actualiza datos
- elimina datos
- combina varias operaciones que deben salir bien juntas

### Ejemplo mental simple

Si un servicio:

1. busca un producto
2. crea un movimiento
3. actualiza stock

y falla en el paso 3, no quieres que el movimiento quede guardado a medias. La transaccion evita ese estado inconsistente.

## 2. `@Transactional(readOnly = true)`

### Que significa

Es una transaccion pensada solo para lectura.

Le dice a Spring y a Hibernate que ese metodo:

- consulta datos
- no deberia modificar entidades
- puede beneficiarse de algunas optimizaciones

### Para que se usa

Es ideal en metodos como:

- `findAll`
- `findOne`
- `findByRole`
- `me`

### Por que no simplemente quitar `@Transactional`

Porque incluso en lectura puede ser util tener transaccion cuando:

- se leen relaciones `LAZY`
- se accede a varias entidades relacionadas
- se quiere un contexto persistente estable durante todo el metodo

### Ojo importante

`readOnly = true` no significa que sea imposible escribir. Es una declaracion de intencion y una optimizacion, no una barrera absoluta de seguridad.

## 3. Diferencia entre `@Transactional`, `@Transactional(readOnly = true)` y no usarlo

### `@Transactional`

Usalo cuando el metodo modifica estado:

- `save`
- `update`
- `delete`
- `register`
- `changePassword`

### `@Transactional(readOnly = true)`

Usalo cuando el metodo solo consulta y quieres mantener contexto transaccional para lectura.

### Sin `@Transactional`

Puede funcionar en consultas simples, pero puede darte problemas cuando:

- accedes a relaciones `LAZY` fuera de la sesion
- haces varios pasos que deberian compartir el mismo contexto
- necesitas consistencia de lectura durante todo el metodo

## 4. Relaciones JPA

En JPA las entidades pueden relacionarse entre si. Ejemplos comunes:

- `@OneToOne`
- `@OneToMany`
- `@ManyToOne`
- `@ManyToMany`

Estas relaciones pueden cargarse de dos maneras principales:

- `EAGER`
- `LAZY`

## 5. `LAZY`

### Que significa

`LAZY` significa que la relacion no se carga inmediatamente cuando traes la entidad principal.

Se carga solo cuando realmente la necesitas.

### Ejemplo conceptual

Si cargas un `Warehouse`, tal vez no necesitas traer de una vez todo su manager o todos sus productos relacionados. Con `LAZY`, Hibernate difiere esa carga hasta que intentes acceder a esa propiedad.

### Ventajas

- evita consultas innecesarias
- reduce carga de memoria
- mejora rendimiento cuando no necesitas todas las relaciones

### Riesgo comun

Si accedes a una relacion `LAZY` fuera de una sesion activa de Hibernate, puedes obtener:

- `LazyInitializationException`

Eso pasa mucho cuando:

1. el repositorio trae la entidad
2. el metodo termina
3. despues intentas leer una relacion `LAZY`
4. la sesion ya esta cerrada

### Relacion con `@Transactional(readOnly = true)`

Muchas veces se usa precisamente para esto:

- mantener abierta la sesion mientras el servicio consulta
- permitir mapear entidades a DTOs sin romper por carga perezosa

## 6. `EAGER`

### Que significa

`EAGER` significa que la relacion se carga de inmediato junto con la entidad principal.

### Ventajas

- es mas simple de entender al inicio
- evita algunos errores de `LazyInitializationException`

### Desventajas

- puede disparar mas consultas de las necesarias
- puede cargar demasiados datos
- puede volver mas pesado cada `find`

Por eso, en proyectos reales suele preferirse `LAZY` y controlar mejor que se consulta y cuando.

## 7. Tipos de relaciones mas comunes

### `@OneToOne`

Una entidad se relaciona con una sola entidad del otro lado.

Ejemplo:

- un perfil y una configuracion unica

### `@OneToMany`

Una entidad tiene muchas del otro lado.

Ejemplo:

- una bodega puede tener muchos movimientos

### `@ManyToOne`

Muchas entidades apuntan a una sola.

Ejemplo:

- muchos productos pueden pertenecer a una misma bodega

### `@ManyToMany`

Muchas entidades de un lado se relacionan con muchas del otro.

Ejemplo:

- usuarios y permisos en un sistema mas complejo

## 8. Como aterrizarlo a este proyecto

En `warehouse-management`, estas ideas aparecen cuando:

- un servicio consulta usuarios, productos, bodegas o movimientos
- luego mapea entidades a DTOs
- algunas relaciones no conviene traerlas siempre completas

Por eso tiene sentido ver cosas como:

- `@Transactional(readOnly = true)` en servicios de consulta
- mappers para evitar exponer entidades enteras
- relaciones cargadas de forma controlada

## 9. Regla practica rapida

Si el metodo:

- solo lee: piensa en `@Transactional(readOnly = true)`
- escribe o actualiza: usa `@Transactional`
- trabaja con relaciones JPA y DTOs: revisa si `LAZY` puede afectarte

## 10. Temas pendientes para ampliar

Iremos agregando aqui los temas que vayas preguntando, por ejemplo:

- `SecurityContext`
- `JwtFilter`
- `UserDetails`
- `Mapper` y DTOs
- `ResponseEntity`
- diferencias entre `Optional`, `List` y entidades
- `CascadeType`
- `fetch = LAZY` vs `fetch = EAGER`
- `@RestControllerAdvice`
- `ResponseStatusException`

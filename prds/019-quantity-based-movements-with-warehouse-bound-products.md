# PRD 019 - Quantity-Based Movements with Warehouse-Bound Products

## Resumen

Documentar el comportamiento funcional vigente antes de migrar el dominio hacia productos individuales.

En el estado actual:

- `Product` pertenece a una sola bodega en el modelo de datos
- `Movement` solicita `quantity`
- las operaciones `ENTRY`, `EXIT` y `TRANSFER` se registran sobre un `productId`

Este PRD no propone una solucion nueva; deja trazado el modelo anterior para referencia historica, auditoria funcional y base comparativa antes del cambio de dominio.

---

## Problema Documentado

El sistema actual mezcla dos enfoques de inventario:

1. Producto como catalogo movible por cantidad.
2. Producto como registro asociado a una sola bodega.

Eso produce una tension funcional:

- si el producto es un catalogo, `quantity` tiene sentido, pero la bodega unica no;
- si el producto es un activo individual, la bodega unica tiene sentido, pero `quantity` sobra.

Aunque inconsistente, este es el comportamiento implementado hoy y debe quedar documentado antes de reemplazarlo.

---

## Objetivo

Describir con precision la funcionalidad actual de movimientos para:

- preservar el contexto previo al refactor
- evitar perdida de decisiones historicas
- facilitar comparacion entre el modelo anterior y el modelo futuro

---

## Modelo Funcional Vigente

### Productos

Cada `product` mantiene:

- `id`
- `name`
- `category`
- `price`
- `warehouseId`

Interpretacion real actual:

- el producto queda visible dentro del alcance de una bodega
- el producto no maneja stock agregado persistido
- la bodega asociada funciona como ubicacion directa del producto

### Movimientos

Cada `movement` registra:

- `movementType`
- `registeredByUserId`
- `performedByEmployeeId`
- `originWarehouseId`
- `destinationWarehouseId`
- `productId`
- `quantity`

Interpretacion real actual:

- el movimiento opera como si se registraran cantidades de producto
- no existe actualizacion de stock ni saldo por producto
- el sistema conserva trazabilidad del evento, no inventario consolidado

---

## Reglas de Negocio Vigentes

- `ENTRY` requiere `destinationWarehouseId` y no `originWarehouseId`
- `EXIT` requiere `originWarehouseId` y no `destinationWarehouseId`
- `TRANSFER` requiere ambas bodegas
- `TRANSFER` no permite misma bodega origen y destino
- `performedByEmployeeId` debe pertenecer a un usuario `EMPLOYEE`
- el empleado debe pertenecer a una bodega participante del movimiento
- `quantity` es obligatoria y debe ser positiva

---

## Limitaciones Conocidas

- no existe stock persistido por producto
- no existe saldo por bodega y producto
- la cantidad registrada no altera inventario calculado
- `product.warehouseId` y `movement.quantity` representan dos modelos de negocio distintos
- el dominio no distingue explicitamente entre catalogo, lote o activo individual

---

## Criterios de Referencia

El comportamiento anterior queda correctamente documentado si:

- se reconoce que `quantity` es parte obligatoria del contrato actual de movimientos
- se reconoce que `Product` sigue atado a una sola bodega
- se deja explicita la inconsistencia entre ambos conceptos
- la documentacion sirve como baseline antes de migrar a productos individuales

---

## Supuestos

- este PRD describe el estado actual implementado, no el estado objetivo
- la migracion posterior podra eliminar `quantity` o redefinir `Product`, pero ese cambio no forma parte de este documento

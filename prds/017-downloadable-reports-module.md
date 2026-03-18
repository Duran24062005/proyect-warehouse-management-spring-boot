# PRD 017 - Downloadable Reports Module

## Resumen

Implementar un modulo de reportes descargables para el sistema LogiTrack, disponible para usuarios autenticados con rol `ADMIN` y `USER`, con capacidad de:

- previsualizar reportes desde la interfaz web
- descargar reportes en `PDF`, `IMG`, `TXT` y `CSV`
- aplicar filtros basicos por entidad

El sistema de reportes debe apoyarse en los datos ya existentes del dominio operativo sin introducir persistencia adicional ni procesos asincronos.

---

## Problema

Actualmente la plataforma permite consultar productos, bodegas y movimientos desde sus modulos respectivos, pero no ofrece una salida formal para:

- compartir informacion operativa fuera del sistema
- descargar evidencia de gestion en formatos comunes
- consolidar vista resumen + detalle en un solo documento

Esto limita la trazabilidad administrativa, la presentacion de informacion y la exportacion para auditoria o revision operativa.

---

## Objetivo

Construir una primera version del sistema de reportes con una experiencia simple:

- el usuario selecciona un tipo de reporte
- ajusta filtros segun la entidad
- ve una previsualizacion con resumen y tabla
- descarga el mismo dataset en el formato deseado

---

## Alcance Funcional

### Tipos de reporte

- `movements`
- `products`
- `warehouses`

### Formatos de salida

- `json` para previsualizacion interna
- `csv`
- `txt`
- `pdf`
- `img`

### Filtros soportados

- movimientos:
  - `productId`
  - `warehouseId`
  - `movementType`
- productos:
  - `warehouseId`
  - `category`
- bodegas:
  - `managerUserId`

### Reglas de acceso

- `ADMIN` puede generar reportes globales
- `USER` solo puede generar reportes dentro de su alcance actual
- `EMPLOYEE` no puede acceder al modulo de reportes

---

## Experiencia de Usuario

### Vista web

Se agrega una nueva pantalla privada `Reportes` dentro del area autenticada de la aplicacion.

La pantalla incluye:

- selector de tipo de reporte
- filtros dinamicos por tipo
- selector de formato de descarga
- accion de `Previsualizar`
- accion de `Descargar`
- bloque de resumen con KPIs
- tabla con el detalle del reporte

### Archivos descargables

Los exportables deben tener una presentacion entendible y util para uso real.

- `CSV`: salida tabular plana para hojas de calculo
- `TXT`: encabezado, resumen, filtros y tabla ASCII legible
- `PDF`: composicion editorial con titulo, subtitulo, descripcion, resumen y tabla
- `IMG`: composicion visual con encabezado, KPIs y vista tabular resumida

---

## Arquitectura Propuesta

### Backend

Se implementa un modulo de reportes compuesto por:

- controlador de reportes
- servicio de orquestacion de datasets
- modelo comun de `ReportDataset`
- renderizadores por formato

Endpoint principal:

- `GET /api/reports`

Parametros:

- `type`
- `format`
- filtros opcionales segun la entidad

Comportamiento:

- `format=json` devuelve preview estructurado
- otros formatos devuelven archivo descargable con `Content-Disposition`

### Frontend

Se implementa:

- nueva pagina `frontend/platform/reports.html`
- nuevo modulo `frontend/js/pages/reports-page.js`
- ampliacion del helper HTTP para descarga binaria
- actualizacion de navegacion compartida

---

## Datos y Presentacion

Cada reporte debe construirse desde un contrato comun con:

- `reportType`
- `title`
- `subtitle`
- `description`
- `generatedAt`
- `generatedBy`
- `filters`
- `summary`
- `columns`
- `rows`

Resumen por tipo:

- movimientos:
  - total de registros
  - conteo por tipo
- productos:
  - total de productos
  - suma total de precios
  - conteo por categoria
- bodegas:
  - total de bodegas
  - capacidad total
  - conteo con y sin manager

---

## Criterios de Aceptacion

- el modulo `Reportes` aparece en la navegacion para `ADMIN` y `USER`
- `EMPLOYEE` no puede usar el endpoint ni la vista
- la previsualizacion refleja exactamente el dataset descargable
- los filtros funcionan segun el tipo de reporte elegido
- el archivo descargado usa el nombre `report-<type>-<yyyyMMdd-HHmm>.<ext>`
- `PDF`, `TXT` e `IMG` incluyen titulo, subtitulo, descripcion y una composicion mas elaborada
- `CSV` mantiene estructura tabular simple

---

## Restricciones y Supuestos

- no se guarda historial de reportes generados
- no hay generacion asincrona
- no se incorporan rangos de fecha en esta version
- la logica de permisos reutiliza el alcance ya definido en servicios existentes
- no se modifican los DTOs de dominio originales

---

## Validacion

### Tecnica

- compilar la aplicacion con `./mvnw -q -DskipTests package`
- validar el frontend con chequeo sintactico del modulo nuevo

### Manual

- generar preview por cada tipo de reporte
- descargar en `PDF`, `IMG`, `TXT` y `CSV`
- validar legibilidad del archivo
- validar que el contenido coincide con los filtros aplicados

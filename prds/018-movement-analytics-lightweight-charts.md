# PRD 018 - Movement Analytics with Lightweight Charts

## Resumen

Agregar una capa de analítica visual para movimientos usando TradingView Lightweight Charts, integrada en el `Dashboard` y en el modulo `Reportes`, con datos agregados por dia para los ultimos 30 dias.

La funcionalidad debe reutilizar el alcance de permisos ya existente:

- `ADMIN` ve analitica global
- `USER` ve analitica dentro de su alcance operativo
- `EMPLOYEE` no accede a analiticas

---

## Problema

El sistema ya ofrece tablas y reportes descargables, pero no cuenta con una vista analitica inmediata para detectar:

- tendencias diarias de movimientos
- volumen de eventos por periodo
- diferencias entre entradas, salidas y transferencias

Esto obliga al usuario a interpretar datos tabulares manualmente y reduce la capacidad del dashboard y de reportes para comunicar comportamiento operativo.

---

## Objetivo

Exponer una vista visual simple y legible de la tendencia de movimientos:

- en `Dashboard` como bloque ejecutivo
- en `Reportes` como seccion sincronizada con la previsualizacion de movimientos

La solucion debe ser ligera, reutilizable y compatible con el frontend actual en vanilla JavaScript.

---

## Alcance Funcional

### Fuente de datos

Analiticas basadas exclusivamente en `movements`, usando agregacion diaria por fecha de registro.

### Ventana temporal

- fija en `ultimos 30 dias` para esta v1

### Filtros soportados

- `warehouseId`
- `productId`
- `movementType`

En `Reportes`, la grafica debe respetar exactamente los mismos filtros de la tabla cuando el tipo de reporte sea `movements`.

### Visualizacion

- serie principal de linea: total diario
- series secundarias de linea:
  - `ENTRY`
  - `EXIT`
  - `TRANSFER`
- leyenda reactiva al `crosshair`
- estado vacio claro cuando no existan datos

---

## Experiencia de Usuario

### Dashboard

Se agrega una nueva tarjeta de analisis con:

- titulo y contexto del periodo
- 2-3 KPIs ejecutivos
- grafica de linea con tendencia diaria
- leyenda externa con fecha y valores por serie

No se agregan filtros manuales en esta pantalla para la primera version.

### Reportes

Se agrega una seccion `Analisis visual` visible solo cuando el tipo de reporte sea `movements`.

Comportamiento:

- al presionar `Previsualizar`, se actualiza tabla + resumen + grafica
- si el tipo cambia a `products` o `warehouses`, la grafica se oculta

---

## Arquitectura Propuesta

### Backend

Nuevo endpoint:

- `GET /api/reports/analytics/movements`

Parametros:

- `window=30d`
- `warehouseId`
- `productId`
- `movementType`

Respuesta:

- `title`
- `subtitle`
- `window`
- `rangeLabel`
- `generatedAt`
- `filters`
- `summary`
- `series`
- `points`

La agregacion debe:

- agrupar movimientos por fecha
- rellenar dias sin movimientos con `0`
- respetar permisos del usuario autenticado

### Frontend

Integracion de Lightweight Charts en modo standalone/CDN sin bundler.

Se crea un modulo compartido para:

- crear y destruir charts
- aplicar opciones visuales base
- renderizar series
- sincronizar leyenda externa
- reaccionar a resize mediante `ResizeObserver`

---

## Criterios de Aceptacion

- existe el endpoint `GET /api/reports/analytics/movements`
- responde datos diarios para los ultimos 30 dias
- rellena dias vacios con `0`
- `Dashboard` muestra la grafica al cargar
- `Reportes` muestra la grafica solo para `movements`
- la grafica de `Reportes` usa el mismo filtro que la previsualizacion tabular
- la leyenda cambia con el `crosshair`
- la grafica es responsiva en desktop y mobile
- `EMPLOYEE` no puede acceder al endpoint

---

## Restricciones y Supuestos

- solo se incluyen analiticas de movimientos en esta fase
- no se agregan comparaciones semanales ni ventanas configurables
- no se exportan graficas como imagen o PDF
- la libreria se carga desde CDN porque el frontend actual no usa bundler

---

## Validacion

### Tecnica

- compilar con `./mvnw -q -DskipTests package`
- validar sintaxis de los modulos JS nuevos o modificados

### Manual

- revisar dashboard con usuario `ADMIN`
- revisar dashboard con usuario `USER`
- validar coincidencia entre filtros del reporte y la grafica
- revisar comportamiento en estados sin datos

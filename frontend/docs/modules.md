# Modulos Compartidos

## `js/core/api.js`

Archivo: [api.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/api.js)

Responsabilidad:

- construir URLs con query params
- agregar headers por defecto
- incluir `Authorization: Bearer ...` cuando `auth: true`
- parsear respuestas JSON o texto
- lanzar errores con mensajes legibles
- limpiar sesion cuando el backend responde `401`

Funcion principal:

- `request(path, options)`

Opciones soportadas:

- `method`
- `body`
- `query`
- `auth`

## `js/core/auth.js`

Archivo: [auth.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/auth.js)

Responsabilidad:

- resolver rutas relativas entre paginas publicas y `platform/`
- redirigir si el usuario ya esta autenticado
- exigir autenticacion para modulos privados
- restringir acceso administrativo

Funciones clave:

- `getLoginPath()`
- `getRegisterPath()`
- `getDashboardPath()`
- `logout()`
- `redirectIfAuthenticated()`
- `requireAuth({ adminOnly })`

## `js/core/session.js`

Archivo: [session.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/session.js)

Responsabilidad:

- guardar y leer token
- guardar y leer usuario autenticado
- guardar la base URL del backend
- exponer helpers de rol y autenticacion

Claves de `localStorage`:

- `logitrack-api-base`
- `logitrack-token`
- `logitrack-user`

Funciones mas usadas:

- `getApiBase()`
- `setApiBase(value)`
- `saveLogin(response)`
- `clearSession()`
- `isAuthenticated()`
- `isAdmin()`

## `js/core/ui.js`

Archivo: [ui.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/ui.js)

Responsabilidad:

- pintar textos repetidos en layout
- mostrar avisos de exito, error o informacion
- formatear fechas y moneda
- poblar `select`
- renderizar tablas simples
- inicializar elementos comunes del layout

Funciones principales:

- `fillText(selector, value)`
- `showNotice(element, message, type)`
- `formatDate(value)`
- `formatMoney(value)`
- `fillSelect(select, items, options)`
- `renderTable(tbody, items, renderRow, options)`
- `setupLayout(pageKey, user)`

Notas:

- `setupLayout()` tambien registra logout y el formulario para cambiar la base URL del backend.
- `showNotice()` oculta el contenedor cuando el mensaje esta vacio.

## `js/core/tailwind-theme.js`

Archivo: [tailwind-theme.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/tailwind-theme.js)

Responsabilidad:

- definir o extender la configuracion visual de Tailwind cargada por CDN.

## `app.js`

Archivo: [app.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/app.js)

Estado actual:

- archivo legado
- solo muestra un `console.warn`
- no debe usarse como base para nuevas funcionalidades

# Mantenimiento

## Como levantar el frontend

1. Inicia el backend en `http://localhost:8000`.
2. Sirve la carpeta `frontend/` con un servidor estatico.
3. Abre [index.html](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/index.html).
4. Si hace falta, cambia la base URL desde el formulario de configuracion rapida.

## Convenciones del proyecto

- una pagina HTML por modulo
- un script por pagina en `js/pages`
- helpers compartidos solo en `js/core`
- tablas simples renderizadas con `renderTable()`
- avisos visuales centralizados con `showNotice()`

## Donde tocar segun el cambio

Si cambias una pantalla:

- revisa su HTML en `frontend/` o `frontend/platform/`
- revisa su script en `frontend/js/pages/`
- revisa `frontend/styles/` solo si el cambio afecta layout o estilos compartidos

Si cambias autenticacion o sesion:

- revisa [auth.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/auth.js)
- revisa [session.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/session.js)
- revisa [api.js](/home/alexi-dg/Desktop/GitHub_Repositories/SpringBoot/warehouse-management/frontend/js/core/api.js)

## Riesgos frecuentes

- romper rutas relativas entre paginas publicas y `platform/`
- olvidar `auth: true` en endpoints privados
- cambiar IDs del HTML sin actualizar selectores en `js/pages`
- asumir que un `USER` ve datos globales cuando en realidad depende de su bodega asignada

## Recomendaciones para nuevas pantallas

1. Crear el HTML en `frontend/platform/` o en la raiz si es publica.
2. Crear un modulo dedicado en `frontend/js/pages/`.
3. Reusar `requireAuth()` y `setupLayout()` si la pagina es privada.
4. Centralizar llamadas HTTP con `request()`.
5. Usar `showNotice()` para feedback de exito y error.

## Recomendaciones para debugging

- revisar `localStorage` para token, usuario y base URL
- confirmar que la API responda en la URL configurada
- comprobar si el backend retorna `401`, porque eso limpia la sesion
- validar que los roles y estados del usuario coincidan con la vista esperada

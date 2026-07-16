# KidsGuard — Backend de referencia ☁️

Servidor **autoalojable** y opcional para el control remoto de KidsGuard.
Permite que un adulto vea el uso de los dispositivos de sus hijos y los
controle a distancia desde un panel web, sin depender de ningún servicio de
terceros.

> La app KidsGuard funciona perfectamente **sin** este servidor (todo en
> local). Esto solo añade monitorización y control remotos si los quieres.

## Qué hace

- **Emparejamiento**: cada dispositivo se vincula con un *código de familia*
  y recibe un token propio.
- **Snapshots**: cada dispositivo envía periódicamente (cada ~15 min, o al
  pulsar «Sincronizar ahora») su perfil activo, configuración y uso del día.
- **Comandos remotos**: desde el panel web, el adulto encola acciones que el
  dispositivo aplica en la siguiente sincronización:
  - `grant_extra_minutes` — conceder 15/30/60 min extra.
  - `set_child_mode` — activar/desactivar el modo niños.
  - `set_allowed_apps` — cambiar la lista de apps permitidas.
  - `set_daily_limit` — cambiar el límite diario.
- **Panel web** en `/` para consultar todo y enviar comandos.

## Puesta en marcha

Requisitos: Node.js 18+.

```bash
cd kidsguard-backend
npm install
ADMIN_PASSWORD="tu-clave-secreta" FAMILY_CODE="MIFAMILIA" npm start
```

Variables de entorno:

| Variable | Por defecto | Descripción |
|---|---|---|
| `PORT` | `3000` | Puerto de escucha |
| `ADMIN_PASSWORD` | `cambia-esta-clave` | Contraseña del panel web |
| `FAMILY_CODE` | `FAMILIA123` | Código que introducen los dispositivos al emparejarse |

Los datos se guardan en `data.json` (no se versiona).

## Emparejar un dispositivo

1. Levanta el servidor y haz que sea accesible desde el móvil del niño
   (misma red local, o expuesto por Internet **siempre con HTTPS**).
2. En la app KidsGuard: **Panel → Control remoto (nube)**.
3. Activa la sincronización, escribe la URL del servidor (p. ej.
   `https://mi-servidor.com`) y el **código de familia**, y pulsa
   **Emparejar dispositivo**.
4. Abre el panel web en `http://TU_SERVIDOR:3000`, introduce la
   `ADMIN_PASSWORD` y verás el dispositivo con su uso en tiempo (casi) real.

## Seguridad ⚠️

Este servidor es una **referencia mínima** pensada para uso familiar:

- Ponlo **siempre detrás de HTTPS** si lo expones a Internet (por ejemplo con
  un proxy inverso como Caddy o Nginx). Los tokens viajan en cabeceras.
- Cambia `ADMIN_PASSWORD` y `FAMILY_CODE` por valores propios y difíciles.
- El almacenamiento es un fichero JSON; para varias familias o producción,
  conviene migrar a una base de datos y añadir cuentas por familia.

## API (resumen)

Dispositivo (requiere `Authorization: Bearer <token>` salvo `pair`):

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/pair` | Empareja: body `{familyCode, deviceName}` → `{deviceId, token}` |
| `POST` | `/api/devices/:id/snapshot` | Envía el estado del dispositivo |
| `GET` | `/api/devices/:id/commands` | Recoge y vacía la cola de comandos |

Administración (requiere `?pass=ADMIN_PASSWORD`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/admin/devices` | Lista dispositivos con su último snapshot |
| `POST` | `/admin/devices/:id/commands` | Encola un comando `{type, payload}` |

# Política de privacidad de KidsGuard

_Última actualización: julio de 2026 — **borrador**; antes de publicar en
Google Play debe alojarse en una URL pública y revisarse legalmente._

## Resumen

KidsGuard es una app de control parental que funciona **de forma
predeterminada 100 % en el dispositivo**: no crea cuentas y no envía ningún
dato fuera del dispositivo.

De forma **opcional y desactivada por defecto**, el adulto puede activar la
sincronización en la nube (menú «Control remoto») para vincular el
dispositivo con **su propio servidor** KidsGuard y monitorizarlo/controlarlo
a distancia. Solo en ese caso, y solo si el adulto lo activa expresamente, la
app envía datos —siempre al servidor que el propio adulto configure, nunca a
Anthropic ni a terceros.

## Datos que la app trata (solo localmente)

| Dato | Para qué | Dónde se guarda |
|---|---|---|
| PIN de adulto y respuesta de seguridad | Proteger el panel de control | Solo su hash SHA-256 con salt, en almacenamiento cifrado (AES-256) del propio dispositivo |
| Lista de apps permitidas y límites | Aplicar las reglas configuradas | Almacenamiento cifrado local |
| App en primer plano | Detectar y bloquear apps no permitidas | Se consulta en tiempo real; solo se conserva el tiempo de uso agregado del día |
| Tiempo de uso diario por app | Límites de tiempo y estadísticas para el adulto | Almacenamiento cifrado local; se reinicia cada día |

## Datos que NO se recogen

- No se recogen datos personales del niño ni del adulto.
- No se lee ni almacena el contenido de la pantalla: el servicio de
  accesibilidad está configurado con `canRetrieveWindowContent="false"` y
  solo recibe el nombre del paquete que pasa a primer plano.
- No hay analítica, publicidad, ubicación ni acceso a contactos, fotos,
  micrófono o cámara.
- Con la sincronización en la nube **desactivada** (estado por defecto), nada
  se transmite por internet salvo la navegación que el propio niño haga en el
  navegador infantil.

## Sincronización en la nube (opcional, desactivada por defecto)

Si el adulto la activa y empareja el dispositivo con su servidor:

| Dato enviado | Para qué | A dónde |
|---|---|---|
| Perfil activo, apps permitidas, límites y uso del día | Que el adulto monitorice y controle a distancia | Únicamente al servidor que el adulto configura |

- El servidor es **autoalojado por el adulto** (código abierto incluido en
  `kidsguard-backend/`); KidsGuard no opera ningún servidor central.
- No se envía el contenido de pantalla ni datos de navegación.
- Al desvincular el dispositivo, deja de enviarse cualquier dato.
- Recomendación: usar siempre HTTPS para la conexión con el servidor.

## Permisos especiales y su justificación

| Permiso | Justificación |
|---|---|
| Acceso a datos de uso | Detectar qué app está en primer plano para poder bloquearla |
| Servicio de accesibilidad | Detectar el cambio de app al instante (sin leer contenido) |
| Mostrar sobre otras apps | Mostrar la pantalla de bloqueo encima de una app no permitida |
| Administrador de dispositivo | Impedir la desinstalación sin conocimiento del adulto |
| Inicio automático (BOOT_COMPLETED) | Reactivar la protección al encender el dispositivo |

## Menores

KidsGuard está diseñada para que un adulto configure el dispositivo de un
menor. Como no se recoge ni transmite ningún dato, no aplica tratamiento de
datos personales de menores (COPPA/GDPR-K). Si en el futuro se añade
sincronización en la nube, esta política deberá revisarse.

## Eliminación de datos

Desinstalar la app elimina todos los datos, que solo existen en el propio
dispositivo.

## Contacto

Para dudas sobre esta política: _(añadir correo de contacto antes de publicar)_.

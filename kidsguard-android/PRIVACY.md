# Política de privacidad de KidsGuard

_Última actualización: julio de 2026 — **borrador**; antes de publicar en
Google Play debe alojarse en una URL pública y revisarse legalmente._

## Resumen

KidsGuard es una app de control parental que funciona **100 % en el
dispositivo**. No tiene servidores, no crea cuentas y **no envía ningún dato
fuera del dispositivo**.

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
- Nada se transmite por internet: la app no solicita el permiso de red.

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

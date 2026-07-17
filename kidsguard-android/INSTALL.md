# 📥 KidsGuard — Guía de descarga e instalación en Android

Esta guía explica, paso a paso, cómo obtener la app KidsGuard e instalarla en
un móvil o tablet Android, y cómo dejarla configurada y funcionando.

- **Compatibilidad**: Android 8.0 (Oreo) o superior · `minSdk 26`, `targetSdk 34`
- **Paquete**: `com.kidsguard.app`

---

## Paso 0 · Elige cómo obtener el APK

Tienes tres formas de conseguir el archivo instalable (`.apk`). La **Opción A**
es la más sencilla si no eres desarrollador.

### Opción A — Descargar el APK ya compilado (recomendada)

Cada cambio en el proyecto genera automáticamente el APK mediante GitHub
Actions.

1. Entra en el repositorio en GitHub → pestaña **Actions**.
2. En la lista de la izquierda elige el workflow **Build KidsGuard APK**.
3. Abre la ejecución más reciente que aparezca en verde ✅.
4. Baja hasta la sección **Artifacts** y descarga:
   - **`kidsguard-debug`** → contiene `app-debug.apk` (instalable tal cual).
   - `kidsguard-release` → versión optimizada; solo está firmada si se
     configuraron las claves (ver `PUBLISHING.md`). Si no, usa el *debug*.
5. Descomprime el `.zip` descargado: dentro está el archivo `.apk`.

> Los artifacts caducan a los 30 días. Si no ves ninguno, vuelve a lanzar el
> workflow desde **Actions → Build KidsGuard APK → Run workflow**.

### Opción B — Compilar el APK tú mismo

Requisitos: **JDK 17+** y el **SDK de Android 34** (por ejemplo, instalando
Android Studio).

```bash
cd kidsguard-android
./gradlew assembleDebug        # genera app/build/outputs/apk/debug/app-debug.apk
# o, para la versión de release (sin firmar si no hay keystore):
./gradlew assembleRelease
```

> Si el proyecto no incluye el *Gradle wrapper* (`gradlew`), usa `gradle`
> directamente teniendo Gradle 8.7+ instalado, o abre la carpeta
> `kidsguard-android/` en **Android Studio** y pulsa ▶️ Run.

### Opción C — Instalar directamente desde Android Studio

Con el dispositivo conectado por USB (depuración USB activada) y la carpeta
`kidsguard-android/` abierta en Android Studio, pulsa **Run ▶** y Android
Studio compila e instala la app en el dispositivo automáticamente. Si eliges
esta vía, puedes saltar al **Paso 3**.

---

## Paso 1 · Pasa el APK al dispositivo

Elige uno:

- **Por cable (ADB)** — la forma más limpia:
  ```bash
  adb install app-debug.apk
  ```
  Si ya había una versión instalada: `adb install -r app-debug.apk`.

- **Sin cable**: copia el `.apk` al teléfono (por cable de archivos, Google
  Drive, correo, etc.) y ábrelo desde la app **Archivos** del dispositivo.

---

## Paso 2 · Permite la instalación de orígenes desconocidos

Como el APK no viene de Play Store, Android pedirá permiso la primera vez:

1. Al abrir el `.apk`, aparecerá un aviso de seguridad.
2. Pulsa **Ajustes** en ese aviso (o ve a **Ajustes → Apps → Acceso
   especial → Instalar apps desconocidas**).
3. Concede permiso a la app desde la que abres el `.apk` (Archivos, Chrome…).
4. Vuelve atrás y pulsa **Instalar**.

---

## Paso 3 · Primer arranque y configuración

Al abrir **KidsGuard** por primera vez, un asistente te guía en 3 pasos:

1. **Crea el PIN de adulto** (4 a 8 dígitos) y una **pregunta de seguridad**
   para poder recuperarlo si lo olvidas.
2. **Concede los permisos del sistema** que la app va pidiendo (siguiente
   apartado).
3. **Elige las apps permitidas** y **activa el modo niños**.

### Permisos que hay que conceder

En la pantalla **Panel → 🔐 Permisos del sistema** verás cada permiso con un
indicador ✅/❌ y un botón para concederlo:

| Permiso | Para qué sirve | Imprescindible |
|---|---|---|
| **Acceso a datos de uso** | Detectar qué app está abierta y poder bloquearla | ✅ Sí |
| **Mostrar sobre otras apps** | Enseñar la pantalla de bloqueo encima de una app | ✅ Sí |
| **Servicio de accesibilidad** | Bloqueo instantáneo al cambiar de app (sin retardo) | ⭐ Muy recomendado |
| **Notificaciones** | Aviso del servicio activo y alertas al adulto | Recomendado |
| **Administrador de dispositivo** | Dificultar la desinstalación por el niño | Recomendado |
| **Launcher predeterminado** | Que KidsGuard sea la pantalla de inicio y arranque solo al encender | ✅ Sí (para protección completa) |

> Al pulsar «Conceder», Android te lleva a la pantalla de ajustes
> correspondiente. Activa el permiso y pulsa «Atrás» para volver a KidsGuard.

### Establecer KidsGuard como pantalla de inicio

Para que el niño solo vea las apps permitidas y la app se abra sola al
encender el dispositivo:

1. Concede el permiso **Launcher predeterminado** desde «Permisos del sistema».
2. Android preguntará qué app usar como inicio → elige **KidsGuard** y
   **Siempre**.

---

## Paso 4 · Uso diario

- **Activar/desactivar la protección**: abre KidsGuard (icono candado 🔒 en la
  pantalla infantil), introduce el PIN y usa el interruptor **Modo niños**.
- **Salir del modo niños**: toca el candado 🔒 del launcher infantil e
  introduce el PIN.
- **Perfiles, límites, filtrado web y control remoto**: todo se gestiona desde
  el **Panel** tras introducir el PIN.

---

## (Opcional) Paso 5 · Modo kiosco máximo (device owner)

Para la protección más fuerte (el niño no puede salir de las apps permitidas
de ninguna forma), en un dispositivo **recién restablecido de fábrica y sin
cuenta de Google añadida**:

```bash
adb shell dpm set-device-owner com.kidsguard.app/.receiver.AdminReceiver
```

Al activar luego el modo niños, KidsGuard fija el launcher, desactiva la barra
de estado y bloquea el modo seguro y el restablecimiento de fábrica. Todo se
revierte al desactivar el modo niños. Consulta el `README.md` para más
detalle.

---

## Solución de problemas

| Problema | Solución |
|---|---|
| «App no instalada» | Desinstala versiones previas; verifica que el `.apk` se descargó completo. |
| La protección no arranca sola tras reiniciar | En Xiaomi/Huawei/Samsung, permite el **inicio automático** de KidsGuard en los ajustes de batería. |
| El bloqueo tarda ~1 s | Activa el **servicio de accesibilidad** para bloqueo instantáneo. |
| Olvidé el PIN | Usa «¿Olvidaste el PIN?» y responde la pregunta de seguridad. |
| No aparece el APK en Actions | Los artifacts caducan a los 30 días; relanza el workflow. |

---

## Aviso legal

KidsGuard usa permisos sensibles (accesibilidad, datos de uso, superposición,
administrador de dispositivo) **exclusivamente** para el control parental
local. Instálalo solo en dispositivos de tu propiedad o de menores a tu cargo.
Consulta [`PRIVACY.md`](PRIVACY.md) para la política de privacidad.

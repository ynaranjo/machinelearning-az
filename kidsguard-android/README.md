# KidsGuard 🛡️ — Control parental para Android

App de control parental estilo **Kids Place**: convierte el móvil o la tablet en un
entorno seguro para niños, donde **solo se pueden usar las apps que un adulto haya
autorizado**. Se levanta automáticamente al encender el dispositivo.

## Funcionalidades

| Función | Descripción |
|---|---|
| 🏠 Launcher infantil | KidsGuard se establece como pantalla de inicio (HOME). El niño solo ve una cuadrícula con las apps permitidas. |
| ✅ Lista blanca de apps | El adulto marca qué apps puede usar el niño; todas las demás se bloquean al instante (incluidos Ajustes, Play Store, navegador, etc.). |
| 🔑 PIN de adulto | Todo el panel de control está protegido por un PIN de 4 a 8 dígitos (guardado con hash SHA-256 + salt, nunca en claro). |
| 🚀 Arranque automático | Al encender el dispositivo, el launcher infantil aparece de inmediato y un `BroadcastReceiver` de `BOOT_COMPLETED` reactiva el servicio de vigilancia. |
| 👁️ Vigilancia continua | Un servicio en primer plano detecta cada segundo la app activa (UsageStatsManager) y bloquea cualquier app no permitida mostrando una pantalla de bloqueo a pantalla completa. |
| ⏱️ Límite diario | Tiempo máximo total de pantalla al día (15–480 min). Al agotarse, se bloquean todas las apps hasta el día siguiente. |
| ⏳ Límite por app | Límite diario individual para cada app permitida. |
| 🌙 Hora de dormir | Franja horaria (admite cruce de medianoche, ej. 21:00→07:00) en la que todo queda bloqueado. |
| 📊 Estadísticas de uso | El adulto ve cuánto tiempo se usó cada app hoy. |
| 🔒 Anti-desinstalación | Administrador de dispositivo: para desinstalar hay que desactivarlo primero. Botón atrás y recientes neutralizados en las pantallas infantiles. |
| 🚨 Seguridad | Las apps de emergencia y llamadas entrantes nunca se bloquean. |

## Estructura del proyecto

```
kidsguard-android/
├── app/src/main/java/com/kidsguard/app/
│   ├── KidsGuardApp.kt                  # Application: canal de notificaciones
│   ├── data/
│   │   ├── PreferencesManager.kt        # PIN, lista blanca, límites, uso diario
│   │   └── AppRepository.kt             # Apps instaladas / permitidas
│   ├── model/AppInfo.kt
│   ├── service/AppMonitorService.kt     # Servicio de vigilancia (foreground)
│   ├── receiver/
│   │   ├── BootReceiver.kt              # Arranque al encender el dispositivo
│   │   └── AdminReceiver.kt             # Administrador de dispositivo
│   ├── util/                            # Permisos, reglas horarias, motivos de bloqueo
│   └── ui/
│       ├── MainActivity.kt              # Entrada del adulto (pide PIN)
│       ├── launcher/KidsHomeActivity.kt # Launcher infantil (HOME)
│       ├── block/BlockedActivity.kt     # Pantalla de bloqueo
│       ├── pin/                         # Crear / introducir PIN
│       └── parent/                      # Panel, apps, límites, uso, permisos
└── app/src/main/res/                    # Layouts, strings (español), tema Material 3
```

## Cómo funciona el bloqueo

1. El adulto activa el **modo niños** en el panel (requiere PIN).
2. `AppMonitorService` corre en primer plano y consulta `UsageStatsManager`
   cada segundo para saber qué app está en pantalla.
3. Si la app **no está en la lista blanca**, o se agotó el **límite diario**,
   el **límite de esa app** o es **hora de dormir**, se lanza `BlockedActivity`
   por encima (permiso *mostrar sobre otras apps*) y el niño vuelve al
   launcher infantil.
4. Como KidsGuard es el launcher predeterminado, el botón HOME y el arranque
   del dispositivo siempre llevan a la pantalla infantil.

## Compilación

### Opción 1: Compilación automática con GitHub Actions ✅

Cada push a la rama `claude/android-parental-control-wit63o` dispara un workflow que:
- Compila versiones **Debug** y **Release** del APK
- Las sube como artifacts (descargables durante 30 días)

Ve a → **Actions** → **Build KidsGuard APK** → último run → **Artifacts**

### Opción 2: Compilación local

Requisitos: Android Studio (o SDK de Android 34) y JDK 17+.

```bash
cd kidsguard-android
./gradlew assembleDebug        # Debug APK
./gradlew assembleRelease      # Release APK (sin firmar)
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Especificaciones

- `minSdk 26` (Android 8.0) · `targetSdk 34` (Android 14) · Kotlin 1.9 · AGP 8.5

## Primer uso

1. Abre **KidsGuard** y crea el **PIN de adulto**.
2. Concede los permisos que pide la pantalla de configuración:
   - **Acceso a datos de uso** (detectar la app activa)
   - **Mostrar sobre otras apps** (pantalla de bloqueo)
   - **Notificaciones** (servicio activo)
   - **Administrador de dispositivo** (anti-desinstalación)
   - **Launcher predeterminado** (pantalla de inicio infantil)
3. En el panel: elige las **apps permitidas** y los **límites de tiempo**.
4. Activa el **modo niños** y entrega el dispositivo. 🎉

Para salir del modo niños: toca el candado 🔒 del launcher infantil e
introduce el PIN.

## Limitaciones conocidas

- En algunos fabricantes (Xiaomi, Huawei…) hay que permitir además el
  «inicio automático» de la app en los ajustes de batería.
- El panel de ajustes rápidos (barra de estado) no se puede bloquear por
  completo sin privilegios de *device owner*; el servicio bloquea la app de
  Ajustes en cuanto se abre.
- Si el niño conoce el PIN del adulto, toda la protección queda anulada:
  elige un PIN que no conozca.

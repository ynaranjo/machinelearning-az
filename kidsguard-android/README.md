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
- El bloqueo actual sondea la app en primer plano una vez por segundo
  (`AppMonitorService`), lo que deja una ventana de ~1s en la que una app no
  permitida es visible antes de bloquearse. Ver «Robustez del bloqueo» abajo.
- No hay sincronización entre dispositivos, backend, ni forma de administrar
  el dispositivo del hijo de forma remota: todo es local y manual en el
  propio dispositivo.

---

## 🚧 Hoja de ruta hacia una app completa, profesional y competitiva

Esta primera versión es un MVP funcional que cubre el caso de uso central
(lista blanca de apps + bloqueo + arranque automático). Para competir de
verdad con Kids Place, Google Family Link, Qustodio o FamiSafe, falta lo
siguiente:

### 1. Robustez del bloqueo (crítico)

- [ ] **Sustituir el polling por un `AccessibilityService`**: detecta el
  cambio de app en tiempo real (evento `TYPE_WINDOW_STATE_CHANGED`) en vez
  de sondear cada segundo — cierra la ventana de 1s en la que una app
  prohibida es visible.
- [ ] **Modo Device Owner vía aprovisionamiento QR** (`DevicePolicyManager` +
  NFC/QR en dispositivo recién reseteado): permite `LockTask` (kiosco real),
  ocultar la barra de estado, bloquear ajustes por completo y impedir
  factory reset — muy superior al modo *Device Admin* actual.
- [ ] **`LockTaskMode` / Screen Pinning** como alternativa sin *device
  owner* para reforzar el modo niños en apps individuales.
- [ ] Detectar y bloquear el **modo seguro (Safe Mode)**, que en muchos
  fabricantes permite saltarse apps de terceros.
- [ ] Reforzar contra desinstalación por ADB y contra revocar permisos
  especiales manualmente (detectar y re-solicitar, notificar al adulto).
- [ ] Persistir el estado del servicio con `WorkManager` (además de
  `START_STICKY`) para sobrevivir a *doze mode* y a fabricantes agresivos
  matando procesos en segundo plano (Xiaomi, Huawei, Samsung).

### 2. Funcionalidades que tienen Kids Place / Family Link y aquí faltan

- [ ] **Filtrado de contenido web** (navegador integrado con lista negra de
  dominios / SafeSearch forzado) — hoy solo se permite o bloquea el
  navegador entero.
- [ ] **Control de instalación/desinstalación de apps y compras** (bloquear
  Play Store o exigir PIN para instalar, desinstalar o comprar).
- [ ] **Múltiples perfiles de hijos** con distintas listas blancas, límites
  y horarios por perfil (hoy solo hay una configuración global).
- [ ] **Control y monitorización remota** desde el móvil del adulto (app
  complementaria o panel web) sin tener que tener el dispositivo del niño
  en la mano.
- [ ] **Notificaciones al adulto**: intento de abrir una app bloqueada,
  límite alcanzado, desinstalación intentada, dispositivo apagado/reiniciado.
- [ ] **Reportes de uso semanales/mensuales** con gráficos (no solo el día
  actual) y exportación/histórico persistente (hoy el uso se resetea cada
  día y no se conserva).
- [ ] **Geolocalización y geovallas** (zona segura / alerta de salida).
- [ ] **Modo "Escuela"** — perfil temporal más restrictivo activable por
  horario o con un toque, sin tocar la configuración normal.
- [ ] **Categorías de apps** (juegos, educativas, redes sociales) para
  aplicar reglas por categoría en vez de app por app.
- [ ] **Extensión de tiempo bajo petición**: el niño solicita más tiempo
  desde la pantalla de bloqueo y el adulto lo aprueba remotamente.

### 3. Seguridad y recuperación

- [ ] **Recuperación de PIN olvidado** (pregunta de seguridad, verificación
  por correo/SMS, o código maestro) — hoy si se olvida el PIN no hay forma
  de recuperar el acceso sin desinstalar.
- [ ] **Desbloqueo biométrico** (huella/rostro) como alternativa al PIN para
  el adulto.
- [ ] Cifrar la configuración sensible con `EncryptedSharedPreferences` /
  Jetpack Security en vez de `SharedPreferences` planas (el hash del PIN ya
  usa salt, pero el resto de datos va sin cifrar).
- [ ] Ofuscación y *minify* con R8 en el build de release
  (`isMinifyEnabled = true` está desactivado actualmente).
- [ ] Auditoría de seguridad (OWASP MASVS) antes de publicar.

### 4. Arquitectura y calidad del código

- [ ] Migrar a **MVVM** con `ViewModel` + `StateFlow`/`LiveData` en vez de
  lógica directa en las `Activity`.
- [ ] Inyección de dependencias con **Hilt** en vez de instanciar
  `PreferencesManager` manualmente en cada pantalla.
- [ ] Persistencia con **Room** para historial de uso, perfiles y reglas
  (las `SharedPreferences` con JSON manual no escalan a multi-perfil ni a
  histórico).
- [ ] **Corrutinas/Flow** para operaciones asíncronas en vez de bloquear el
  hilo principal al leer preferencias.
- [ ] Modularización (`:core`, `:data`, `:feature-launcher`,
  `:feature-parent`) si el proyecto crece.
- [ ] Tests: unitarios (`PreferencesManager`, `TimeRules`, evaluación de
  bloqueo), instrumentados (`AppMonitorService`) y de UI (Espresso) — hoy no
  hay ningún test.
- [ ] Linting automático (`ktlint`/`detekt`) integrado en CI.
- [ ] `isMinifyEnabled` + reglas ProGuard reales para el build de release.

### 5. UX / UI

- [ ] Asistente de configuración inicial (*onboarding wizard*) que guíe
  paso a paso en vez de pantallas sueltas.
- [ ] Tema oscuro completo y soporte de tablets (layouts adaptativos, hoy
  la cuadrícula es fija a 4 columnas).
- [ ] Accesibilidad: `contentDescription` completos, tamaños de fuente
  dinámicos, soporte TalkBack.
- [ ] Internacionalización real (`values-en`, `values-pt`…) — hoy todo el
  texto está *hardcodeado* en español.
- [ ] Animaciones y pulido visual del launcher infantil (hoy es una
  cuadrícula estática).
- [ ] Icono de app y branding definitivos (el icono actual es un
  placeholder vectorial simple).

### 6. CI/CD y distribución

- [ ] Firma de release real: keystore gestionado con **Play App Signing** o
  secrets de GitHub Actions (hoy el workflow genera un APK *sin firmar*).
- [ ] Publicación automatizada a **Play Store** (interna → cerrada →
  producción) con `fastlane` o el `google-github-actions/upload-google-play`.
- [ ] Versionado semántico automático y *changelog* por release.
- [ ] Reporte de cobertura de tests y *quality gate* en el pipeline.
- [ ] Escaneo de seguridad de dependencias (Dependabot / Snyk).

### 7. Cumplimiento legal y políticas de Google Play

- [ ] **Política de privacidad** pública (obligatoria para publicar).
- [ ] Cumplir la **Google Play Families Policy** si se distribuye como app
  familiar (requisitos extra de privacidad, anuncios, contenido).
- [ ] Formulario de **declaración de permisos especiales** en Play Console
  (`PACKAGE_USAGE_STATS`, `SYSTEM_ALERT_WINDOW`, Accessibility si se añade)
  — Google exige justificar cada uno o rechaza la publicación.
- [ ] Sección **Data Safety** completa (qué datos se recogen, dónde se
  almacenan, si se comparten).
- [ ] Revisar cumplimiento **COPPA** (EE. UU.) / **GDPR-K** (UE) si se
  recogen datos de menores, especialmente si se añade backend/nube.

### 8. Backend y sincronización (para ser realmente competitivo)

- [ ] Cuenta familiar en la nube (Firebase Auth o similar) para vincular
  el dispositivo del hijo con el del adulto.
- [ ] Sincronización de configuración/reportes vía Firestore o backend
  propio, con notificaciones push (FCM) en tiempo real al adulto.
- [ ] Panel web complementario para gestionar todo sin depender del móvil
  del niño.

### Priorización sugerida

1. **Crítico para que el bloqueo sea confiable**: `AccessibilityService` en
   vez de polling, cifrado de preferencias, recuperación de PIN.
2. **Crítico para publicar en Play Store**: firma de release, política de
   privacidad, formulario de permisos especiales, `isMinifyEnabled`.
3. **Diferenciador competitivo**: perfiles múltiples, control remoto,
   reportes históricos, notificaciones al adulto, filtrado web.
4. **Pulido**: MVVM/Hilt/Room, tests, i18n, tema oscuro, onboarding.

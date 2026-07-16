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
| ⚡ Bloqueo instantáneo *(v1.1)* | Servicio de accesibilidad que bloquea en el mismo instante en que la app pasa a primer plano, sin leer contenido de pantalla. El sondeo queda como respaldo. |
| 👆 Biometría *(v1.1)* | El adulto puede entrar al panel con huella/rostro además del PIN. |
| 🆘 Recuperación de PIN *(v1.1)* | Pregunta de seguridad para restablecer el PIN olvidado (respuesta guardada como hash con salt). |
| 🔔 Alertas al adulto *(v1.1)* | Notificación cuando el niño intenta abrir una app bloqueada o cuando se revoca un permiso de protección. |
| 🔐 Almacenamiento cifrado *(v1.1)* | Toda la configuración se guarda en `EncryptedSharedPreferences` (AES-256), con migración automática de datos previos. |
| ♻️ Watchdog *(v1.1)* | `WorkManager` revive el servicio de vigilancia cada 15 min si un fabricante agresivo con la batería lo mata. |
| 👧 Perfiles múltiples *(v1.2)* | Varios hijos en el mismo dispositivo, cada uno con sus apps permitidas, límites, horarios y uso. |
| 📈 Informe semanal *(v1.2)* | Historial de 30 días en base de datos local (Room): barras por día y top de apps de la semana, por perfil. |
| 🗂️ Categorías *(v1.2)* | La selección de apps se agrupa por categoría del sistema (juegos, vídeo, social…) para configurar más rápido. |

## Estructura del proyecto

```
kidsguard-android/
├── app/src/main/java/com/kidsguard/app/
│   ├── KidsGuardApp.kt                  # Application: canal de notificaciones
│   ├── data/
│   │   ├── PreferencesManager.kt        # PIN, lista blanca, límites, uso diario
│   │   └── AppRepository.kt             # Apps instaladas / permitidas
│   ├── model/AppInfo.kt
│   ├── service/
│   │   ├── AppBlockerAccessibilityService.kt  # Bloqueo instantáneo (v1.1)
│   │   ├── AppMonitorService.kt         # Sondeo de respaldo + contador de uso
│   │   └── ServiceWatchdogWorker.kt     # Watchdog con WorkManager (v1.1)
│   ├── receiver/
│   │   ├── BootReceiver.kt              # Arranque al encender el dispositivo
│   │   └── AdminReceiver.kt             # Administrador de dispositivo
│   ├── util/                            # Permisos, reglas horarias, BlockEvaluator,
│   │                                    # AdultNotifier (alertas al adulto)
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
- Si haces push de un tag `v*`, publica el APK como Release de GitHub

Ve a → **Actions** → **Build KidsGuard APK** → último run → **Artifacts**

#### Firma del APK de release (opcional pero recomendado)

Sin configurar nada, el release sale **sin firmar**. Para que el CI firme
el APK (instalable directamente):

1. Genera un keystore (guárdalo a buen recaudo; si lo pierdes no podrás
   actualizar la app instalada):

   ```bash
   keytool -genkeypair -v -keystore kidsguard.keystore \
     -alias kidsguard -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Codifícalo en base64: `base64 -w0 kidsguard.keystore`

3. En GitHub → **Settings → Secrets and variables → Actions**, crea:

   | Secret | Valor |
   |---|---|
   | `KIDSGUARD_KEYSTORE_BASE64` | El keystore en base64 (paso 2) |
   | `KIDSGUARD_KEYSTORE_PASSWORD` | Contraseña del keystore |
   | `KIDSGUARD_KEY_ALIAS` | `kidsguard` (o el alias que usaras) |
   | `KIDSGUARD_KEY_PASSWORD` | Contraseña de la clave |

   Para compilar firmado en local, exporta las mismas variables de entorno
   más `KIDSGUARD_KEYSTORE_FILE` con la ruta del keystore.

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

1. Abre **KidsGuard** y crea el **PIN de adulto** con su **pregunta de
   seguridad** (permite recuperar el PIN si se olvida).
2. Concede los permisos que pide la pantalla de configuración:
   - **Acceso a datos de uso** (detectar la app activa)
   - **Servicio de accesibilidad** (bloqueo instantáneo — recomendado)
   - **Mostrar sobre otras apps** (pantalla de bloqueo)
   - **Notificaciones** (servicio activo y alertas al adulto)
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
- Si el servicio de accesibilidad no está activado, el bloqueo recae en el
  sondeo de `AppMonitorService` (1 vez/segundo), que deja una ventana de ~1s
  en la que una app no permitida es visible antes de bloquearse. Con el
  servicio de accesibilidad activo (recomendado en la pantalla de permisos),
  el bloqueo es instantáneo.
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

- [x] **Sustituir el polling por un `AccessibilityService`** ✅ *(v1.1)*:
  `AppBlockerAccessibilityService` bloquea en tiempo real con
  `TYPE_WINDOW_STATE_CHANGED`; el sondeo queda como respaldo y contador de
  tiempo de uso.
- [ ] **Modo Device Owner vía aprovisionamiento QR** (`DevicePolicyManager` +
  NFC/QR en dispositivo recién reseteado): permite `LockTask` (kiosco real),
  ocultar la barra de estado, bloquear ajustes por completo y impedir
  factory reset — muy superior al modo *Device Admin* actual.
- [ ] **`LockTaskMode` / Screen Pinning** como alternativa sin *device
  owner* para reforzar el modo niños en apps individuales.
- [ ] Detectar y bloquear el **modo seguro (Safe Mode)**, que en muchos
  fabricantes permite saltarse apps de terceros.
- [x] Detectar la revocación manual de permisos especiales y **notificar al
  adulto** ✅ *(v1.1)*: el servicio comprueba cada minuto que datos de uso,
  superposición y accesibilidad sigan concedidos. *(Pendiente: reforzar
  contra desinstalación por ADB.)*
- [x] Persistir el estado del servicio con `WorkManager` ✅ *(v1.1)*:
  `ServiceWatchdogWorker` relanza el servicio cada 15 min si el modo niños
  está activo y el sistema lo mató (Xiaomi, Huawei, Samsung…).

### 2. Funcionalidades que tienen Kids Place / Family Link y aquí faltan

- [ ] **Filtrado de contenido web** (navegador integrado con lista negra de
  dominios / SafeSearch forzado) — hoy solo se permite o bloquea el
  navegador entero.
- [ ] **Control de instalación/desinstalación de apps y compras** (bloquear
  Play Store o exigir PIN para instalar, desinstalar o comprar).
- [x] **Múltiples perfiles de hijos** ✅ *(v1.2)*: perfiles con nombre y
  emoji, cada uno con su lista blanca, límites, horarios y uso propios;
  gestión completa desde el panel (crear, editar, eliminar, activar) y
  migración automática de la configuración anterior al primer perfil.
- [ ] **Control y monitorización remota** desde el móvil del adulto (app
  complementaria o panel web) sin tener que tener el dispositivo del niño
  en la mano.
- [x] **Notificaciones al adulto** ✅ *(v1.1, parcial)*: intento de abrir una
  app bloqueada y permisos de protección revocados, con cooldown anti-spam.
  *(Pendiente: desinstalación intentada, dispositivo apagado/reiniciado —
  requieren backend/push.)*
- [x] **Reportes de uso semanales e histórico persistente** ✅ *(v1.2,
  parcial)*: base de datos Room que conserva 30 días de uso por perfil e
  informe semanal con barras por día y top de apps. *(Pendiente: reportes
  mensuales y exportación.)*
- [ ] **Geolocalización y geovallas** (zona segura / alerta de salida).
- [ ] **Modo "Escuela"** — perfil temporal más restrictivo activable por
  horario o con un toque, sin tocar la configuración normal.
- [x] **Categorías de apps** ✅ *(v1.2, parcial)*: la selección de apps se
  agrupa por la categoría declarada por cada app (juegos, vídeo, social…).
  *(Pendiente: límites de tiempo por categoría.)*
- [ ] **Extensión de tiempo bajo petición**: el niño solicita más tiempo
  desde la pantalla de bloqueo y el adulto lo aprueba remotamente.

### 3. Seguridad y recuperación

- [x] **Recuperación de PIN olvidado** ✅ *(v1.1)*: pregunta de seguridad
  configurada junto al PIN; la respuesta se guarda como hash SHA-256 + salt.
  *(Pendiente: verificación por correo/SMS como segunda vía.)*
- [x] **Desbloqueo biométrico** ✅ *(v1.1)*: huella/rostro como alternativa
  al PIN (androidx.biometric), activable desde el panel.
- [x] Cifrar la configuración con `EncryptedSharedPreferences` ✅ *(v1.1)*:
  AES-256 vía Jetpack Security, con migración automática del almacén plano
  anterior y *fallback* si el dispositivo no soporta el keystore.
- [x] Ofuscación y *minify* con R8 en release ✅ *(v1.1)*:
  `isMinifyEnabled = true` + `shrinkResources`.
- [ ] Auditoría de seguridad (OWASP MASVS) antes de publicar.

### 4. Arquitectura y calidad del código

- [ ] Migrar a **MVVM** con `ViewModel` + `StateFlow`/`LiveData` en vez de
  lógica directa en las `Activity`.
- [ ] Inyección de dependencias con **Hilt** en vez de instanciar
  `PreferencesManager` manualmente en cada pantalla.
- [x] Persistencia con **Room** para el historial de uso ✅ *(v1.2,
  parcial)*: `daily_usage` por perfil con retención de 30 días y escritura
  por lotes fuera del hilo principal. *(Pendiente: migrar perfiles y reglas
  a Room; hoy viven en preferencias cifradas con claves por perfil.)*
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
- [x] Tema oscuro completo y cuadrícula adaptativa ✅ *(v1.3)*: paleta
  `values-night` para todas las pantallas y columnas del launcher según el
  ancho de pantalla (3–8, tablets incluidas). *(Pendiente: layouts
  específicos de tablet para el panel.)*
- [ ] Accesibilidad: `contentDescription` completos, tamaños de fuente
  dinámicos, soporte TalkBack.
- [x] Internacionalización ✅ *(v1.3, parcial)*: traducción completa al
  inglés (`values-en`, 120/120 cadenas); el español queda como idioma por
  defecto. *(Pendiente: más idiomas, p. ej. `values-pt`.)*
- [ ] Animaciones y pulido visual del launcher infantil (hoy es una
  cuadrícula estática).
- [ ] Icono de app y branding definitivos (el icono actual es un
  placeholder vectorial simple).

### 6. CI/CD y distribución

- [x] Firma de release con secrets de GitHub Actions ✅ *(v1.3)*: si los
  secrets del keystore están configurados, el CI produce un APK firmado
  (ver sección «Firma del APK de release»); sin ellos, sigue saliendo sin
  firmar. *(Pendiente: Play App Signing al publicar.)*
- [ ] Publicación automatizada a **Play Store** (interna → cerrada →
  producción) con `fastlane` o el `google-github-actions/upload-google-play`.
- [ ] Versionado semántico automático y *changelog* por release.
- [ ] Reporte de cobertura de tests y *quality gate* en el pipeline.
- [ ] Escaneo de seguridad de dependencias (Dependabot / Snyk).

### 7. Cumplimiento legal y políticas de Google Play

- [x] **Política de privacidad** ✅ *(v1.1, borrador)*: ver
  [`PRIVACY.md`](PRIVACY.md). *(Pendiente: alojarla en una URL pública y
  revisión legal antes de publicar.)*
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

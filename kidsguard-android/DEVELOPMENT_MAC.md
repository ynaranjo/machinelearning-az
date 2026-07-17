# 🍎 KidsGuard — Preparar un Mac para desarrollo y mantenimiento

Guía paso a paso para dejar un Mac (Intel o Apple Silicon M1/M2/M3/M4) listo
para desarrollar, compilar, probar y mantener KidsGuard.

**Versiones que usa el proyecto** (para instalar las correctas):

| Herramienta | Versión |
|---|---|
| JDK | **17** |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 1.9.24 |
| Gradle | 8.7 |
| Android SDK (compile/target) | **34** · minSdk 26 (Android 8.0) |
| detekt | 1.23.6 |
| Node.js (solo para el backend) | ≥ 18 |

> **Camino rápido**: instala **Android Studio** (Pasos 1–5) y ábrelo con la
> carpeta `kidsguard-android/`. Android Studio trae su propio JDK y Gradle, así
> que es la vía con menos fricción. El resto de pasos cubren la línea de
> comandos, la firma, el backend y el mantenimiento.

---

## Paso 1 · Herramientas de línea de comandos de Xcode (git)

Abre **Terminal** y ejecuta:

```bash
xcode-select --install
```

Acepta la ventana que aparece. Esto instala `git` y utilidades de compilación.
Comprueba:

```bash
git --version
```

---

## Paso 2 · Homebrew (gestor de paquetes)

Si no lo tienes:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

En **Apple Silicon**, añade Homebrew al PATH (el instalador te lo indica):

```bash
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

Comprueba: `brew --version`.

---

## Paso 3 · JDK 17

```bash
brew install --cask temurin@17
```

Configura `JAVA_HOME` en tu shell (`~/.zshrc`):

```bash
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc
java -version   # debe mostrar 17.x
```

---

## Paso 4 · Android Studio

```bash
brew install --cask android-studio
```

O descárgalo de https://developer.android.com/studio (elige el instalador de
**Apple Silicon** o **Intel** según tu Mac).

Ábrelo una vez y completa el asistente inicial: acepta las **licencias** y deja
que instale el SDK por defecto.

---

## Paso 5 · Componentes del Android SDK

En Android Studio: **Settings/Preferences → Languages & Frameworks → Android
SDK** (o el icono *SDK Manager*). Instala:

- Pestaña **SDK Platforms**: **Android 14 (API 34)**.
- Pestaña **SDK Tools**: **Android SDK Platform-Tools**, **Android SDK
  Build-Tools**, **Android Emulator** y (si lo quieres) **Android SDK
  Command-line Tools**.

Anota la ruta del SDK (arriba en esa pantalla), normalmente:
`/Users/TU_USUARIO/Library/Android/sdk`

Añade el SDK a tu shell (`~/.zshrc`):

```bash
echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator' >> ~/.zshrc
source ~/.zshrc
adb --version   # comprueba que platform-tools está en el PATH
```

---

## Paso 6 · Clonar el repositorio y situarse en la rama

```bash
cd ~/dev            # o donde guardes tus proyectos
git clone https://github.com/ynaranjo/machinelearning-az.git
cd machinelearning-az
git checkout claude/android-parental-control-wit63o
```

La app está en `kidsguard-android/` y el backend opcional en
`kidsguard-backend/`.

---

## Paso 7 · Abrir y compilar en Android Studio

1. **File → Open** y selecciona la carpeta **`kidsguard-android`** (no la raíz
   del repo).
2. Espera a que termine el *Gradle sync* (descarga dependencias; la primera vez
   tarda).
3. Compila con **Build → Make Project**, o genera el APK con **Build → Build
   Bundle(s)/APK(s) → Build APK(s)**.

Si el sync pide instalar alguna versión de SDK/Build-Tools, acepta.

---

## Paso 8 · Compilar desde la línea de comandos (opcional)

El proyecto **no incluye el Gradle wrapper** (`gradlew`). Tienes dos opciones:

### Opción A — Generar el wrapper una vez (recomendado para mantenimiento)

Instala Gradle y crea el wrapper para que a partir de entonces uses `./gradlew`:

```bash
brew install gradle
cd kidsguard-android
gradle wrapper --gradle-version 8.7
# a partir de aquí:
./gradlew assembleDebug
```

> Si generas el wrapper, considera **commitearlo** (`gradlew`, `gradlew.bat`,
> `gradle/wrapper/`) para que el CI y el resto del equipo usen la misma versión
> de Gradle sin instalar nada.

### Opción B — Usar Gradle del sistema

```bash
brew install gradle          # instala Gradle 8.x
cd kidsguard-android
gradle assembleDebug
```

Comandos útiles:

```bash
gradle testDebugUnitTest     # tests unitarios
gradle detekt                # análisis estático (no bloqueante)
gradle assembleDebug         # APK de depuración
gradle assembleRelease       # APK de release (sin firmar si no hay keystore)
gradle bundleRelease         # App Bundle .aab para Play (ver PUBLISHING.md)
```

---

## Paso 9 · Ejecutar la app en un emulador o dispositivo

### Emulador

1. En Android Studio: **Device Manager → Create device**.
2. Elige un teléfono (p. ej. Pixel 6), y una imagen del sistema **API 34**.
   - En **Apple Silicon**, elige la imagen **arm64-v8a** (más rápida).
3. Arranca el emulador y pulsa **Run ▶**.

### Dispositivo físico

1. En el teléfono: **Ajustes → Información → Número de compilación**, tócalo 7
   veces para activar **Opciones de desarrollador**.
2. Activa **Depuración USB**, conéctalo por cable y acepta la huella RSA.
3. `adb devices` debe listarlo; pulsa **Run ▶** o `adb install`.

> Muchas funciones de KidsGuard (accesibilidad, administrador de dispositivo,
> launcher, device owner) se prueban mejor en un **dispositivo físico** o en un
> emulador con Google APIs.

---

## Paso 10 · Firma para versiones de release (mantenimiento)

Para compilar release **firmado** desde el Mac, genera el keystore (una sola
vez) y exporta las variables antes de compilar:

```bash
keytool -genkeypair -v -keystore ~/keys/kidsguard-release.keystore \
  -alias kidsguard -keyalg RSA -keysize 2048 -validity 10000

export KIDSGUARD_KEYSTORE_FILE="$HOME/keys/kidsguard-release.keystore"
export KIDSGUARD_KEYSTORE_PASSWORD="tu-contraseña"
export KIDSGUARD_KEY_ALIAS="kidsguard"
export KIDSGUARD_KEY_PASSWORD="tu-contraseña"

cd kidsguard-android
gradle bundleRelease   # genera app/build/outputs/bundle/release/app-release.aab
```

El detalle completo (Play Console, declaraciones, subida) está en
[`PUBLISHING.md`](PUBLISHING.md). **Nunca** subas el keystore al repositorio.

---

## Paso 11 · Backend de control remoto (opcional)

Solo si vas a trabajar con la sincronización en la nube (`kidsguard-backend/`):

```bash
brew install node        # Node 18+ (o `brew install node@20`)
cd kidsguard-backend
npm install
ADMIN_PASSWORD="clave" FAMILY_CODE="MIFAMILIA" npm start
```

Detalles en [`../kidsguard-backend/README.md`](../kidsguard-backend/README.md).

---

## Paso 12 · Flujo de trabajo y mantenimiento

- **Rama de trabajo**: `claude/android-parental-control-wit63o` (o crea la tuya
  desde ella).
- **Subir versión**: al preparar una release, incrementa `versionCode` y
  `versionName` en `app/build.gradle.kts`.
- **CI**: cada push que toque `kidsguard-android/**` dispara el workflow **Build
  KidsGuard APK** (GitHub Actions), que ejecuta detekt, los tests y genera los
  APKs. Revisa que quede en verde antes de fusionar.
- **Antes de commitear**, en local:
  ```bash
  gradle detekt testDebugUnitTest assembleDebug
  ```
- **Traducciones**: si añades textos, mantén la paridad entre
  `res/values/strings.xml` (español) y `res/values-en/strings.xml` (inglés):
  ```bash
  cd kidsguard-android/app/src/main/res
  diff <(grep -o 'name="[^"]*"' values/strings.xml | sort) \
       <(grep -o 'name="[^"]*"' values-en/strings.xml | sort)
  ```

---

## Solución de problemas (macOS)

| Problema | Solución |
|---|---|
| `JAVA_HOME` no apunta a 17 | `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` |
| Gradle usa otro JDK | En Android Studio: **Settings → Build Tools → Gradle → Gradle JDK = 17**. |
| `adb: command not found` | Añade `$ANDROID_HOME/platform-tools` al PATH (Paso 5). |
| Emulador lento en Apple Silicon | Usa una imagen de sistema **arm64-v8a**, no x86. |
| «SDK location not found» | Crea `kidsguard-android/local.properties` con `sdk.dir=/Users/TU_USUARIO/Library/Android/sdk` (no se versiona). |
| Licencias del SDK sin aceptar | `sdkmanager --licenses` (desde `$ANDROID_HOME/cmdline-tools/latest/bin`). |
| Sync falla por red corporativa | Configura el proxy en **Settings → Appearance & Behavior → System Settings → HTTP Proxy**. |

---

## Resumen: de cero a compilando

```bash
xcode-select --install
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
brew install --cask temurin@17 android-studio
# abrir Android Studio, instalar SDK 34 + platform-tools + emulador
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
git clone https://github.com/ynaranjo/machinelearning-az.git
cd machinelearning-az && git checkout claude/android-parental-control-wit63o
# abrir kidsguard-android/ en Android Studio y pulsar Run ▶
```

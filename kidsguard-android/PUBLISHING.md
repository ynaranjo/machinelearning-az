# 🚀 KidsGuard — Guía completa de publicación en Google Play

Todos los pasos necesarios para llevar KidsGuard desde el código hasta estar
publicado en Google Play Store. Sigue el orden: cada bloque depende del
anterior.

> ⚠️ **Aviso importante para apps de control parental.** KidsGuard usa varios
> permisos **restringidos y sensibles** (servicio de accesibilidad, acceso a
> datos de uso, superposición, `QUERY_ALL_PACKAGES`, administrador de
> dispositivo). Google revisa estas apps con especial rigor y puede rechazar
> o retirar la app si no se justifican correctamente. Lee el **Paso 7** con
> atención: es la parte donde más publicaciones fallan.

---

## Resumen del recorrido

1. Requisitos previos (cuenta y herramientas)
2. Generar la clave de firma (keystore)
3. Configurar la firma en el proyecto
4. Compilar el **App Bundle** (`.aab`) firmado
5. Crear la app en Google Play Console
6. Ficha de Play Store (textos e imágenes)
7. **Declaraciones obligatorias** (permisos, Data Safety, audiencia, contenido)
8. Política de privacidad pública
9. Subir a un canal de pruebas → producción
10. Alternativas fuera de Play Store

---

## Paso 1 · Requisitos previos

- [ ] **Cuenta de Google Play Console** — pago único de **25 USD**:
  https://play.google.com/console/signup
- [ ] Verificación de identidad (y de organización, si publicas como empresa).
  Google exige D-U-N-S para cuentas de organización.
- [ ] **JDK 17+** y el **SDK de Android 34** (Android Studio recomendado).
- [ ] El código de KidsGuard (`kidsguard-android/`).

---

## Paso 2 · Generar la clave de firma (keystore)

La clave firma tus versiones. **Si la pierdes, no podrás volver a actualizar
la app**: guárdala con copias de seguridad seguras.

```bash
keytool -genkeypair -v \
  -keystore kidsguard-release.keystore \
  -alias kidsguard \
  -keyalg RSA -keysize 2048 -validity 10000
```

Te pedirá una contraseña y algunos datos. Anota:
- Ruta del archivo `kidsguard-release.keystore`
- Contraseña del keystore
- Alias (`kidsguard`)
- Contraseña de la clave

> Con **Play App Signing** (recomendado y activado por defecto en Play
> Console), esta clave es tu *clave de subida*: Google guarda la clave de
> firma final y tú solo firmas los envíos. Aun así, no pierdas la de subida.

---

## Paso 3 · Configurar la firma en el proyecto

El proyecto ya está preparado: lee la firma de **variables de entorno**. No
hay que tocar código. Exporta las variables antes de compilar:

```bash
export KIDSGUARD_KEYSTORE_FILE="/ruta/a/kidsguard-release.keystore"
export KIDSGUARD_KEYSTORE_PASSWORD="tu-contraseña-keystore"
export KIDSGUARD_KEY_ALIAS="kidsguard"
export KIDSGUARD_KEY_PASSWORD="tu-contraseña-clave"
```

- Si estas variables **existen**, el build de release sale **firmado**.
- Si **no existen**, sale sin firmar (útil solo para pruebas internas).

### (Opcional) Firmar automáticamente en GitHub Actions

En GitHub → **Settings → Secrets and variables → Actions**, crea:

| Secret | Valor |
|---|---|
| `KIDSGUARD_KEYSTORE_BASE64` | El keystore en base64: `base64 -w0 kidsguard-release.keystore` |
| `KIDSGUARD_KEYSTORE_PASSWORD` | Contraseña del keystore |
| `KIDSGUARD_KEY_ALIAS` | `kidsguard` |
| `KIDSGUARD_KEY_PASSWORD` | Contraseña de la clave |

El workflow los usa para producir un APK de release firmado (ver
`README.md`).

---

## Paso 4 · Compilar el App Bundle (`.aab`) firmado

**Google Play exige un Android App Bundle (`.aab`), no un APK.** El APK sirve
para instalación directa; para publicar necesitas el bundle:

```bash
cd kidsguard-android
# con las variables de firma del Paso 3 exportadas:
./gradlew bundleRelease
```

El archivo resultante estará en:

```
app/build/outputs/bundle/release/app-release.aab
```

Verifica antes de subir:
- [ ] `versionCode` incrementado respecto a la versión anterior
      (en `app/build.gradle.kts`; cada envío a Play debe subir este número).
- [ ] `versionName` actualizado (p. ej. `1.12.0`).
- [ ] El `.aab` está **firmado** (compilaste con las variables del Paso 3).

> 💡 Puedes añadir un paso `bundleRelease` al workflow de CI si quieres que el
> `.aab` se genere automáticamente; hoy el CI genera APKs.

---

## Paso 5 · Crear la app en Google Play Console

1. Entra en https://play.google.com/console → **Crear app**.
2. Rellena: nombre (**KidsGuard**), idioma predeterminado, tipo **App**,
   **Gratis** o de pago.
3. Acepta las declaraciones de políticas y de leyes de exportación de EE. UU.
4. Se crea la app y aparece el **panel de configuración** con una lista de
   tareas obligatorias (los pasos 6, 7 y 8 de esta guía).

---

## Paso 6 · Ficha de Play Store (textos e imágenes)

En **Crecimiento → Presencia en Play Store → Ficha de Store principal**:

**Textos**
- [ ] **Nombre de la app** (máx. 30 caracteres): `KidsGuard`
- [ ] **Descripción breve** (máx. 80): p. ej. *"Control parental: solo las
      apps que tú permitas, con límites de tiempo y filtrado web."*
- [ ] **Descripción completa** (máx. 4000): funciones (lista blanca de apps,
      launcher infantil, límites diarios y por app/categoría, hora de dormir,
      filtrado web, informes, modo kiosco, control remoto opcional).

**Recursos gráficos** (requisitos de Google)
- [ ] **Icono**: PNG 512×512, 32 bits con alfa.
- [ ] **Gráfico destacado** (*feature graphic*): 1024×500.
- [ ] **Capturas de teléfono**: mínimo **2** (recomendado 4–8), entre 320 px y
      3840 px de lado. Muestra el launcher infantil, el panel, los límites y
      el filtrado web.
- [ ] (Opcional) Capturas de tablet de 7" y 10" si soportas tablets.

---

## Paso 7 · Declaraciones obligatorias ⚠️ (lo más crítico)

Aquí es donde una app de control parental se juega la aprobación. Ve a
**Política → Contenido de la app** y completa **cada** sección.

### 7.1 · Permisos sensibles y restringidos

KidsGuard declara permisos que Google audita. Prepárate para justificar su uso
en el **formulario de declaración de permisos**:

| Permiso | Declaración necesaria |
|---|---|
| **Servicio de accesibilidad** (`BIND_ACCESSIBILITY_SERVICE`) | Google prohíbe el uso de accesibilidad salvo casos justificados. Debes explicar que se usa **para control parental: detectar la app en primer plano y bloquearla**, y grabar el vídeo de demostración que Play pide. La app ya declara `canRetrieveWindowContent="false"` (no lee la pantalla): recálcalo en la justificación. |
| **Acceso a datos de uso** (`PACKAGE_USAGE_STATS`) | Justifica que es para medir tiempo de uso y detectar la app activa. |
| **Mostrar sobre otras apps** (`SYSTEM_ALERT_WINDOW`) | Para la pantalla de bloqueo. |
| **`QUERY_ALL_PACKAGES`** | Requiere **formulario de declaración**: es necesario porque el adulto elige entre *todas* las apps instaladas cuáles permitir. Sin esta justificación, la app se rechaza. |
| **Administrador de dispositivo** (`BIND_DEVICE_ADMIN`) | Para dificultar la desinstalación. |
| **Servicio en primer plano de uso especial** (`FOREGROUND_SERVICE_SPECIAL_USE`) | En `targetSdk 34`, Google pide **justificar el subtipo**. La app declara `parental_control_app_monitoring`; explica la vigilancia continua del modo niños. |
| **Arranque al encender** (`RECEIVE_BOOT_COMPLETED`) | Reactivar la protección al reiniciar. |

> 🔴 Si no piensas usar el servicio de accesibilidad, podrías retirarlo para
> facilitar la aprobación; pero perderías el bloqueo instantáneo. Es una
> decisión de producto.

### 7.2 · Seguridad de los datos (Data Safety)

En **Contenido de la app → Seguridad de los datos**, declara con sinceridad:

- **Con la nube desactivada** (estado por defecto): la app **no recopila ni
  comparte datos**; todo se procesa en el dispositivo.
- **Si un usuario activa el control remoto** (opcional): se envían datos de uso
  y configuración **al servidor que el propio adulto aloja** (no a ti). Debes
  declararlo como recopilación opcional con esa finalidad.
- No se recoge ubicación, contactos, fotos, micrófono ni contenido de pantalla.

Basa las respuestas en [`PRIVACY.md`](PRIVACY.md).

### 7.3 · Audiencia y contenido (Familias)

- **Grupo de edad objetivo**: KidsGuard la instala y maneja **un adulto** para
  controlar el dispositivo de un menor. Selecciona el público con cuidado: si
  marcas que va dirigida a niños, entra en la **Política de Familias** de Play
  con requisitos extra (contenido, anuncios, SDKs). Normalmente una app de
  control parental se declara para **adultos/padres**, no para niños.
- **Clasificación de contenido**: completa el **cuestionario IARC**; KidsGuard
  no tiene contenido violento ni sexual → clasificación para todos los
  públicos.
- **Anuncios**: declara que **no** contiene anuncios.

### 7.4 · Otras secciones del panel

- [ ] **Categoría de la app**: *Herramientas* o *Estilo de vida* (parental).
- [ ] **Datos de contacto** del desarrollador (correo obligatorio).
- [ ] **App de gobierno / financiera / salud**: No.

---

## Paso 8 · Política de privacidad pública

Google **exige una URL pública** con la política de privacidad (más aún con
permisos sensibles).

1. Aloja el contenido de [`PRIVACY.md`](PRIVACY.md) en una URL accesible:
   - **GitHub Pages** (gratis): activa Pages en el repo y publica el `.md`.
   - O cualquier hosting / tu propia web.
2. Revisa el borrador antes de publicar: añade un **correo de contacto** real
   y, si activas la nube, la descripción del tratamiento (ya está redactada).
3. Pega esa URL en **Contenido de la app → Política de privacidad**.

> Recomendado: una revisión legal antes de publicar, especialmente por tratar
> con dispositivos de menores (COPPA en EE. UU., GDPR-K en la UE).

---

## Paso 9 · Subir a un canal de pruebas → producción

Google recomienda (y a veces exige) probar antes de producción.

1. Ve a **Probar y publicar → Pruebas → Internas** (la más rápida).
2. **Crear versión** → sube el `app-release.aab` del Paso 4.
3. Si es la primera versión, Play te propondrá activar **Play App Signing**:
   acéptalo.
4. Añade **notas de la versión** (qué incluye).
5. Añade correos de **testers** y comparte el enlace de prueba.
6. Cuando esté validado, **promociona** la versión: Internas → Cerradas →
   Abiertas → **Producción**.
7. En producción, envía a **revisión**. La primera revisión de una app con
   permisos sensibles puede tardar **varios días**.

**Requisito de cuentas nuevas**: desde 2023, las cuentas de desarrollador
personales creadas recientemente deben hacer una **prueba cerrada con al menos
12 testers durante 14 días** antes de poder publicar en producción. Tenlo en
cuenta en la planificación.

---

## Paso 10 · Alternativas fuera de Google Play

Si prefieres no pasar por Play Store (o mientras se aprueba):

- **APK directo**: distribuye el `app-release.apk` firmado (ver
  [`INSTALL.md`](INSTALL.md)). Ideal para uso personal/familiar.
- **F-Droid**: requiere que el proyecto sea de código abierto y build
  reproducible; su revisión es distinta a la de Play.
- **Otras tiendas** (Samsung Galaxy Store, Amazon Appstore, Aptoide): cada una
  con su proceso.

Para uso propio (tus hijos, tu dispositivo), el **APK directo** suele ser
suficiente y no requiere ninguno de los pasos de este documento salvo la firma
opcional.

---

## Checklist final antes de enviar a revisión

- [ ] `.aab` firmado con la clave de release y `versionCode` incrementado.
- [ ] Ficha completa: nombre, descripciones, icono, gráfico destacado y ≥2
      capturas.
- [ ] Formularios de permisos sensibles completados y justificados
      (accesibilidad, `QUERY_ALL_PACKAGES`, FGS special use).
- [ ] Seguridad de los datos rellenada conforme a `PRIVACY.md`.
- [ ] Clasificación de contenido (IARC) y audiencia declaradas.
- [ ] Política de privacidad en una **URL pública** y enlazada.
- [ ] Datos de contacto del desarrollador.
- [ ] Probado en un canal interno/cerrado antes de producción.

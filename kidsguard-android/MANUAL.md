# 📖 Manual de uso de KidsGuard

**Versión de la app:** 1.12 · **Idiomas:** Español e inglés · **Requiere:** Android 8.0+

KidsGuard convierte un móvil o tablet Android en un entorno seguro para niños:
solo se pueden usar las apps que un adulto autorice, con límites de tiempo,
horario de descanso, navegador filtrado y control remoto opcional.

---

## Índice

1. [Conceptos básicos](#1-conceptos-básicos)
2. [Primeros pasos](#2-primeros-pasos)
3. [La pantalla infantil (launcher)](#3-la-pantalla-infantil-launcher)
4. [El panel de control del adulto](#4-el-panel-de-control-del-adulto)
5. [Apps permitidas](#5-apps-permitidas)
6. [Límites de tiempo](#6-límites-de-tiempo)
7. [Navegador y filtrado web](#7-navegador-y-filtrado-web)
8. [Uso de hoy e informe semanal](#8-uso-de-hoy-e-informe-semanal)
9. [Perfiles de hijos](#9-perfiles-de-hijos)
10. [Conceder tiempo extra](#10-conceder-tiempo-extra)
11. [Control remoto (nube)](#11-control-remoto-nube)
12. [Seguridad: PIN, biometría y recuperación](#12-seguridad-pin-biometría-y-recuperación)
13. [Modo kiosco (protección máxima)](#13-modo-kiosco-protección-máxima)
14. [Alertas para el adulto](#14-alertas-para-el-adulto)
15. [Preguntas frecuentes](#15-preguntas-frecuentes)

---

## 1 · Conceptos básicos

| Concepto | Qué es |
|---|---|
| **Modo niños** | El estado de protección. Activado: el niño solo puede usar las apps permitidas y rigen los límites. Desactivado: el dispositivo funciona con normalidad. |
| **PIN de adulto** | Clave de 4–8 dígitos que protege todo el panel de control. Sin él (o sin tu huella), nadie puede cambiar la configuración ni salir del modo niños. |
| **Perfil** | Cada hijo tiene su propio perfil, con sus apps, límites, horarios y estadísticas. |
| **Launcher infantil** | La pantalla de inicio que ve el niño: una cuadrícula solo con sus apps permitidas. |
| **Pantalla de bloqueo** | Lo que aparece cuando el niño intenta abrir algo no permitido o se agota el tiempo. |

---

## 2 · Primeros pasos

La primera vez que abras KidsGuard, un asistente te guía en tres pasos:

### 2.1 Crear el PIN de adulto

1. Escribe un PIN de **4 a 8 dígitos** y confírmalo.
2. Escribe una **pregunta de seguridad** y su respuesta (p. ej. «nombre de mi
   primera mascota»). Sirve para recuperar el PIN si lo olvidas.

> 🔐 Elige un PIN que tu hijo no pueda adivinar (evita fechas de cumpleaños).

### 2.2 Conceder los permisos del sistema

La pantalla **Permisos del sistema** muestra cada permiso con ✅/❌ y un botón
**Conceder** que te lleva al ajuste exacto de Android:

| Permiso | Función | ¿Necesario? |
|---|---|---|
| Acceso a datos de uso | Detectar la app abierta para poder bloquearla | Imprescindible |
| Mostrar sobre otras apps | Enseñar la pantalla de bloqueo | Imprescindible |
| Servicio de accesibilidad | Bloqueo **instantáneo** al cambiar de app | Muy recomendado |
| Notificaciones | Servicio activo + alertas al adulto | Recomendado |
| Administrador de dispositivo | Impedir la desinstalación fácil | Recomendado |
| Launcher predeterminado | Pantalla de inicio infantil y arranque automático | Imprescindible |
| Modo kiosco (device owner) | Protección máxima (ver §13) | Opcional |

### 2.3 Elegir apps y activar

1. En el panel, entra en **📱 Apps permitidas** y marca las apps del niño.
2. Vuelve y activa el interruptor **Modo niños**.
3. Si Android pregunta por la app de inicio, elige **KidsGuard → Siempre**.

¡Listo! Entrega el dispositivo: el niño solo verá sus apps.

---

## 3 · La pantalla infantil (launcher)

Lo que ve el niño cuando el modo niños está activo:

- **Cuadrícula de apps permitidas** (se adapta a móvil y tablet). Tocar una app
  la abre con normalidad.
- **Reloj** y **nombre del perfil activo** arriba.
- **«Quedan X»**: tiempo de pantalla restante del día (si hay límite diario).
- **🌐 Navegador**: solo aparece si el adulto lo ha habilitado (ver §7).
- **🔒 Candado**: acceso para adultos. Pide el PIN (o huella) y abre el panel.

El botón «Atrás» no hace nada en esta pantalla y el botón «Inicio» siempre
vuelve aquí (si KidsGuard es el launcher predeterminado). Al encender el
dispositivo, esta pantalla aparece automáticamente.

### La pantalla de bloqueo

Si el niño abre algo no permitido o se agota un límite, aparece una pantalla a
color con el motivo:

| Icono | Motivo |
|---|---|
| 🚫 | App no permitida |
| ⏰ | Límite diario agotado |
| ⏳ | Límite de esa app agotado |
| 🗂️ | Límite de esa categoría agotado |
| 🌙 | Hora de dormir |

Desde ahí, el niño vuelve al inicio, o un adulto puede **conceder tiempo
extra** (ver §10).

---

## 4 · El panel de control del adulto

Se accede con el candado 🔒 + PIN (o huella). Contiene:

- **Interruptor Modo niños** — activa/desactiva toda la protección.
- **Desbloqueo con huella/rostro** — alternativa al PIN (si el equipo tiene
  biometría).
- **📱 Apps permitidas** · **⏱ Límites de tiempo** · **🌐 Navegador y filtrado
  web** · **📊 Uso de hoy** · **📈 Informe semanal** · **👧 Perfiles** ·
  **☁️ Control remoto** · **🔐 Permisos del sistema** · **🔑 Cambiar PIN** ·
  **🏠 Cambiar launcher predeterminado**.

El panel muestra siempre el **perfil activo**; todo lo que configures se aplica
a ese perfil.

---

## 5 · Apps permitidas

**Panel → 📱 Apps permitidas**

- La lista muestra **todas las apps instaladas agrupadas por categoría**
  (🎮 Juegos, 🎬 Vídeo, 💬 Social…).
- Marca la casilla de cada app que el niño **sí** puede usar. Todo lo demás
  queda bloqueado, incluidos los Ajustes de Android y la Play Store.
- Los cambios se guardan al instante y por perfil.

> 💡 Consejo: empieza permitiendo poco y amplía. Es más seguro que al revés.

---

## 6 · Límites de tiempo

**Panel → ⏱ Límites de tiempo**

### 6.1 Límite diario
Tiempo **total** de uso de apps al día (15–480 min con el deslizador). Al
agotarse, todas las apps se bloquean hasta mañana. El contador se reinicia
cada medianoche.

### 6.2 Hora de dormir
Franja en la que **todo** queda bloqueado (p. ej. 21:00 → 07:00; admite cruzar
la medianoche). Configura «Desde» y «Hasta» con el selector de hora.

### 6.3 Límites por categoría
Máximo diario por **tipo de app** (p. ej. Juegos 60 min). Suma el uso de todas
las apps de esa categoría. Toca una categoría y escribe los minutos (vacío =
sin límite).

### 6.4 Límites por app
En **📊 Uso de hoy**, toca cualquier app y fija sus minutos diarios (vacío =
sin límite).

> Los cuatro tipos de límite conviven: se aplica el primero que se agote.

---

## 7 · Navegador y filtrado web

**Panel → 🌐 Navegador y filtrado web**

1. Activa **Navegador infantil**: aparecerá el icono 🌐 en el launcher del niño.
2. Elige el modo:
   - **Solo sitios permitidos** (lista blanca): el niño únicamente puede abrir
     los dominios que añadas. Ideal para pequeños.
   - **Modo normal** (lista negra): puede navegar por todo **excepto** los
     dominios bloqueados.
3. Gestiona la lista con **Añadir dominio** (p. ej. `wikipedia.org`; cubre
   también sus subdominios) y el botón 🗑️ para quitar.

Siempre, en ambos modos:
- **SafeSearch forzado** en Google, Bing y DuckDuckGo.
- **YouTube en modo restringido**.
- Si el niño intenta abrir un sitio no permitido (aunque sea desde un enlace),
  ve una pantalla de «Página bloqueada».

> El filtro aplica al navegador infantil de KidsGuard. Si permites Chrome u
> otro navegador en «Apps permitidas», ese navegador NO está filtrado: para
> menores, deja como único navegador el de KidsGuard.

---

## 8 · Uso de hoy e informe semanal

- **📊 Uso de hoy**: tiempo total del día y desglose por app, ordenado de mayor
  a menor. Tocar una app permite fijar su límite diario.
- **📈 Informe semanal**: barras con el total de cada uno de los últimos 7 días
  y el **top 5 de apps** de la semana. Los datos se conservan 30 días en el
  dispositivo, por perfil.

---

## 9 · Perfiles de hijos

**Panel → 👧 Perfiles de hijos**

- Cada perfil (nombre + emoji) tiene **su propia** lista de apps, límites,
  horarios, filtro web y estadísticas.
- **Tocar un perfil lo activa**: el launcher y todas las reglas cambian a ese
  hijo al instante.
- ✏️ edita nombre/emoji · 🗑️ elimina (siempre debe quedar al menos uno).

Uso típico: una tablet compartida por dos hermanos → dos perfiles; al cambiar
de manos, el adulto entra con su PIN y activa el perfil correspondiente.

---

## 10 · Conceder tiempo extra

Cuando el niño se queda sin tiempo (límite diario, por app o por categoría), en
la pantalla de bloqueo aparece **«🔑 Más tiempo (solo adultos)»**:

1. Toca el botón e introduce el **PIN de adulto**.
2. Elige **15, 30 o 60 minutos**.
3. El tiempo extra **amplía todos los límites de hoy** para el perfil activo y
   el niño vuelve a la app al instante. Mañana todo vuelve a la normalidad.

También puede concederse **a distancia** desde el panel web (ver §11).

---

## 11 · Control remoto (nube)

**Opcional y desactivado por defecto.** Permite ver el uso y controlar el
dispositivo del niño desde un navegador, sin tocarlo.

Requiere montar el servidor incluido en el proyecto (`kidsguard-backend/`, ver
su README). Después, en **Panel → ☁️ Control remoto (nube)**:

1. Activa la sincronización.
2. Escribe la **URL del servidor** y el **código de familia**.
3. Pulsa **Emparejar dispositivo**.

Desde el **panel web** del adulto podrás:
- Ver cada dispositivo, su perfil activo y el uso de hoy por app.
- **Conceder tiempo extra** (15/30/60 min) a distancia.
- **Activar o desactivar el modo niños** a distancia.

El dispositivo sincroniza cada ~15 minutos (o al pulsar **Sincronizar ahora**).
Con la nube desactivada, **ningún dato sale del dispositivo**.

---

## 12 · Seguridad: PIN, biometría y recuperación

- **Cambiar el PIN**: Panel → 🔑 Cambiar PIN (puedes actualizar también la
  pregunta de seguridad).
- **Huella/rostro**: activa «Desbloqueo con huella/rostro» en el panel; la
  pantalla de PIN ofrecerá biometría automáticamente.
- **¿Olvidaste el PIN?**: en la pantalla de PIN, toca «¿Olvidaste el PIN?»,
  responde tu pregunta de seguridad y crea uno nuevo.
- Todo se guarda **cifrado (AES-256)** en el dispositivo; el PIN y la respuesta
  solo como hash irreversible.
- El **administrador de dispositivo** impide desinstalar KidsGuard sin
  desactivarlo antes (lo que exige pasar por Ajustes, que está bloqueado en
  modo niños).

---

## 13 · Modo kiosco (protección máxima)

Para el nivel más alto de protección, KidsGuard puede ser **propietario del
dispositivo** (*device owner*). Requiere un dispositivo recién restablecido de
fábrica, sin cuenta Google, y un ordenador con ADB:

```
adb shell dpm set-device-owner com.kidsguard.app/.receiver.AdminReceiver
```

Con el modo niños activo, el kiosco añade:
- El niño queda **encerrado** en las apps permitidas (LockTask): ningún gesto
  ni botón le saca.
- Launcher fijado por política del sistema (sin diálogos).
- **Barra de estado desactivada** (sin ajustes rápidos).
- **Modo seguro** y **restablecimiento de fábrica** bloqueados.

Todo se revierte al desactivar el modo niños. El estado se consulta en
**Permisos del sistema → Modo kiosco**.

---

## 14 · Alertas para el adulto

Si las notificaciones están activadas, recibirás avisos cuando:

- 🚫 El niño **intente abrir una app bloqueada** (máx. una vez cada 10 min por
  app).
- ⚠️ Se **revoque un permiso de protección** (datos de uso, superposición o
  accesibilidad) mientras el modo niños está activo — tócala para restaurarlo.

Además, una notificación fija «KidsGuard activo» confirma que la vigilancia
está funcionando.

---

## 15 · Preguntas frecuentes

**¿El niño puede cerrar KidsGuard?**
No. Si intenta salir, el bloqueo lo devuelve al launcher. Con el servicio de
accesibilidad, el bloqueo es instantáneo; con el modo kiosco, es total.

**¿Qué pasa si el dispositivo se reinicia?**
La protección se reactiva sola al arrancar y el launcher infantil aparece
automáticamente.

**¿Y si el sistema mata el servicio en segundo plano?**
Un vigilante lo relanza cada 15 minutos. En Xiaomi/Huawei/Samsung, permite
además el «inicio automático» de KidsGuard en los ajustes de batería.

**¿Puedo usar el dispositivo yo sin desinstalar nada?**
Sí: candado 🔒 → PIN → desactiva **Modo niños**. Cuando termines, actívalo de
nuevo.

**¿Las llamadas de emergencia funcionan?**
Sí. Las apps de emergencia y de llamadas entrantes nunca se bloquean.

**¿KidsGuard envía datos a algún sitio?**
No, salvo que actives tú el control remoto con **tu propio** servidor. Ver
[`PRIVACY.md`](PRIVACY.md).

**¿Cómo desinstalo KidsGuard?**
Desactiva el modo niños con tu PIN, desactiva el administrador de dispositivo
(Ajustes → Seguridad → Administradores) y desinstala como cualquier app. Si es
device owner, primero quita ese modo (`adb shell dpm remove-active-admin …`) o
restablece de fábrica.

---

*Consulta también: [Instalación](INSTALL.md) · [Desarrollo en Mac](DEVELOPMENT_MAC.md) · [Publicación](PUBLISHING.md) · [Privacidad](PRIVACY.md)*

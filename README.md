# 🪪 Mis Credenciales (PortaCredenciales para Android)

<p align="center">
  <img src="app/src/main/res/drawable/widget_preview.xml" width="160" alt="Mis Credenciales Logo" />
</p>

<p align="center">
  <strong>Organizador digital de credenciales, carnets e identificaciones fotográficas con widgets interactivos y redimensionables para tu pantalla de inicio.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.3-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-informational?style=for-the-badge" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-success?style=for-the-badge" alt="Target SDK" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License" />
</p>

---

## 📖 Descripción General

**Mis Credenciales** es una aplicación moderna y nativa para Android diseñada para que lleves siempre contigo fotografías de tus identificaciones oficiales, licencias de conducir, credenciales de estudiante, pases de trabajo y membresías.

A diferencia de guardar fotos sueltas en la galería, la app las organiza con el formato físico estándar de tarjetas (ISO/IEC 7810 ID-1), te permite capturar tanto el **anverso** como el **reverso**, y ofrece un **Widget para la pantalla de inicio completamente redimensionable** que puedes **voltear con un toque** para consultar la información trasera al instante sin abrir la aplicación.

---

## ✨ Características Principales

### 🪪 Gestión de Credenciales en Foto
* **Doble Cara (Anverso y Reverso)**: Captura con la cámara o importa desde tu galería el frente y la parte trasera de tus identificaciones.
* **Giro Tridimensional 3D (Flip)**: Animación fluida de volteo entre frente y reverso dentro de la app.
* **Visor HD con Zoom Táctil**: Modo pantalla completa con soporte para *pinch-to-zoom* y paneo libre sobre fondo negro inmersivo.
* **Control de Vigencia Automático**:
  * 🟢 **Vigente**: Faltan más de 30 días para su vencimiento.
  * 🟠 **Por Vencer**: Faltan 30 días o menos.
  * 🔴 **Vencida**: Fecha anterior al día de hoy.
* **Búsqueda Instantánea**: Encuentra credenciales al teclear su título, nombre del titular, folio o notas.
* **Compartir Rápido**: Envía la foto de tu credencial directamente por WhatsApp, Telegram o correo electrónico.

### 🧩 Widget Redimensionable Interactivo
* **Toca para Voltear (Frente ⇄ Reverso)**: Al presionar la foto de la credencial en el widget, la tarjeta se voltea de inmediato en tu pantalla de inicio para ver el reverso.
* **Cero Toques Accidentales**: Cuenta con un botón específico `[ 🔍 Abrir App ]` en la barra inferior para entrar a la aplicación únicamente cuando lo desees.
* **Completamente Redimensionable**: Soporta ajuste tanto en horizontal como en vertical (`android:resizeMode="horizontal|vertical"`), adaptándose a cualquier cuadrícula de tu lanzador (desde 2x1 hasta 4x4 o pantalla completa) sin distorsionar la imagen.
* **1 Toque para Fijar**: Pulsa el botón *"Fijar Widget"* desde la app para colocarlo en tu pantalla de inicio mediante la API nativa de Android (`requestPinAppWidget`).

### 🏷️ Categorías Organizadas
* 🪪 **Identificación**: INE, DNI, Cédula Profesional, Pasaporte.
* 🚗 **Licencias / Conducción**: Licencia de manejo, tarjeta de circulación.
* 🏢 **Laboral / Trabajo**: Gafetes de empleado, pases de acceso.
* 🎓 **Educación**: Carnet de estudiante, credencial de biblioteca.
* 🏥 **Salud y Seguros**: Carnet de Seguro Social, pólizas de gastos médicos.
* 🏋️ **Membresías**: Gimnasio, clubes, tarjetas de puntos.
* 📁 **Otros**: Cualquier otro documento en formato tarjeta.

### 🔒 Privacidad y Almacenamiento 100% Local
* Todas las fotos y datos se guardan estrictamente en el almacenamiento interno privado del dispositivo (`context.filesDir/credentials/`).
* Cero servidores externos, cero analíticas, cero rastreadores. Tus credenciales nunca salen de tu teléfono.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.3+ |
| **Interfaz (UI)** | Jetpack Compose + Material Design 3 |
| **Arquitectura** | Reactiva basada en Repositorio y `StateFlow` |
| **Navegación** | AndroidX Navigation 3 |
| **Almacenamiento** | Persistencia atómica JSON + Almacenamiento Interno Privado |
| **Widgets** | Android AppWidgetProvider + RemoteViews reactivos |
| **Captura de Cámara** | Activity Result Contracts + AndroidX FileProvider |

---

## 📁 Estructura del Proyecto

```
AplicacionAndroid/
├── .github/
│   └── workflows/
│       └── build.yml                        # CI/CD automático para compilar APK en GitHub Actions
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml              # Declaración de permisos, widgets y FileProvider
│   │   ├── java/com/example/credencialholder/
│   │   │   ├── MainActivity.kt              # Actividad principal y receptor de deep links
│   │   │   ├── Navigation.kt                # Navegación reactiva con Navigation 3
│   │   │   ├── NavigationKeys.kt            # Destinos y rutas serializables
│   │   │   ├── model/
│   │   │   │   ├── Credential.kt            # Entidad de credencial con serialización JSON
│   │   │   │   └── CredentialCategory.kt    # Categorías, iconos y paleta de colores
│   │   │   ├── data/
│   │   │   │   ├── CredentialRepository.kt  # Repositorio de datos con flujo reactivo
│   │   │   │   └── ImageStorageManager.kt   # Compresión, manejo EXIF y persistencia de fotos
│   │   │   ├── widget/
│   │   │   │   ├── CredentialWidgetProvider.kt            # Provider del widget interactivo
│   │   │   │   ├── CredentialWidgetConfigureActivity.kt   # Selector de tarjeta para el widget
│   │   │   │   ├── CredentialWidgetPinReceiver.kt         # Receptor de fijado en 1 toque
│   │   │   │   ├── WidgetPrefsManager.kt                  # Preferencias de widgets activos
│   │   │   │   └── WidgetPinHelper.kt                     # Integración con AppWidgetManager
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt                 # Paleta moderna (Azul Eléctrico, Slate, Índigo)
│   │   │   │   ├── Theme.kt                 # Tema dinámico Material 3 (Dark/Light)
│   │   │   │   └── Type.kt                  # Tipografía de la aplicación
│   │   │   └── ui/
│   │   │       ├── components/              # Tarjetas físicas, insignias de vigencia
│   │   │       ├── main/                    # Pantalla principal con filtros y buscador
│   │   │       ├── detail/                  # Vista de detalle, fijado de widget y compartir
│   │   │       ├── edit/                    # Formulario de alta/edición con cámara y galería
│   │   │       └── fullscreen/              # Visor HD con zoom táctil
│   │   └── res/                             # Recursos de interfaz (layouts, drawables, XML)
│   └── build.gradle.kts                     # Dependencias y configuración de compilación de app
├── gradle/
│   └── libs.versions.toml                   # Catálogo de versiones de dependencias (Version Catalog)
├── instalar_en_celular.ps1                  # Script de 1 comando para instalar directo en tu celular
├── .gitignore                               # Exclusiones profesionales de Git
├── LICENSE                                  # Licencia MIT
└── README.md                                # Documentación del proyecto
```

---

## 🚀 Compilación e Instalación

### Requisitos Previos
* **JDK 17 o 21**
* **Android SDK** con Plataforma 36 y Build-Tools 35/36.
* Teléfono Android con **Depuración por USB activada** (o un emulador de Android Studio).

### Compilar el APK con Gradle
En la terminal del proyecto:
```powershell
# En Windows (PowerShell)
.\gradlew assembleDebug

# En Linux / macOS
./gradlew assembleDebug
```

El archivo APK compilado se generará en:
`app/build/outputs/apk/debug/app-debug.apk`

### Instalación Rápida por USB
Puedes ejecutar el script incluido:
```powershell
.\instalar_en_celular.ps1
```
O directamente con `adb`:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

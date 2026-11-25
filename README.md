# Induztek Protocolos

Aplicación móvil Android para la **formalización inmediata de protocolos de prueba eléctrica en terreno** de Induztek Ingeniería SpA (caso DSY1105 – Desarrollo de Aplicaciones Móviles, Duoc UC).

Hoy los técnicos anotan las mediciones en papel y el cliente recibe el protocolo días después. La app permite programar visitas, registrar los protocolos en terreno con fotos de evidencia, formalizarlos, entregar al cliente un **Acta de prueba en terreno**, revisar el **historial comparable** de cada equipo y administrar usuarios e instrumentos. Todos los datos viven en los **microservicios Spring Boot** del proyecto, que la app consume con **Retrofit** a través de un API Gateway.

## Integrantes — Equipo 014

| Integrante | Usuario GitHub |
|---|---|
| Solange Hernandez | [@SolntHR](https://github.com/SolntHR) |
| Matias Mazzo | [@mmazzo1411](https://github.com/mmazzo1411) |

## Repositorios

| Repositorio | Contenido |
|---|---|
| [AppProtocolos_EQ014](https://github.com/SolntHR/AppProtocolos_EQ014) | Esta aplicación Android (Kotlin + Jetpack Compose) |
| [MS-Kotlin](https://github.com/SolntHR/MS-Kotlin) | Backend: Eureka, API Gateway y microservicios de usuarios, instalaciones, instrumentos y protocolos, cada uno con su base de datos, Swagger y JaCoCo |

## Funcionalidades implementadas

| Módulo | Descripción | Referencias del caso |
|---|---|---|
| Inicio de sesión | Login con validación por campo, contraseña cifrada (SHA-256 en la app y sal + SHA-256 en el servidor) y bloqueo de cuentas desactivadas. | RF-14 |
| Registro | Registro público con nombre, RUT, celular, correo personal y pregunta de seguridad. La cuenta queda pendiente hasta que un jefe técnico le asigna un perfil. | RF-22 |
| Recuperar contraseña | Por **código enviado al correo personal** (API de correo en ms-usuarios) **o** por **pregunta de seguridad**. Se ingresa siempre el correo de acceso. | — |
| Inicio por perfil | Accesos directos primero. Los accesos a **protocolos por formalizar** y **usuarios pendientes** se destacan en naranjo cuando hay algo que atender. | RF-13, RF-15, RN-18 |
| Visitas | Asignar visitas con ingeniero a cargo y técnicos en terreno (con buscador por nombre). Editar si están programadas o en curso; eliminar solo si están programadas. | RF-03 |
| Registro de protocolos | Formulario **por pasos** (visita y equipo, condiciones, un paso por cada prueba, observaciones y evidencia, resumen). Los campos faltantes se marcan en rojo. Resultado automático de cada prueba, solo instrumentos con calibración vigente y **fotos de evidencia** con cámara o galería. | RF-03 a RF-06, RF-11, RN-02, RN-04 a RN-11 |
| Protocolos | Lista con filtros por estado y detalle con formalizar, observar y anular. | RF-12, RF-13, RF-21, RN-16, RN-17 |
| Acta en terreno | Documento de la visita con equipos, pruebas y resultados; registro de quien recibe (nombre, RUT validado, cargo, fecha y hora) y cierre de la visita. | RF-07, RF-08, RN-13, RN-14 |
| Historial del equipo | Protocolos del más reciente al más antiguo y alerta de variación de aislación sobre 25 %. | RF-10, RF-11, RN-09, HU-11 |
| Instalaciones y equipos | Registro de instalaciones del cliente y de equipos con TAG único por instalación. | RF-01, RF-02, HU-01, RN-01 |
| Gestión de usuarios | Jefe técnico: personal y clientes, buscador, filtro por perfil, pendientes destacados, activar/desactivar, crear y editar usuarios y **cambiar perfiles confirmando con su contraseña**. | RF-22, RN-20 |
| Instrumentos | Estado de calibración (vigente, por vencer en 30 días, vencida) y registro de nuevas calibraciones. | RF-19, RN-11, RN-12 |
| Perfil | Datos del usuario, edición y foto de perfil con **cámara** o **galería**. | RF-14 |
| Notificaciones | Avisos locales al registrar, observar, formalizar o anular un protocolo y al cerrar una visita. | RF-07, RNF-02 |

## Requisitos de la evaluación

| Requisito | Cómo se cumple |
|---|---|
| Consumo de microservicios | Retrofit 3 + Gson contra el **API Gateway** (`data/remote`). Una sola URL base en `RetrofitClient.URL_GATEWAY`. |
| Manejo de respuestas | `LlamadaApi` traduce códigos HTTP a mensajes claros: `204` = lista vacía, `400` muestra el campo rechazado, `503` servicio no disponible y sin conexión. |
| API externa | Envío de correos (código de recuperación y activación de cuenta) desde ms-usuarios con SMTP. |
| Pruebas unitarias | JUnit 4 en `app/src/test` (ver tabla de pruebas). |
| Diseño visual con Material 3 | Tema propio (`ui/theme`) con paleta azul Induztek y acento naranjo para lo que requiere acción. |
| Formularios validados | Errores por campo en rojo; reglas en `util/Validaciones*.kt` y en los ViewModel. |
| Navegación | Navigation Compose con barra inferior por perfil y rutas con argumentos. |
| Gestión de estado | `StateFlow` + `UiState` por pantalla con `collectAsStateWithLifecycle`. |
| Recursos nativos | Cámara (permiso en tiempo de ejecución), galería y notificaciones locales (`POST_NOTIFICATIONS` en Android 13+). |
| Animaciones | `AnimatedVisibility`, `AnimatedContent`, `Crossfade` y transiciones entre pantallas. |
| Ícono y splash | Ícono adaptativo con el logo y splash con `core-splashscreen`. |

## Arquitectura

Patrón **MVVM** en un solo módulo:

```
app/src/main/java/com/example/appprotocolos_eq014/
├── data/
│   ├── remote/        RetrofitClient, interfaces *Api, modelos JSON y LlamadaApi
│   └── SesionPreferencias (sesión en SharedPreferences)
├── model/             Entidades, catálogos, reglas de negocio y UiState
├── repository/        Usuarios, visitas, protocolos, instrumentos, catálogo y foto de perfil
├── notificaciones/    Notificaciones locales
├── permisos/          Permisos en tiempo de ejecución
├── util/              Validaciones, formato y fechas
├── viewmodel/         ViewModels y AppViewModelFactory
└── ui/                Componentes, navegación, pantallas, estilos y tema
```

Flujo: **Pantalla → ViewModel → Repository → Retrofit → API Gateway (8090) → microservicio**.

Tecnologías: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lifecycle ViewModel, Retrofit 3, Gson, Coil y core-splashscreen.

## Cómo ejecutar

1. Levantar el backend siguiendo el README de [MS-Kotlin](https://github.com/SolntHR/MS-Kotlin) (Eureka, los cuatro microservicios y el API Gateway en el puerto 8090).
2. Encender el túnel persistente del equipo, que publica el puerto 8090 (instalar antes la herramienta con `winget install Microsoft.devtunnel`):
   ```
   devtunnel user login -g
   devtunnel host induztek-eq014
   ```
   Debe mostrar `https://fjfh1k06-8090.brs.devtunnels.ms`, la URL que ya está en `RetrofitClient.URL_GATEWAY`.
3. Si se usa otro túnel, cambiar `URL_GATEWAY` en `app/src/main/java/com/example/appprotocolos_eq014/data/remote/RetrofitClient.kt` (terminada en `/`).
4. Abrir el proyecto en **Android Studio** con el **Android SDK 37** y esperar la sincronización de Gradle.
5. Ejecutar en un emulador o celular con Android 7.0 (API 24) o superior con **Run ▶**.

## Usuarios de prueba

Los crea ms-usuarios al iniciar por primera vez. Todos usan la contraseña `Induztek#2026`.

| Perfil | Correo de acceso |
|---|---|
| Jefe técnico | `respinoza@induztek.ejemplo` |
| Ingeniero de pruebas | `acontreras@induztek.ejemplo` |
| Técnico de terreno | `mgodoy@induztek.ejemplo` |
| Cliente (Envasadora del Pacífico) | `mfuentes@envpacifico.ejemplo` |

- Recuperar por pregunta: con `mgodoy@induztek.ejemplo` la respuesta es `firulais`.
- Recuperar por correo: el código llega al **correo personal** de la cuenta. Los usuarios de prueba tienen correos ficticios, así que para probarlo registra una cuenta con tu correo real o cambia el correo personal de un usuario desde Gestión de usuarios.

## Pruebas unitarias

```
./gradlew test
```

| Archivo | Qué prueba | Pruebas |
|---|---|---|
| `util/ValidacionesRegistroTest` | Formulario de registro completo y vacío, RUT con dígito incorrecto y teléfonos con o sin +56 | 4 |
| `util/ValidacionesProtocoloTest` | Borrador válido y registro sin temperatura que vuelve al paso Condiciones | 2 |
| `util/ValidacionesVisitaTest` | Visita completa válida y OT mal escrita sin técnicos | 2 |
| `util/ValidacionesCatalogoTest` | Equipo completo válido y equipo sin TAG con tensión no numérica | 2 |
| `model/InstrumentoTest` | Calibración por vencer (10 días) y vencida o sin fecha | 2 |
| `viewmodel/LoginViewModelTest` | Login con campos vacíos y con datos completos | 2 |
| `viewmodel/RegistroViewModelTest` | Registro incompleto que no se envía y cambio de pregunta de seguridad | 2 |
| `repository/UsuarioRepositoryTest` | Cambio de perfil: envía el jefe y su contraseña cifrada | 1 |
| `repository/RecuperacionCorreoRepositoryTest` | Solicitar, verificar y restablecer con código; recuperación por pregunta | 4 |

## Entregables

- APK y AAB **firmados** en modo release, junto al archivo de firma **JKS**. La contraseña del JKS no se guarda en el repositorio.

## Planificación

Tablero de tareas en Trello: [Desarrollo de Apps Móviles](https://trello.com/b/7FjqPLFk/desarrollo-de-apps-m%C3%B3viles)

> Todos los datos del proyecto son ficticios y fueron creados con fines académicos.
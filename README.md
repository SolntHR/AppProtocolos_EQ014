# Induztek Protocolos

Aplicación móvil Android para la **formalización inmediata de protocolos de prueba eléctrica en terreno** de Induztek Ingeniería SpA (caso DSY1105 – Desarrollo de Aplicaciones Móviles, Duoc UC).

Hoy los técnicos anotan las mediciones en papel y el cliente recibe el protocolo días después. La app permite consultar visitas, entregar al cliente un **Acta de prueba en terreno** al cerrar la visita, revisar el **historial comparable** de cada equipo y administrar usuarios e instrumentos, todo con **persistencia local** para trabajar sin conexión.

## Integrantes — Equipo 014

| Integrante | Usuario GitHub |
|---|---|
| Solange Hernandez | [@SolntHR](https://github.com/SolntHR) |
| Matias Mazzo | [@mmazzo1411](https://github.com/mmazzo1411) |

## Funcionalidades implementadas

| Módulo | Descripción | Referencias del caso |
|---|---|---|
| Inicio de sesión | Login con validación de correo y contraseña, mensajes por campo y bloqueo de cuentas desactivadas. | RF-14 |
| Recuperar contraseña | Flujo en tres pasos: correo, pregunta de seguridad y nueva contraseña con confirmación. | — |
| Inicio por perfil | Técnico/ingeniero: visitas asignadas con avance. Cliente: sus equipos con búsqueda por TAG. Jefe técnico: gestión de usuarios. | RF-13, RF-15, RN-18 |
| Acta en terreno | Documento de la visita con equipos, pruebas, mediciones y resultados, marcado "Pendiente de formalización". Registro de quien recibe (nombre, RUT validado, cargo, fecha y hora) y cierre de la visita. | RF-07, RF-08, RN-13, RN-14 |
| Historial del equipo | Protocolos del más reciente al más antiguo, comparación de la lectura de aislación y alerta de variación significativa sobre 25 %. | RF-10, RF-11, RN-09, HU-11 |
| Perfil | Datos del usuario, foto de perfil con **cámara** o **galería** y cierre de sesión. | RF-14 |
| Editar perfil | Formulario con validaciones; RUT, perfil y correo protegidos según el rol. | RN-03 |
| Gestión de usuarios | Jefe técnico: listado de personal y clientes, activar o desactivar cuentas, agregar y editar usuarios. | RF-22, RN-20 |
| Instrumentos | Jefe técnico: estado de calibración (vigente, por vencer en 30 días, vencida) y registro de nuevas calibraciones. | RF-19, RN-11, RN-12 |

## Requisitos de la evaluación

| Requisito | Cómo se cumple |
|---|---|
| Diseño visual con Material 3 | Tema propio (`ui/theme`) con paleta, tipografía y componentes Material 3. |
| Formularios validados | Login, recuperar contraseña, editar perfil, ficha de usuario, acta y calibración, con error por campo e íconos. |
| Validación desacoplada | Reglas en `util/Validaciones.kt` y en los ViewModel; las pantallas solo muestran el estado. |
| Navegación funcional | Navigation Compose con barra inferior por rol, rutas con argumentos y transiciones. |
| Gestión de estado | `StateFlow` + `UiState` por pantalla, recolectado con `collectAsStateWithLifecycle`. |
| Almacenamiento local | SQLite (`InduztekDbHelper`, 23 tablas basadas en el MER) y `SharedPreferences` para la sesión; fotos en almacenamiento interno. |
| Recursos nativos | **Cámara** (con solicitud de permiso en tiempo de ejecución) y **galería** (selector de fotos del sistema). |
| Animaciones | Transiciones entre pantallas, `AnimatedVisibility`, `AnimatedContent`, `Crossfade`, colores y progreso animados. |

## Arquitectura

Patrón **MVVM** en un solo módulo, organizado por capas:

```
app/src/main/java/com/example/appprotocolos_eq014/
├── data/          InduztekDbHelper (SQLite) y SesionPreferencias
├── model/         Data classes, enums y UiState de cada pantalla
├── repository/    Acceso a datos: usuarios, protocolos, visitas, instrumentos, fotos
├── util/          Validaciones y Formato (fechas y números)
├── viewmodel/     ViewModels y AppViewModelFactory
└── ui/
    ├── components/  Componentes reutilizables (campos, botones, etiquetas, barras)
    ├── navigation/  AppNavegacion y barra inferior
    ├── screens/     Pantallas por funcionalidad
    ├── styles/      Estilos de botones, campos y tarjetas
    └── theme/       Colores, tipografía, dimensiones y tema
```

Tecnologías: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lifecycle ViewModel, SQLite.

## Cómo ejecutar

1. Instalar **Android Studio** (versión estable reciente) con el **Android SDK 37**.
2. Clonar el repositorio:
   ```bash
   git clone https://github.com/SolntHR/AppProtocolos_EQ014.git
   ```
3. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
4. Crear o conectar un dispositivo (emulador con API 24 o superior, o un celular con depuración USB).
5. Presionar **Run ▶**.

La base de datos se crea con datos de ejemplo del Anexo 9 al primer inicio de sesión.

## Usuarios de prueba

Todos usan la contraseña `123456`.

| Perfil | Correo |
|---|---|
| Jefe técnico | `respinoza@induztek.ejemplo` |
| Ingeniero de pruebas | `acontreras@induztek.ejemplo` |
| Técnico de terreno | `mgodoy@induztek.ejemplo` |
| Cliente (Envasadora del Pacífico) | `mfuentes@envpacifico.ejemplo` |

Para recuperar la contraseña de `mgodoy@induztek.ejemplo`, la respuesta de seguridad es `firulais`.

## Planificación

Tablero de tareas en Trello: [Desarrollo de Apps Móviles](https://trello.com/b/7FjqPLFk/desarrollo-de-apps-m%C3%B3viles)

> Todos los datos del proyecto son ficticios y fueron creados con fines académicos.

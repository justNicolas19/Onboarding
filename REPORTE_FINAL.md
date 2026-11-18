# OnboardGo: cruce Figma ↔ backend (versión final)

Fuentes:

- **Backend**: `backend.txt` (Spring Boot `com.example.onboarding`). Se completó con lo que pedía `REPORTE.md`; el detalle está en `backend/docs/CAMBIOS.md`.
- **Figma**: `onboarding.png` y `onboarding.fig` (≈130 frames, 23 filas). Colores medidos sobre la exportación; la mascota y el logo se recortaron de ella.
- **REPORTE.md**: la conexión entre ambos y la lista de lo que faltaba.

Entregables:

| Carpeta | Contenido |
|---|---|
| `backend/` | Proyecto Maven listo para IntelliJ. Compila y pasa 8 pruebas de integración (H2). |
| `android/` | Proyecto Gradle listo para Android Studio (Kotlin, Compose, MVVM, Retrofit). |

Convenciones:

- ✅ Todo lo que muestra la pantalla sale del backend.
- ✅ **nuevo**: el respaldo se agregó al backend en esta entrega (antes era ⛔ o 🟡 en `REPORTE.md`).
- 🟡 Funciona con el backend, con una diferencia frente al Figma que se explica.

Casi todas las listas tienen cuatro variantes (normal, cargando, vacío y error). Se implementan una vez con `PantallaConEstado` y cuentan como una pantalla.

## 1. Tabla de cruce

### Acceso

| Pantalla del Figma | Respaldo en el backend | Estado | Código (app) |
|---|---|---|---|
| Splash | Lee la sesión guardada (`AuthResponse.expiraEn`) | ✅ | `ui/auth/AuthScreens.kt` · `SplashScreen` |
| Iniciar sesión | `POST /api/auth/login` (`LoginRequest` → `AuthResponse`), `JwtService`, `JwtAuthenticationFilter` | ✅ nuevo | `LoginScreen`, `SesionViewModel`, `IniciarSesionUseCase` |
| Error de acceso | 401 con `mensaje: "Correo o contraseña incorrectos"`, `codigo: CREDENCIALES_INVALIDAS` | ✅ nuevo | `LoginScreen` (estado de error) |
| Acceso de manager · Acceso de administrador | Mismo login; la app entra al rol que devuelve `AuthResponse.rol` | ✅ nuevo | `LoginScreen(perfil)` |
| "¿Problemas para acceder?" | `POST /api/auth/recuperar` (siempre 202) | ✅ nuevo | `SesionViewModel.recuperarAcceso` |
| Seleccionar cuenta | No necesita backend: llena el correo de las cuentas demo (`onboarding.demo.enabled=true`) | ✅ | `SeleccionarCuentaScreen` |

### Empleado

| Pantalla del Figma | Respaldo en el backend | Estado | Código (app) |
|---|---|---|---|
| Mi ruta (camino, avance, refuerzo + 3 estados) | `GET /api/progreso/mi-ruta`; nuevos `rutaNombre`, `unidad`, `unidadTitulo`, `tipo` | ✅ nuevo | `ui/empleado/RutaScreens.kt` · `MiRutaScreen` |
| Mi progreso (14 %, 29 %, refuerzo + 3 estados) | `mi-ruta` con `tipo` en el progreso | ✅ nuevo | `MiProgresoScreen` |
| Normativas · lectura / completado / evaluación, Cultura · video, Herramientas · evaluación | `mi-ruta` (`descripcion`, `duracionMinutos`, `calificacion`, `puntajeMinimo`) | ✅ nuevo | `DetalleModuloScreen` |
| Abrir módulo · Cultura de la empresa (Moodle) | `POST /api/progreso/modulos/{id}/abrir`; `urlMoodle`; `evaluacionModuloId` ("Ir a la evaluación") | ✅ nuevo | `AbrirModuloScreen` |
| Módulo completado | `POST /api/progreso/avance` → `COMPLETADO`; `puntosXp` ("+300 XP"); siguiente módulo desbloqueado | ✅ nuevo | `ResultadoAvanceScreen` |
| Módulo en refuerzo | `POST /api/progreso/avance` → `EN_REFUERZO` | ✅ | `ResultadoAvanceScreen` |
| Calificación pendiente (×2) | 400 con `codigo: CALIFICACION_PENDIENTE` | ✅ | `ResultadoAvanceScreen` |
| Tema bloqueado | `EstadoProgreso.BLOQUEADO` + `orden` | ✅ | `TemaBloqueadoScreen` |
| Sesiones de seguimiento (+3 estados) | `GET /api/sesiones/mis-sesiones`; nuevos `asunto`, `duracionMinutos`, `fechaFin` | ✅ nuevo | `ui/empleado/EmpleadoScreens.kt` · `SesionesEmpleadoScreen` |
| Detalle de sesión (programada, cancelada, realizada, verificada) | `GET /api/sesiones/{id}`; nuevos `managerCargo`, `descripcion`, `espacioNombre` | ✅ nuevo | `DetalleSesionEmpleadoScreen` |
| Mis reservas (+3 estados) | `GET /api/reservas/mias` (ahora incluye las reservas de sus sesiones); `espacioCapacidad`, `sesionManagerNombre` | ✅ nuevo | `MisReservasScreen` |
| Mis alertas (+3 estados) | `GET /api/alertas/mias`; nuevos `titulo`, `activa` | ✅ nuevo | `ui/compartido/AlertasScreen.kt` |
| Mis líderes (+3 estados) | `GET /api/lideres` (`LiderResponse`); "Agendar una sesión" → `POST /api/sesiones/solicitudes` | ✅ nuevo | `MisLideresScreen`, `LideresViewModel` |
| Tu líder (×5) | `GET /api/lideres` (`presentacion`, `area`, `cargo`) + próxima sesión de `mis-sesiones` | 🟡 nuevo: "Enviar mensaje" abre el correo (ver §3) | `TuLiderScreen` |
| Mi perfil | `GET /api/usuarios/me`, `GET/PUT /api/usuarios/me/preferencias` | ✅ nuevo | `ui/perfil/PerfilScreens.kt` · `PerfilScreen` |
| Subir foto | `POST /api/usuarios/me/foto` (JPG o PNG, máximo 5 MB) | ✅ nuevo | `SubirFotoScreen` |

### Manager

| Pantalla del Figma | Respaldo en el backend | Estado | Código (app) |
|---|---|---|---|
| Mi equipo (+3 estados) | `GET /api/progreso/equipo` (`sesionesRequeridas`), área de `GET /api/usuarios/me` | ✅ nuevo | `ui/manager/ManagerScreens.kt` · `MiEquipoScreen` |
| Detalle del empleado (+3 estados) | `GET /api/progreso/empleados/{id}` + resumen (`rutaNombre`) | ✅ | `DetalleEmpleadoScreen` |
| Agenda (programada, cancelada, realizada, verificada + 3 estados) | `GET /api/sesiones/mi-agenda` con `asunto` y `duracionMinutos` | ✅ nuevo | `AgendaScreen` |
| Agendar sesión | `POST /api/sesiones`, `PUT /api/sesiones/{id}` con `duracionMinutos` (hora de fin), `asunto`, `descripcion`; `GET /api/usuarios/mi-equipo`, `GET /api/espacios` | 🟡 nuevo: el enlace de Teams es de solo lectura (lo genera el backend) | `AgendarSesionScreen` |
| Detalle de sesión (manager) | `GET /api/sesiones/{id}`, `POST .../cancelar`, `PUT` | ✅ nuevo | `DetalleSesionManagerScreen` |
| Adjuntar soporte | `POST .../realizar`, `POST .../soporte` con `observaciones` | ✅ nuevo | `AdjuntarSoporteScreen` |
| Sesión verificada · Sesión cancelada | Respuesta de `/soporte` (`fechaVerificacion`) y `/cancelar` | 🟡 "Se notificó la cancelación al empleado" se cambió: no hay notificaciones | `DetalleSesionManagerScreen` |
| Espacios disponibles (+3 estados) | `GET /api/espacios` con `ubicacion` | ✅ nuevo | `EspaciosDisponiblesScreen` |
| Reservar espacio | `GET /api/reservas/espacio/{id}/franjas?fecha=` (horario configurable), `POST /api/reservas` | ✅ nuevo | `ReservarEspacioScreen`, `ReservaViewModel` |
| Reserva confirmada | `ReservaResponse` (`espacioCapacidad`, `reservadoPorNombre`, `reservadoPorArea`) | ✅ nuevo | `ReservaConfirmadaScreen` |
| Alertas del equipo (+3 estados) | `GET /api/alertas/mi-equipo` | ✅ | `AlertasScreen(delEquipo = true)` |
| Mi perfil · Subir foto | Igual que el empleado | ✅ nuevo | `PerfilScreens.kt` |

### Administrador

| Pantalla del Figma | Respaldo en el backend | Estado | Código (app) |
|---|---|---|---|
| Usuarios (todos, filtro por rol + 3 estados) | `GET /api/usuarios` | ✅ | `ui/admin/AdminScreens.kt` · `UsuariosScreen` |
| Filtrar usuarios | Enum `Rol` (filtro en el cliente) | ✅ | `FiltrarUsuariosScreen` |
| Crear usuario · Editar usuario | `POST /api/usuarios`, `PUT /api/usuarios/{id}` (`idMoodle` ya vuelve y no se borra), `GET /api/rutas`; nuevos `area`, `presentacion` | ✅ nuevo | `UsuarioFormScreen` |
| Usuario desactivado | `DELETE /api/usuarios/{id}`; el filtro JWT corta el acceso de inmediato | ✅ | `UsuarioDesactivadoScreen` |
| Rutas y módulos (+3 estados) | `GET /api/rutas?incluirInactivas=true` (borradores), `empleadosAsignados` | ✅ nuevo | `RutasScreen` |
| Detalle de ruta (+3 estados) | `GET /api/rutas/{id}` con `idMoodle` en `ModuloResponse` | ✅ nuevo | `DetalleRutaScreen` |
| Crear ruta | `POST /api/rutas` | 🟡 "Versión" es de solo lectura: la asigna el backend | `CrearRutaScreen` |
| Editar módulo (×3) / Añadir módulo | `POST /api/modulos`, `PUT /api/modulos/{id}` (`idMoodle` precargado, más `duracionMinutos`, `puntosXp`, `unidad`, `unidadTitulo`, `evaluacionId`) | ✅ nuevo | `ModuloFormScreen` |
| Tipo de módulo | Enum `TipoModulo` | ✅ | `TipoModuloScreen` |
| Espacios físicos (+3 estados) | `GET /api/espacios?incluirInactivos=true` con `ubicacion` | ✅ nuevo | `EspaciosAdminScreen` |
| Crear espacio · Editar espacio (×3) | `POST /api/espacios`, `PUT /api/espacios/{id}` (desactivar = `activo: false`) | ✅ | `EspacioFormScreen` |
| Auditoría (página 1, página 2 + 3 estados) | `GET /api/auditoria`, `GET /api/auditoria/usuario/{id}` (de 20 en 20) | ✅ | `AuditoriaScreen` |
| Mi perfil · Subir foto | Igual que el empleado | ✅ nuevo | `PerfilScreens.kt` |

## 2. PANTALLAS SIN BACKEND (en el `backend.txt` original) y lo que se agregó

Ninguna pantalla quedó con datos de ejemplo. Estas eran las que no tenían lógica en el TXT; todo lo de la columna derecha ya está implementado en `backend/` y conectado en la app.

### 2.1 Iniciar sesión, Error de acceso, Acceso de manager, Acceso de administrador

- **Datos y acciones**: correo y contraseña; token, expiración, id, nombre y rol; "Correo o contraseña incorrectos"; "¿Problemas para acceder?".
- **Agregado**:
  - Endpoints: `POST /api/auth/login` (público, `LoginRequest` → `AuthResponse`) y `POST /api/auth/recuperar` (`RecuperarContrasenaRequest` → 202 con `MensajeSimpleResponse`).
  - Seguridad: `JwtService` (jjwt, HS256, `onboarding.jwt.secret`), `JwtAuthenticationFilter` (id como principal, `ROLE_<rol>`, rechaza cuentas inactivas en cada petición), `/api/auth/**` público.
  - Reglas: rechaza `activo = false`; registra `INICIO_SESION`; `CredencialesInvalidasException` → 401.
  - Integración: `CorreoClient` + `CorreoClientSimulado` (como Teams y Moodle).
  - App: `IniciarSesionUseCase` guarda el token en `TokenStore`; `AuthInterceptor` lo envía y vuelve al login ante un 401.

### 2.2 Seleccionar cuenta

- **Datos y acciones**: elegir una de las tres cuentas del prototipo.
- **Agregado**: nada en los endpoints. `DatosInicialesConfig` crea esas cuentas con `onboarding.demo.enabled=true`; la pantalla solo llena el correo y el login es real.

### 2.3 Mis líderes y Tu líder

- **Datos y acciones**: manager del empleado (nombre, cargo, área), líderes por área, presentación ("Sobre Elena"), próxima sesión, "Agendar una sesión", "Enviar mensaje".
- **Agregado**:
  - Modelo: `Usuario.area`, `Usuario.presentacion`.
  - DTO: `LiderResponse(id, nombre, correo, cargo, area, presentacion, fotoUrl, esMiManager)`.
  - Endpoints: `GET /api/lideres` (EMPLEADO) y `GET /api/usuarios/me`.
  - "Agendar una sesión": entidad `SolicitudSesion`, `SolicitudSesionRequest(fechaPropuesta, comentario)`, `POST /api/sesiones/solicitudes` (EMPLEADO), `GET /api/sesiones/solicitudes` (MANAGER). Al agendar, las pendientes quedan `ATENDIDA`.
  - "Enviar mensaje": entidad `Mensaje`, `MensajeRequest(destinatarioId, texto)`, `POST /api/mensajes`, `GET /api/mensajes/recibidos`.
  - Casos de uso: `ListarLideresUseCase`, `ObtenerMiManagerUseCase`, `SolicitarSesionUseCase`, `EnviarMensajeUseCase`.

### 2.4 Mi perfil (los tres roles)

- **Datos y acciones**: nombre, cargo, manager, rol, "Datos personales", "Recordatorios de reuniones", "Avisos de progreso", "Notificaciones", "Idioma".
- **Agregado**: `GET /api/usuarios/me`; entidad `PreferenciasUsuario`; `PreferenciasRequest`/`PreferenciasResponse`; `GET/PUT /api/usuarios/me/preferencias`; casos de uso `ObtenerMiPerfilUseCase` y `ActualizarPreferenciasUseCase`.

### 2.5 Subir foto

- **Datos y acciones**: imagen de la galería o de la cámara (JPG o PNG, máximo 5 MB) que se ve en el perfil.
- **Agregado**: `Usuario.fotoUrl` y `UsuarioResponse.fotoUrl`; `POST /api/usuarios/me/foto` (multipart `archivo`) con validación de tipo y tamaño; `GET /api/archivos/fotos/{nombre}`; `AlmacenamientoArchivos` + `AlmacenamientoLocal`; caso de uso `SubirFotoPerfilUseCase` (la app convierte HEIC/WebP a JPG).

## 3. Diferencias que quedan frente al Figma

| Dónde | Qué pasa | Por qué |
|---|---|---|
| Tu líder · "Enviar mensaje" | Abre el correo con la dirección del líder | El Figma no tiene pantalla para escribir el mensaje. `POST /api/mensajes` está listo para cuando exista |
| Agendar sesión · "Enlace de Teams" | De solo lectura ("Se genera al guardar") | Lo crea `TeamsClient` en el backend |
| Sesión cancelada | "La sesión quedó cancelada y su espacio reservado se liberó." | No hay notificaciones al empleado |
| Crear ruta · "Versión" | De solo lectura y entera (1, 2, 3...) | El backend la controla y sube en cada `PUT` |
| Mi progreso | Al final, accesos a Sesiones, Mis reservas y Mis alertas | El Figma marca esas pantallas con la pestaña Progreso pero no dibuja cómo llegar |
| Mi perfil | Fila "Cerrar sesión" al final; "Privacidad" sin subtítulo | El Figma corta la lista en "Privacidad" |
| Formularios | Campos extra al final (área, presentación, duración, XP, unidad, evaluación asociada, asunto, descripción, ubicación) | Son los campos nuevos del backend; el Figma corta esos formularios |
| Tipografía | Fuente del sistema en lugar de Nunito | Se decidió no incluir el archivo de la fuente |

## 4. LÓGICA SIN PANTALLA

Está en el backend y en la capa de datos de la app (`OnboardingApi`, repositorios, ViewModels), pero el Figma no tiene dónde mostrarla:

| Lógica | Qué falta en el Figma |
|---|---|
| `DELETE /api/modulos/{id}` (falla si el módulo tiene avances) | Botón "Eliminar módulo". `RutaRepository.eliminarModulo` ya existe |
| `PUT /api/rutas/{id}` (sube la versión; activar un borrador) | Pantalla "Editar ruta". `RutaRepository.actualizar` ya existe |
| `GET /api/sesiones/solicitudes` (MANAGER) | Lista de solicitudes de sesión del equipo |
| `POST /api/mensajes`, `GET /api/mensajes/recibidos` | Pantallas para escribir y leer mensajes |
| `POST /api/reservas` para EMPLEADO | El empleado solo ve "Mis reservas" |
| `GET /api/reservas/mias` para MANAGER | No hay "Mis reservas" del manager |
| `ReservaRequest.sesionId` fuera del agendamiento | Vincular una reserva a una sesión ya creada |
| `GET /api/rutas/{rutaId}/modulos`, `GET /api/espacios/{id}`, `GET /api/usuarios/{id}`, `GET /api/reservas/espacio/{id}?desde&hasta` | Redundantes con lo que ya cargan las pantallas; quedan en los repositorios |
| `AlertaRezagoService.generar` (motor de IA) | Es interno; no necesita pantalla |
| `PLANILLA_EXPORTADA`, `findByEmpleadoIdAndEstadoOrderByFechaHoraAsc` (RF12) | Exportar la planilla de cumplimiento |
| `findByEntidadAndEntidadId`, `findByFechaHoraBetween` | Filtros de auditoría por entidad y por fechas |
| Máximo de 10 sesiones (`MAX_SESIONES`) | Solo aparece como mensaje de error (`codigo: MAX_SESIONES`) |

## 5. Verificación

| Qué | Cómo | Resultado |
|---|---|---|
| Backend compila | `mvn compile` con JDK 21 y Spring Boot 4.1.2 | ✅ |
| Pruebas del backend | `mvn test`: contexto + 7 pruebas de integración con H2 y datos demo (login, error de acceso, JWT, cuenta desactivada, ruta y avance, líderes, perfil, foto, franjas, solicitudes, borradores e inactivos, arreglo de `idMoodle`, auditoría) | ✅ 8 de 8 |
| Pruebas de red de la app | `CapaDeRedTest` (MockWebServer): DTOs, fechas ISO, `codigo` de error, 401, 204 | ✅ 6 de 6 |
| App contra el backend real | El backend corriendo (H2 + demo) y los repositorios, DTOs y casos de uso de la app llamando 43 endpoints de los tres roles | ✅ 43 de 43 |
| Código de la app (Kotlin + Compose) | Compilado con Kotlin 2.1.0 y Compose Multiplatform 1.7.3 (mismas APIs de Compose 1.7 / Material 3 1.3), con stubs solo para las clases de Android (`Context`, `Intent`, `R`, `BuildConfig`...) | ✅ 0 errores, 0 advertencias |
| Compilación Android con AGP | No se hizo: requiere aceptar la licencia del Android SDK y se decidió no aceptarla | ⚠️ La primera compilación con AGP será en Android Studio |

Los íconos de Material usados se comprobaron contra `material-icons-extended` 1.7.6.

## 6. Pendiente antes de producción

1. **Rotar la contraseña de Supabase** que estaba en `application-local.properties` del repositorio original.
2. Definir `JWT_SECRET` y `ADMIN_CONTRASENA` en el servidor.
3. Reemplazar los clientes simulados (`MoodleClientSimulado`, `TeamsClientSimulado`, `CorreoClientSimulado`) por las integraciones reales; mientras tanto Moodle siempre devuelve 100.
4. Servir el backend por HTTPS y cambiar `BASE_URL` en la app.

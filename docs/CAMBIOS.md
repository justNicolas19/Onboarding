# Cambios respecto al backend.txt

Todo sale del `REPORTE.md`. Las columnas nuevas son nullable para que `ddl-auto=update` pueda agregarlas sobre tablas con datos; los valores por defecto se aplican en el código.

## Huecos que bloqueaban la app (sección 2 del reporte)

| # | Hueco | Qué se hizo |
|---|---|---|
| 1 | No había login ni JWT | `AuthController` (`POST /api/auth/login`, `POST /api/auth/recuperar`), `JwtService` (jjwt 0.12, HS256), `JwtAuthenticationFilter` (id del usuario como principal, `ROLE_<rol>` como authority; lee rol y estado de la base, así un usuario desactivado pierde el acceso de inmediato). `/api/auth/**` es público. El login registra `INICIO_SESION` y rechaza cuentas inactivas. |
| 2 | Entidades sin constructor vacío | Todas usan `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` (sin `@Data`). |
| 3 | Credenciales en el repositorio | Se quitó `application-local.properties`; queda `application-local.properties.example` y `.gitignore`. **Hay que rotar la contraseña de Supabase.** |
| 4 | `idMoodle` se escribía pero no se leía | `ModuloResponse` y `UsuarioResponse` lo devuelven. Al editar un usuario, `idMoodle: null` conserva el actual y `""` lo borra. |
| 5 | Rutas y espacios inactivos no se listaban | `GET /api/rutas?incluirInactivas=true` y `GET /api/espacios?incluirInactivos=true` (solo administrador). |
| 6 | No existía "mi usuario" | `GET /api/usuarios/me` para cualquier rol. |

## Pantallas sin backend (sección 3)

| Pantalla | Backend nuevo |
|---|---|
| Iniciar sesión, Error de acceso, Acceso de manager/administrador | `POST /api/auth/login` → `AuthResponse`; error 401 con `mensaje: "Correo o contraseña incorrectos"` y `codigo: CREDENCIALES_INVALIDAS`. "¿Problemas para acceder?" → `POST /api/auth/recuperar` (siempre 202; `CorreoClient` simulado como Teams y Moodle). |
| Mis líderes, Tu líder | `Usuario.area`, `Usuario.presentacion`; `LiderResponse(id, nombre, correo, cargo, area, presentacion, fotoUrl, esMiManager)`; `GET /api/lideres` (EMPLEADO). "Agendar una sesión" → `SolicitudSesion`, `POST /api/sesiones/solicitudes` (EMPLEADO) y `GET /api/sesiones/solicitudes` (MANAGER); al agendar, las solicitudes pendientes quedan `ATENDIDA`. "Enviar mensaje" → `Mensaje`, `POST /api/mensajes`, `GET /api/mensajes/recibidos`. |
| Mi perfil | `GET /api/usuarios/me`; `PreferenciasUsuario` con `GET/PUT /api/usuarios/me/preferencias`. |
| Subir foto | `Usuario.fotoUrl`; `POST /api/usuarios/me/foto` (multipart `archivo`, JPG o PNG, máximo 5 MB); `GET /api/archivos/fotos/{nombre}` (público, nombres UUID). |

## Campos sin respaldo (sección 4)

| Dónde | Campo nuevo |
|---|---|
| Mi progreso, Detalle de módulo | `ProgresoModuloResponse`: `tipo`, `descripcion`, `duracionMinutos` (nuevo en `Modulo`) |
| Abrir módulo | `urlMoodle` (plantilla `onboarding.moodle.url-actividad`) y `evaluacionModuloId` (`Modulo.evaluacion`) |
| Mi ruta | `unidad`, `unidadTitulo`, `rutaId`, `rutaNombre` |
| Módulo completado | `puntosXp` |
| Sesiones, Agenda | `asunto`, `descripcion`, `duracionMinutos` en entidad, request y response (por defecto 60, el valor fijo anterior) |
| Agendar sesión | Hora de fin → `duracionMinutos`. El enlace de Teams lo sigue generando `TeamsClient` (la app lo muestra de solo lectura) |
| Adjuntar soporte | `observaciones` y `fechaVerificacion` |
| Detalle de sesión | `managerCargo`, `empleadoCargo`, `espacioId`, `espacioNombre`, `fechaFin` |
| Mi equipo | `Usuario.area` (vía `/me`); `ResumenAvanceResponse`: `sesionesRequeridas` (5), `empleadoCargo`, `rutaNombre` |
| Espacios | `EspacioFisico.ubicacion` |
| Mis alertas | `AlertaRezago.titulo` y `activa` (últimos 7 días) |
| Detalle de ruta, Editar módulo | `idMoodle` en `ModuloResponse` |
| Rutas y módulos | `RutaResponse.empleadosAsignados` |
| Reservar espacio | `GET /api/reservas/espacio/{id}/franjas?fecha=` con horario configurable (`onboarding.reservas.*`) |
| Mis reservas | `ReservaResponse`: `espacioCapacidad`, `reservadoPorNombre`, `reservadoPorArea`, `sesionAsunto`, `sesionManagerNombre`; `/mias` incluye las reservas de las sesiones donde el usuario es el empleado |

## Otros

- `ErrorResponse.codigo`: código estable para la app (`CALIFICACION_PENDIENTE`, `MODULO_NO_ABIERTO`, `MODULO_BLOQUEADO`, `SIN_ID_MOODLE`, `ESPACIO_OCUPADO`, `MAX_SESIONES`, `SESION_FUTURA`, `SOLICITUD_PENDIENTE`, `SIN_MANAGER`, `FORMATO_NO_PERMITIDO`, `ARCHIVO_GRANDE`).
- `AccionAuditoria`: `SESION_SOLICITADA`, `MENSAJE_ENVIADO`, `PERFIL_ACTUALIZADO`.
- `DatosInicialesConfig`: crea el primer administrador y, con `onboarding.demo.enabled=true`, las cuentas del prototipo.
- Un administrador ya no puede desactivarse a sí mismo.

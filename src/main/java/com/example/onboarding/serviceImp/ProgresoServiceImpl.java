package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.ReportarAvanceRequest;
import com.example.onboarding.dto.response.ProgresoModuloResponse;
import com.example.onboarding.dto.response.ResumenAvanceResponse;
import com.example.onboarding.entity.Modulo;
import com.example.onboarding.entity.ProgresoModulo;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.EstadoProgreso;
import com.example.onboarding.enums.EstadoSesion;
import com.example.onboarding.exception.AccesoDenegadoException;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.integration.MoodleClient;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.AlertaRezagoRepository;
import com.example.onboarding.repository.ModuloRepository;
import com.example.onboarding.repository.ProgresoModuloRepository;
import com.example.onboarding.repository.SesionSeguimientoRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.ProgresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgresoServiceImpl implements ProgresoService {


    private static final int DIAS_ALERTA_RECIENTE = 7;

    private final ProgresoModuloRepository progresoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final SesionSeguimientoRepository sesionRepository;
    private final AlertaRezagoRepository alertaRepository;
    private final MoodleClient moodleClient;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public void sincronizarRuta(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        if (usuario.getRuta() == null) {
            return;
        }
        List<Modulo> modulos = moduloRepository.findByRutaIdOrderByOrdenAsc(usuario.getRuta().getId());
        Map<Long, ProgresoModulo> existentes = progresoRepository.findByUsuarioIdOrderByModuloOrdenAsc(usuarioId)
                .stream()
                .collect(Collectors.toMap(p -> p.getModulo().getId(), Function.identity(), (a, b) -> a));

        List<ProgresoModulo> nuevos = new ArrayList<>();
        boolean anteriorCompletado = true;
        for (Modulo modulo : modulos) {
            ProgresoModulo progreso = existentes.get(modulo.getId());
            if (progreso == null) {
                progreso = ProgresoModulo.builder()
                        .usuario(usuario)
                        .modulo(modulo)
                        .estado(anteriorCompletado ? EstadoProgreso.DISPONIBLE : EstadoProgreso.BLOQUEADO)
                        .build();
                nuevos.add(progreso);
            }
            anteriorCompletado = progreso.getEstado() == EstadoProgreso.COMPLETADO;
        }
        progresoRepository.saveAll(nuevos);
    }

    @Override
    @Transactional
    public List<ProgresoModuloResponse> listarRuta(Long usuarioId) {
        sincronizarRuta(usuarioId);
        Usuario usuario = buscarUsuario(usuarioId);
        return progresoDeRutaActual(usuario).stream().map(mapper::toProgresoResponse).toList();
    }

    @Override
    @Transactional
    public List<ProgresoModuloResponse> listarRutaDeEmpleado(Long managerId, Long empleadoId) {
        Usuario empleado = buscarUsuario(empleadoId);
        if (empleado.getManager() == null || !empleado.getManager().getId().equals(managerId)) {
            throw new AccesoDenegadoException("El empleado no está a tu cargo");
        }
        return listarRuta(empleadoId);
    }

    @Override
    @Transactional
    public ProgresoModuloResponse abrirModulo(Long usuarioId, Long moduloId) {
        ProgresoModulo progreso = obtenerProgreso(usuarioId, moduloId);

        if (progreso.getEstado() == EstadoProgreso.BLOQUEADO) {
            throw new ReglaNegocioException("Debes completar el módulo anterior para abrir este");
        }
        if (progreso.getEstado() == EstadoProgreso.DISPONIBLE || progreso.getEstado() == EstadoProgreso.EN_REFUERZO) {
            progreso.setEstado(EstadoProgreso.EN_CURSO);
            if (progreso.getFechaInicio() == null) {
                progreso.setFechaInicio(LocalDateTime.now());
            }
            progreso = progresoRepository.save(progreso);
        }
        auditoriaService.registrar(usuarioId, AccionAuditoria.MODULO_ABIERTO, "ProgresoModulo", progreso.getId(),
                "Módulo abierto: " + progreso.getModulo().getTitulo());
        return mapper.toProgresoResponse(progreso);
    }

    @Override
    @Transactional
    public ProgresoModuloResponse reportarAvance(Long usuarioId, ReportarAvanceRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);
        ProgresoModulo progreso = obtenerProgreso(usuarioId, request.moduloId());

        if (progreso.getEstado() != EstadoProgreso.EN_CURSO) {
            throw new ReglaNegocioException("Debes abrir el módulo antes de reportar el avance");
        }
        if (usuario.getIdMoodle() == null || usuario.getIdMoodle().isBlank()) {
            throw new ReglaNegocioException("El usuario no tiene un identificador de Moodle asignado");
        }

        Modulo modulo = progreso.getModulo();
        Double nota = moodleClient.consultarCalificacion(usuario.getIdMoodle(), modulo.getIdMoodle());
        if (nota == null) {
            throw new ReglaNegocioException("Moodle aún no reporta una calificación para este módulo");
        }
        progreso.setCalificacion(nota);

        if (nota >= modulo.getPuntajeMinimo()) {
            progreso.setEstado(EstadoProgreso.COMPLETADO);
            progreso.setFechaFin(LocalDateTime.now());
            progreso = progresoRepository.save(progreso);
            desbloquearSiguiente(usuario, modulo);
            auditoriaService.registrar(usuarioId, AccionAuditoria.MODULO_COMPLETADO, "ProgresoModulo",
                    progreso.getId(), "Módulo completado: " + modulo.getTitulo() + " (nota " + nota + ")");
        } else {
            progreso.setEstado(EstadoProgreso.EN_REFUERZO);
            progreso = progresoRepository.save(progreso);
            auditoriaService.registrar(usuarioId, AccionAuditoria.MODULO_EN_REFUERZO, "ProgresoModulo",
                    progreso.getId(), "Módulo en refuerzo: " + modulo.getTitulo() + " (nota " + nota + ")");
        }
        return mapper.toProgresoResponse(progreso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenAvanceResponse> resumenEquipo(Long managerId) {
        LocalDateTime desde = LocalDateTime.now().minusDays(DIAS_ALERTA_RECIENTE);
        List<ResumenAvanceResponse> resumen = new ArrayList<>();

        for (Usuario empleado : usuarioRepository.findByManagerIdAndActivoTrue(managerId)) {
            List<ProgresoModulo> progresos = progresoDeRutaActual(empleado);
            int total = progresos.size();
            int completados = (int) progresos.stream()
                    .filter(p -> p.getEstado() == EstadoProgreso.COMPLETADO).count();
            double porcentaje = total == 0 ? 0 : Math.round(completados * 1000.0 / total) / 10.0;

            OptionalDouble promedio = progresos.stream()
                    .filter(p -> p.getCalificacion() != null)
                    .mapToDouble(ProgresoModulo::getCalificacion)
                    .average();
            int sesionesVerificadas = (int) sesionRepository
                    .countByEmpleadoIdAndEstado(empleado.getId(), EstadoSesion.VERIFICADA);

            resumen.add(new ResumenAvanceResponse(
                    empleado.getId(), empleado.getNombre(), total, completados, porcentaje,
                    promedio.isPresent() ? Double.valueOf(promedio.getAsDouble()) : null,
                    sesionesVerificadas,
                    alertaRepository.existsByEmpleadoIdAndFechaAfter(empleado.getId(), desde)));
        }
        return resumen;
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    /** Solo los módulos de la ruta actual; si el usuario cambió de ruta, el avance anterior se ignora. */
    private List<ProgresoModulo> progresoDeRutaActual(Usuario usuario) {
        if (usuario.getRuta() == null) {
            return List.of();
        }
        Long rutaId = usuario.getRuta().getId();
        return progresoRepository.findByUsuarioIdOrderByModuloOrdenAsc(usuario.getId()).stream()
                .filter(p -> p.getModulo().getRuta().getId().equals(rutaId))
                .toList();
    }

    private ProgresoModulo obtenerProgreso(Long usuarioId, Long moduloId) {
        sincronizarRuta(usuarioId);
        Usuario usuario = buscarUsuario(usuarioId);
        return progresoDeRutaActual(usuario).stream()
                .filter(p -> p.getModulo().getId().equals(moduloId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("El módulo no pertenece a tu ruta"));
    }

    private void desbloquearSiguiente(Usuario usuario, Modulo actual) {
        moduloRepository
                .findFirstByRutaIdAndOrdenGreaterThanOrderByOrdenAsc(actual.getRuta().getId(), actual.getOrden())
                .flatMap(siguiente -> progresoRepository.findByUsuarioIdAndModuloId(usuario.getId(), siguiente.getId()))
                .filter(p -> p.getEstado() == EstadoProgreso.BLOQUEADO)
                .ifPresent(p -> {
                    p.setEstado(EstadoProgreso.DISPONIBLE);
                    progresoRepository.save(p);
                });
    }
}
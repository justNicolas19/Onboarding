package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.ModuloRequest;
import com.example.onboarding.dto.response.ModuloResponse;
import com.example.onboarding.entity.Modulo;
import com.example.onboarding.entity.Ruta;
import com.example.onboarding.enums.TipoModulo;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.ModuloRepository;
import com.example.onboarding.repository.ProgresoModuloRepository;
import com.example.onboarding.repository.RutaRepository;
import com.example.onboarding.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloServiceImpl implements ModuloService {

    private static final int PUNTAJE_POR_DEFECTO = 70;

    private final ModuloRepository moduloRepository;
    private final RutaRepository rutaRepository;
    private final ProgresoModuloRepository progresoRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public ModuloResponse crear(ModuloRequest request) {
        Ruta ruta = rutaRepository.findById(request.rutaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + request.rutaId()));
        if (moduloRepository.existsByRutaIdAndOrden(ruta.getId(), request.orden())) {
            throw new ReglaNegocioException("Ya existe un módulo en la posición " + request.orden() + " de esta ruta");
        }
        Modulo modulo = Modulo.builder()
                .ruta(ruta)
                .titulo(request.titulo().trim())
                .descripcion(request.descripcion())
                .tipo(request.tipo())
                .orden(request.orden())
                .idMoodle(request.idMoodle().trim())
                .puntajeMinimo(request.puntajeMinimo() != null ? request.puntajeMinimo() : PUNTAJE_POR_DEFECTO)
                .duracionMinutos(request.duracionMinutos())
                .puntosXp(request.puntosXp())
                .unidad(request.unidad())
                .unidadTitulo(limpiar(request.unidadTitulo()))
                .build();
        modulo.setEvaluacion(buscarEvaluacion(request.evaluacionId(), ruta.getId(), null));
        return mapper.toModuloResponse(moduloRepository.save(modulo));
    }

    @Override
    @Transactional
    public ModuloResponse actualizar(Long id, ModuloRequest request) {
        Modulo modulo = buscar(id);
        if (!modulo.getRuta().getId().equals(request.rutaId())) {
            throw new ReglaNegocioException("No se puede mover un módulo a otra ruta");
        }
        if (!modulo.getOrden().equals(request.orden())
                && moduloRepository.existsByRutaIdAndOrden(request.rutaId(), request.orden())) {
            throw new ReglaNegocioException("Ya existe un módulo en la posición " + request.orden() + " de esta ruta");
        }
        modulo.setTitulo(request.titulo().trim());
        modulo.setDescripcion(request.descripcion());
        modulo.setTipo(request.tipo());
        modulo.setOrden(request.orden());
        modulo.setIdMoodle(request.idMoodle().trim());
        if (request.puntajeMinimo() != null) {
            modulo.setPuntajeMinimo(request.puntajeMinimo());
        }
        modulo.setDuracionMinutos(request.duracionMinutos());
        modulo.setPuntosXp(request.puntosXp());
        modulo.setUnidad(request.unidad());
        modulo.setUnidadTitulo(limpiar(request.unidadTitulo()));
        modulo.setEvaluacion(buscarEvaluacion(request.evaluacionId(), request.rutaId(), modulo.getId()));
        return mapper.toModuloResponse(moduloRepository.save(modulo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Modulo modulo = buscar(id);
        if (progresoRepository.existsByModuloId(id)) {
            throw new ReglaNegocioException("El módulo ya tiene avances registrados y no se puede eliminar");
        }
        // Los módulos de contenido que apuntaban a esta evaluación quedan sin evaluación asociada.
        moduloRepository.findByEvaluacionId(id).forEach(m -> m.setEvaluacion(null));
        modulo.getRuta().getModulos().remove(modulo);
        moduloRepository.delete(modulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuloResponse> listarPorRuta(Long rutaId) {
        return moduloRepository.findByRutaIdOrderByOrdenAsc(rutaId).stream()
                .map(mapper::toModuloResponse).toList();
    }

    private Modulo buscar(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado: " + id));
    }

    /** La evaluación debe existir, ser de la misma ruta, ser de tipo EVALUACION y no ser el propio módulo. */
    private Modulo buscarEvaluacion(Long evaluacionId, Long rutaId, Long moduloId) {
        if (evaluacionId == null) {
            return null;
        }
        if (evaluacionId.equals(moduloId)) {
            throw new ReglaNegocioException("Un módulo no puede ser su propia evaluación");
        }
        Modulo evaluacion = buscar(evaluacionId);
        if (!evaluacion.getRuta().getId().equals(rutaId)) {
            throw new ReglaNegocioException("La evaluación debe pertenecer a la misma ruta");
        }
        if (evaluacion.getTipo() != TipoModulo.EVALUACION) {
            throw new ReglaNegocioException("El módulo asociado como evaluación debe ser de tipo EVALUACION");
        }
        return evaluacion;
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}

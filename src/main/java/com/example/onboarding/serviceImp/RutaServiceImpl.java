package com.example.onboarding.serviceImp;



import com.example.onboarding.dto.request.RutaRequest;
import com.example.onboarding.dto.response.RutaResponse;
import com.example.onboarding.entity.Ruta;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.RutaRepository;
import com.example.onboarding.service.RutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RutaServiceImpl implements RutaService {


    private final RutaRepository rutaRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public RutaResponse crear(RutaRequest request) {
        Ruta ruta = Ruta.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion())
                .activa(request.activa() == null || request.activa())
                .build();
        return mapper.toRutaResponse(rutaRepository.save(ruta));
    }

    @Override
    @Transactional
    public RutaResponse actualizar(Long id, RutaRequest request) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        ruta.setNombre(request.nombre().trim());
        ruta.setDescripcion(request.descripcion());
        if (request.activa() != null) {
            ruta.setActiva(request.activa());
        }
        ruta.setVersion(ruta.getVersion() + 1);
        return mapper.toRutaResponse(rutaRepository.save(ruta));
    }

    @Override
    @Transactional(readOnly = true)
    public RutaResponse obtener(Long id) {
        Ruta ruta = rutaRepository.findConModulosById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        return mapper.toRutaResponse(ruta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaResponse> listarActivas() {
        return rutaRepository.findByActivaTrue().stream().map(mapper::toRutaResponse).toList();
    }
}

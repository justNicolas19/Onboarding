package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.EspacioRequest;
import com.example.onboarding.dto.response.EspacioResponse;
import com.example.onboarding.entity.EspacioFisico;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.EspacioFisicoRepository;
import com.example.onboarding.service.EspacioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioServiceImpl implements EspacioService {

    private final EspacioFisicoRepository espacioRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public EspacioResponse crear(EspacioRequest request) {
        EspacioFisico espacio = EspacioFisico.builder()
                .nombre(request.nombre().trim())
                .capacidad(request.capacidad())
                .activo(request.activo() == null || request.activo())
                .build();
        return mapper.toEspacioResponse(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioResponse actualizar(Long id, EspacioRequest request) {
        EspacioFisico espacio = buscar(id);
        espacio.setNombre(request.nombre().trim());
        espacio.setCapacidad(request.capacidad());
        if (request.activo() != null) {
            espacio.setActivo(request.activo());
        }
        return mapper.toEspacioResponse(espacioRepository.save(espacio));
    }

    @Override
    @Transactional(readOnly = true)
    public EspacioResponse obtener(Long id) {
        return mapper.toEspacioResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponse> listarActivos() {
        return espacioRepository.findByActivoTrue().stream().map(mapper::toEspacioResponse).toList();
    }

    private EspacioFisico buscar(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado: " + id));
    }
}

package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.response.LiderResponse;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.LiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiderServiceImpl implements LiderService {

    private final UsuarioRepository usuarioRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<LiderResponse> listarParaEmpleado(Long empleadoId) {
        Usuario empleado = usuarioRepository.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + empleadoId));
        Long managerId = empleado.getManager() != null ? empleado.getManager().getId() : null;
        return usuarioRepository.findByRolAndActivoTrueOrderByNombreAsc(Rol.MANAGER).stream()
                .map(l -> mapper.toLiderResponse(l, managerId))
                .sorted(Comparator.comparing(LiderResponse::esMiManager).reversed())
                .toList();
    }
}

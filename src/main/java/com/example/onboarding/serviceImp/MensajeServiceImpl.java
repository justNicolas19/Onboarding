package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.MensajeRequest;
import com.example.onboarding.dto.response.MensajeResponse;
import com.example.onboarding.entity.Mensaje;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.MensajeRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public MensajeResponse enviar(Long remitenteId, MensajeRequest request) {
        if (remitenteId.equals(request.destinatarioId())) {
            throw new ReglaNegocioException("No puedes enviarte un mensaje a ti mismo");
        }
        Usuario destinatario = usuarioRepository.findById(request.destinatarioId())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Destinatario no encontrado"));
        Mensaje mensaje = mensajeRepository.save(Mensaje.builder()
                .remitente(usuarioRepository.getReferenceById(remitenteId))
                .destinatario(destinatario)
                .texto(request.texto().trim())
                .build());
        auditoriaService.registrar(remitenteId, AccionAuditoria.MENSAJE_ENVIADO, "Mensaje", mensaje.getId(),
                "Mensaje para " + destinatario.getNombre());
        return mapper.toMensajeResponse(mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeResponse> recibidos(Long usuarioId) {
        return mensajeRepository.findByDestinatarioIdOrderByFechaDesc(usuarioId).stream()
                .map(mapper::toMensajeResponse).toList();
    }
}

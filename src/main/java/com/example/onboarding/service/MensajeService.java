package com.example.onboarding.service;

import com.example.onboarding.dto.request.MensajeRequest;
import com.example.onboarding.dto.response.MensajeResponse;

import java.util.List;

public interface MensajeService {

    MensajeResponse enviar(Long remitenteId, MensajeRequest request);

    List<MensajeResponse> recibidos(Long usuarioId);
}

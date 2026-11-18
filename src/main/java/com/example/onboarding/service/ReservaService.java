package com.example.onboarding.service;

import com.example.onboarding.dto.request.ReservaRequest;
import com.example.onboarding.dto.response.FranjaResponse;
import com.example.onboarding.dto.response.ReservaResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaService {

    ReservaResponse crear(Long usuarioId, ReservaRequest request);

    /** Libera la reserva asociada a una sesión, si existe. */
    void liberarPorSesion(Long sesionId);

    List<ReservaResponse> listarPorUsuario(Long usuarioId);

    List<ReservaResponse> listarPorEspacio(Long espacioId, LocalDateTime desde, LocalDateTime hasta);

    /** Franjas del día con su disponibilidad (horario configurable en onboarding.reservas.*). */
    List<FranjaResponse> franjasDelDia(Long espacioId, LocalDate fecha);
}

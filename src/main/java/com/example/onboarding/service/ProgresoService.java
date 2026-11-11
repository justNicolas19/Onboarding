package com.example.onboarding.service;

import com.example.onboarding.dto.request.ReportarAvanceRequest;
import com.example.onboarding.dto.response.ProgresoModuloResponse;
import com.example.onboarding.dto.response.ResumenAvanceResponse;

import java.util.List;

public interface ProgresoService {

    /** Crea el progreso de los módulos que falten en la ruta actual del usuario. Es idempotente. */
    void sincronizarRuta(Long usuarioId);

    /** Ruta del empleado con el estado de cada módulo (RF2). */
    List<ProgresoModuloResponse> listarRuta(Long usuarioId);

    /** Ruta de un empleado vista por su manager. */
    List<ProgresoModuloResponse> listarRutaDeEmpleado(Long managerId, Long empleadoId);

    /** El empleado abre un módulo disponible; pasa a EN_CURSO. */
    ProgresoModuloResponse abrirModulo(Long usuarioId, Long moduloId);

    /** El empleado termina el módulo; el servidor consulta la nota en Moodle (RF3, RF4). */
    ProgresoModuloResponse reportarAvance(Long usuarioId, ReportarAvanceRequest request);

    /** Panel de seguimiento del manager (RF7). */
    List<ResumenAvanceResponse> resumenEquipo(Long managerId);
}
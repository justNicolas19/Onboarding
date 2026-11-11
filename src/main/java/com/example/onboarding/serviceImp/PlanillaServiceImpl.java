package com.example.onboarding.serviceImp;

import com.example.onboarding.entity.SesionSeguimiento;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.EstadoSesion;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.exception.AccesoDenegadoException;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.repository.SesionSeguimientoRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.PlanillaService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanillaServiceImpl implements PlanillaService {

    /** Rango de sesiones exigido por empleado (pendiente de confirmar con la empresa). */
    private static final int MIN_SESIONES = 5;
    private static final int MAX_SESIONES = 10;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String VACIO = "-";

    private final UsuarioRepository usuarioRepository;
    private final SesionSeguimientoRepository sesionRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public byte[] exportarExcel(Long actorId, Long empleadoId) {
        Usuario actor = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + actorId));
        List<Usuario> empleados = empleadosEnAlcance(actor, empleadoId);
        if (empleados.isEmpty()) {
            throw new ReglaNegocioException("No hay empleados para incluir en la planilla");
        }

        byte[] archivo = construir(actor, empleados);
        auditoriaService.registrar(actorId, AccionAuditoria.PLANILLA_EXPORTADA, "Usuario", empleadoId,
                "Planilla de cumplimiento exportada (" + empleados.size() + " empleado(s))");
        return archivo;
    }

    private List<Usuario> empleadosEnAlcance(Usuario actor, Long empleadoId) {
        if (actor.getRol() == Rol.EMPLEADO) {
            throw new AccesoDenegadoException("Solo un manager o un administrador puede exportar la planilla");
        }
        if (empleadoId != null) {
            Usuario empleado = usuarioRepository.findById(empleadoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + empleadoId));
            if (empleado.getRol() != Rol.EMPLEADO) {
                throw new ReglaNegocioException("El usuario indicado no es un empleado");
            }
            if (actor.getRol() == Rol.MANAGER
                    && (empleado.getManager() == null || !empleado.getManager().getId().equals(actor.getId()))) {
                throw new AccesoDenegadoException("El empleado no está a tu cargo");
            }
            return List.of(empleado);
        }
        List<Usuario> lista = actor.getRol() == Rol.MANAGER
                ? usuarioRepository.findByManagerIdAndActivoTrue(actor.getId())
                : usuarioRepository.findByRol(Rol.EMPLEADO).stream()
                        .filter(u -> Boolean.TRUE.equals(u.getActivo())).toList();
        return lista.stream().sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private byte[] construir(Usuario actor, List<Usuario> empleados) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            hojaResumen(libro, estilos, actor, empleados);
            for (int i = 0; i < empleados.size(); i++) {
                hojaDetalle(libro, estilos, empleados.get(i), i + 1);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", e);
        }
    }

    private void hojaResumen(XSSFWorkbook libro, Estilos estilos, Usuario actor, List<Usuario> empleados) {
        Sheet hoja = libro.createSheet("Resumen");
        int fila = 0;
        celda(hoja.createRow(fila++), 0, "Planilla de cumplimiento - Sesiones de seguimiento", estilos.titulo);
        celda(hoja.createRow(fila++), 0, "Generada el: " + LocalDateTime.now().format(FECHA_HORA), null);
        celda(hoja.createRow(fila++), 0, "Generada por: " + actor.getNombre() + " (" + actor.getRol() + ")", null);
        celda(hoja.createRow(fila++), 0, "Criterio: entre " + MIN_SESIONES + " y " + MAX_SESIONES
                + " sesiones verificadas (con soporte) por empleado", null);
        fila++;

        encabezado(hoja.createRow(fila++), estilos, "Empleado", "Correo", "Cargo", "Manager", "Ruta",
                "Sesiones verificadas", "Realizadas sin soporte", "Programadas", "Estado");
        for (Usuario empleado : empleados) {
            long verificadas = sesionRepository.countByEmpleadoIdAndEstado(empleado.getId(), EstadoSesion.VERIFICADA);
            long sinSoporte = sesionRepository.countByEmpleadoIdAndEstado(empleado.getId(), EstadoSesion.REALIZADA);
            long programadas = sesionRepository.countByEmpleadoIdAndEstado(empleado.getId(), EstadoSesion.PROGRAMADA);
            Row row = hoja.createRow(fila++);
            celda(row, 0, empleado.getNombre(), estilos.cuerpo);
            celda(row, 1, empleado.getCorreo(), estilos.cuerpo);
            celda(row, 2, texto(empleado.getCargo()), estilos.cuerpo);
            celda(row, 3, empleado.getManager() != null ? empleado.getManager().getNombre() : VACIO, estilos.cuerpo);
            celda(row, 4, empleado.getRuta() != null ? empleado.getRuta().getNombre() : VACIO, estilos.cuerpo);
            celda(row, 5, verificadas, estilos.cuerpo);
            celda(row, 6, sinSoporte, estilos.cuerpo);
            celda(row, 7, programadas, estilos.cuerpo);
            celda(row, 8, estado(verificadas), estilos.cuerpo);
        }
        anchos(hoja, 28, 32, 22, 26, 26, 20, 22, 14, 22);
    }

    private void hojaDetalle(XSSFWorkbook libro, Estilos estilos, Usuario empleado, int numero) {
        Sheet hoja = libro.createSheet(WorkbookUtil.createSafeSheetName(numero + " " + empleado.getNombre()));
        List<SesionSeguimiento> sesiones = sesionRepository
                .findByEmpleadoIdAndEstadoOrderByFechaHoraAsc(empleado.getId(), EstadoSesion.VERIFICADA);

        int fila = 0;
        celda(hoja.createRow(fila++), 0, "Sesiones de seguimiento - " + empleado.getNombre(), estilos.titulo);
        celda(hoja.createRow(fila++), 0, "Correo: " + empleado.getCorreo() + "   |   Cargo: " + texto(empleado.getCargo()), null);
        celda(hoja.createRow(fila++), 0, "Manager: "
                + (empleado.getManager() != null ? empleado.getManager().getNombre() : VACIO)
                + "   |   Ruta: " + (empleado.getRuta() != null ? empleado.getRuta().getNombre() : VACIO), null);
        celda(hoja.createRow(fila++), 0, "Sesiones verificadas: " + sesiones.size() + " de " + MIN_SESIONES
                + " mínimas - " + estado(sesiones.size()), estilos.negrita);
        fila++;

        encabezado(hoja.createRow(fila++), estilos, "N.°", "Fecha y hora", "Manager", "Enlace de Teams", "Soporte");
        int n = 1;
        for (SesionSeguimiento s : sesiones) {
            Row row = hoja.createRow(fila++);
            celda(row, 0, n++, estilos.cuerpo);
            celda(row, 1, s.getFechaHora().format(FECHA_HORA), estilos.cuerpo);
            celda(row, 2, s.getManager().getNombre(), estilos.cuerpo);
            celda(row, 3, texto(s.getEnlaceTeams()), estilos.cuerpo);
            celda(row, 4, texto(s.getSoporteUrl()), estilos.cuerpo);
        }
        anchos(hoja, 8, 20, 28, 60, 60);
    }

    private String estado(long verificadas) {
        return verificadas >= MIN_SESIONES ? "CUMPLE" : "NO CUMPLE (faltan " + (MIN_SESIONES - verificadas) + ")";
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? VACIO : valor;
    }

    private void celda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellValue(valor);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    private void celda(Row fila, int columna, long valor, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private void encabezado(Row fila, Estilos estilos, String... titulos) {
        for (int i = 0; i < titulos.length; i++) {
            celda(fila, i, titulos[i], estilos.encabezado);
        }
    }

    /** Anchos fijos (en caracteres): autoSizeColumn necesita fuentes del sistema y falla en servidores sin ellas. */
    private void anchos(Sheet hoja, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) {
            hoja.setColumnWidth(i, caracteres[i] * 256);
        }
    }

    private static class Estilos {
        final CellStyle titulo;
        final CellStyle negrita;
        final CellStyle encabezado;
        final CellStyle cuerpo;

        Estilos(XSSFWorkbook libro) {
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTitulo);

            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            negrita = libro.createCellStyle();
            negrita.setFont(fuenteNegrita);

            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
            encabezado = libro.createCellStyle();
            encabezado.setFont(fuenteEncabezado);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(encabezado);

            cuerpo = libro.createCellStyle();
            bordes(cuerpo);
        }

        private static void bordes(CellStyle estilo) {
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
        }
    }
}

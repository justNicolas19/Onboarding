package com.example.onboarding.exception;

import com.example.onboarding.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(), ex.getCodigo());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of(), "CREDENCIALES_INVALIDAS");
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException ex) {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), List.of(), null);
    }

    /** Lo lanza @PreAuthorize cuando el rol no alcanza. Sin este handler caería en el 500 genérico. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción", List.of(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la petición", detalles, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido (revisa el JSON, las fechas y los valores de los enums)",
                List.of(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' no tiene un formato válido",
                List.of(), null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + ex.getParameterName() + "'",
                List.of(), null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivoGrande(MaxUploadSizeExceededException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El archivo supera el máximo de 5 MB", List.of(), "ARCHIVO_GRANDE");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return construir(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", List.of(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", List.of(), null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, List<String> detalles,
                                                    String codigo) {
        ErrorResponse cuerpo = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, detalles, codigo);
        return ResponseEntity.status(status).body(cuerpo);
    }
}

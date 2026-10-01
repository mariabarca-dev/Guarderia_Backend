package com.guarderiaCentral.guarderia_Backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones de la API REST de la Guardería Central.
 * <p>
 * Captura las excepciones de la jerarquía {@link BusinessException}, los errores de validación
 * ({@code @Valid}), los cuerpos de solicitud ilegibles, los accesos denegados por
 * {@code @PreAuthorize}, los conflictos de integridad de la base de datos y cualquier error
 * inesperado, y los convierte en una respuesta JSON estructurada ({@link ErrorResponse})
 * con el código HTTP correspondiente.
 * </p>
 *
 * @author Guardería Central
 * @version 2.2
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones del tipo BusinessException y sus subclases (como GarageYaOcupadoException).
     *
     * @param ex      Excepción capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con el código HTTP adecuado.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Excepción de negocio capturada: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return construirRespuesta(ex.getStatus(), ex.getMessage(), request);
    }

    /**
     * Maneja los errores de validación de los Request (@Valid) para devolverlos con el formato ErrorResponse.
     *
     * @param ex      Excepción de validación capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con el detalle de los campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Error de validación: {} - Path: {}", errores, request.getRequestURI());
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Errores de validación: " + errores, request);
    }

    /**
     * Maneja los cuerpos de solicitud ilegibles: JSON mal formado, fechas con formato inválido
     * o valores que no existen en un enum.
     *
     * @param ex      Excepción de lectura del cuerpo capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 400 (BAD_REQUEST).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Cuerpo de solicitud ilegible: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido o tiene un formato incorrecto.", request);
    }

    /**
     * Maneja los parámetros de ruta o de consulta con un tipo incorrecto (por ejemplo, un id no numérico).
     *
     * @param ex      Excepción de conversión de tipo capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 400 (BAD_REQUEST).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Parámetro con tipo inválido '{}': {} - Path: {}", ex.getName(), ex.getValue(), request.getRequestURI());
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido.", request);
    }

    /**
     * Maneja los accesos denegados por {@code @PreAuthorize} cuando el rol del usuario
     * no alcanza para el recurso solicitado.
     * <p>
     * Es necesario declararlo de forma explícita: sin este método, el manejador genérico de
     * {@link Exception} lo convertiría en un error 500.
     * </p>
     *
     * @param ex      Excepción de acceso denegado capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 403 (FORBIDDEN).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return construirRespuesta(HttpStatus.FORBIDDEN,
                "No tiene permisos para acceder a este recurso.", request);
    }

    /**
     * Maneja los métodos HTTP no soportados por el endpoint solicitado.
     *
     * @param ex      Excepción de método no soportado capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 405 (METHOD_NOT_ALLOWED).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Método no soportado: {} - Path: {}", ex.getMethod(), request.getRequestURI());
        return construirRespuesta(HttpStatus.METHOD_NOT_ALLOWED,
                "El método HTTP " + ex.getMethod() + " no está permitido para este recurso.", request);
    }

    /**
     * Maneja las solicitudes a rutas que no existen en la API.
     * Desde Spring Boot 3.2 estas solicitudes lanzan {@link NoResourceFoundException}; sin este
     * método las atraparía el manejador genérico y se devolvería un error 500 en lugar de 404.
     *
     * @param ex      Excepción de recurso no encontrado capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 404 (NOT_FOUND).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Ruta inexistente - Path: {}", request.getRequestURI());
        return construirRespuesta(HttpStatus.NOT_FOUND,
                "El recurso solicitado no existe.", request);
    }

    /**
     * Maneja las violaciones de integridad de la base de datos (claves únicas, claves foráneas)
     * que no fueron prevenidas por las validaciones de los services.
     *
     * @param ex      Excepción de integridad de datos capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 409 (CONFLICT).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Violación de integridad de datos - Path: {}", request.getRequestURI(), ex);
        return construirRespuesta(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de los datos.", request);
    }

    /**
     * Maneja cualquier excepción inesperada que no tenga un manejador más específico.
     * Registra la traza completa en el log y devuelve un mensaje genérico para no exponer
     * detalles internos al cliente.
     *
     * @param ex      Excepción inesperada capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con estado 500 (INTERNAL_SERVER_ERROR).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado - Path: {}", request.getRequestURI(), ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor.", request);
    }

    /**
     * Construye la respuesta de error estándar de la API.
     *
     * @param status  Estado HTTP de la respuesta.
     * @param mensaje Mensaje descriptivo del error.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponse} con el estado indicado.
     */
    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
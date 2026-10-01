package com.guarderiaCentral.guarderia_Backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones de la API REST de la Guardería Central.
 * <p>
 * Captura las excepciones de la jerarquía {@link BusinessException}, los errores de validación
 * ({@code @Valid}) y los cuerpos de solicitud ilegibles, y los convierte en una respuesta JSON
 * estructurada ({@link ErrorResponse}) con el código HTTP correspondiente.
 * </p>
 *
 * @author Guardería Central
 * @version 2.1
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

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, ex.getStatus());
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

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Errores de validación: " + errores,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El cuerpo de la solicitud es inválido o tiene un formato incorrecto.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
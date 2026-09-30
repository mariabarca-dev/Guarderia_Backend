package com.guarderiaCentral.guarderia_Backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones de la API REST de la Guardería Central.
 * <p>
 * Captura las excepciones de la jerarquía {@link BusinessException} y las convierte en una
 * respuesta JSON estructurada ({@link ErrorResponseDTO}) con el código HTTP asociado a cada una.
 * </p>
 *
 * @author Guardería Central
 * @version 2.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones del tipo BusinessException y sus subclases (como GarageYaOcupadoException).
     *
     * @param ex      Excepción capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponseDTO} con el código HTTP adecuado.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Excepción de negocio capturada: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}
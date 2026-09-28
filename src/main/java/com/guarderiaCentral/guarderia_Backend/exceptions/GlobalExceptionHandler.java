package com.guarderiaCentral.guarderia_Backend.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones del tipo BusinessException y sus subclases (como GarageYaOcupadoException).
     *
     * @param ex Excepción capturada.
     * @param request Información de la solicitud HTTP actual.
     * @return Respuesta estructurada {@link ErrorResponseDTO} con el código HTTP adecuado.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("Excepción de negocio capturada: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}
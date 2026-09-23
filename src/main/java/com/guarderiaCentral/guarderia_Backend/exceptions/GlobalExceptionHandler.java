package com.guarderiaCentral.guarderia_Backend.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Captura las excepciones de negocio y de validación de Bean Validation para formatear
 * una respuesta JSON estándar con el estado HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones derivadas de {@link BusinessException}.
     *
     * @param ex Excepción de negocio capturada.
     * @param request Solicitud HTTP en curso.
     * @return {@link ResponseEntity} con la estructura {@link ErrorResponseDTO} y el estado HTTP adecuado.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Captura las fallas de validación de Bean Validation (@Valid en RequestDTOs).
     *
     * @param ex Excepción de argumentos no válidos.
     * @param request Solicitud HTTP en curso.
     * @return {@link ResponseEntity} con el detalle de las reglas violadas.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

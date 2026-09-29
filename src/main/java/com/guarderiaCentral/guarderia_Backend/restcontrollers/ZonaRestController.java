package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaUpdate;
import com.guarderiaCentral.guarderia_Backend.services.ZonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * Controlador REST API para la gestión de Zonas en la Guardería Central.
 * Proporciona endpoints para consulta (GET) accesibles por SOCIO, EMPLEADO y ADMINISTRADOR,
 * y operaciones de escritura/modificación (POST, PUT, DELETE) exclusivas para ADMINISTRADOR.
 *
 * @author Cátedra Desarrollo Java Backend
 * @version 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaRestController {

    private final ZonaService zonaService;

    /**
     * Expresión regular para validar el formato de la letra de la zona (1 a 3 caracteres alfabéticos).
     */
    private static final String REGEX_LETRA_ZONA = "^[a-zA-Z]{1,3}$";

    /**
     * Registra una nueva zona en el sistema.
     * Operación restringida a usuarios con rol ADMINISTRADOR.
     *
     * @param request DTO con los datos para la creación de la Zona.
     * @return {@link ResponseEntity} con la {@link ZonaResponse} creada y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ZonaResponse> registrarZona(@Valid @RequestBody ZonaRequest request) {
        log.info("REST Request para registrar nueva Zona con letra: {}", request.getLetra());
        ZonaResponse nuevaZona = zonaService.registrarZona(request);
        return new ResponseEntity<>(nuevaZona, HttpStatus.CREATED);
    }

    /**
     * Obtiene la lista completa de todas las zonas activas en el sistema.
     * Accesible por usuarios con rol SOCIO, EMPLEADO o ADMINISTRADOR.
     *
     * @return {@link ResponseEntity} con la lista de {@link ZonaResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<ZonaResponse>> listarZonas() {
        log.info("REST Request para listar todas las zonas activas");
        List<ZonaResponse> zonas = zonaService.listarTodas();
        return ResponseEntity.ok(zonas);
    }

    /**
     * Busca y obtiene el detalle de una zona por su letra identificadora.
     * Accesible por usuarios con rol SOCIO, EMPLEADO o ADMINISTRADOR.
     *
     * @param letra Letra o identificador único de la zona (1 a 3 letras).
     * @return {@link ResponseEntity} con la {@link ZonaResponse} encontrada y código HTTP 200 OK.
     */
    @GetMapping("/{letra}")
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<ZonaResponse> buscarPorLetra(
            @PathVariable
            @Pattern(regexp = REGEX_LETRA_ZONA, message = "La letra de la zona debe contener entre 1 y 3 caracteres alfabéticos.")
            String letra) {

        log.info("REST Request para buscar Zona por letra: {}", letra);
        ZonaResponse zona = zonaService.buscarPorLetra(letra.trim().toUpperCase());
        return ResponseEntity.ok(zona);
    }

    /**
     * Actualiza la información de una zona existente identified por su ID.
     * Operación restringida a usuarios con rol ADMINISTRADOR.
     *
     * @param id Identificador numérico único de la zona a actualizar.
     * @param update DTO con los datos actualizados de la Zona.
     * @return {@link ResponseEntity} con la {@link ZonaResponse} actualizada y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ZonaResponse> actualizarZona(
            @PathVariable Integer id,
            @Valid @RequestBody ZonaUpdate update) {

        log.info("REST Request para actualizar Zona con ID: {}", id);
        ZonaResponse zonaActualizada = zonaService.actualizarZona(id, update);
        return ResponseEntity.ok(zonaActualizada);
    }

    /**
     * Realiza la baja lógica de una zona mediante su letra identificadora o ID.
     * Operación restringida a usuarios con rol ADMINISTRADOR.
     *
     * @param letra Letra identificadora de la zona a eliminar.
     * @return {@link ResponseEntity} con respuesta sin contenido (204 No Content).
     */
    @DeleteMapping("/{letra}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarZona(
            @PathVariable
            @Pattern(regexp = REGEX_LETRA_ZONA, message = "Debe especificar una letra de zona válida para eliminar (1 a 3 letras).")
            String letra) {

        log.info("REST Request para realizar borrado lógico de Zona con letra: {}", letra);
        zonaService.eliminarZona(letra.trim().toUpperCase());
        return ResponseEntity.noContent().build();
    }
}
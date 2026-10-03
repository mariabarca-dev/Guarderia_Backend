package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaUpdate;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionEmpleadoZonaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador RESTful para la gestión de las asignaciones de empleados a zonas (AsignacionEmpleadoZona).
 * Proporciona endpoints para realizar operaciones CRUD respetando la seguridad y permisos asignados por rol.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 * @version 1.0
 */
@RestController
@RequestMapping("/api/asignaciones-empleado-zona")
@RequiredArgsConstructor
public class AsignacionEmpleadoZonaRestController {

    private static final Logger logger = LoggerFactory.getLogger(AsignacionEmpleadoZonaRestController.class);

    private final AsignacionEmpleadoZonaService asignacionService;

    /**
     * Obtiene la lista completa de asignaciones activas de empleados a zonas.
     * Permitido para roles: SOCIO, EMPLEADO, ADMINISTRADOR.
     *
     * @return ResponseEntity con la lista de {@link AsignacionEmpleadoZonaResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> obtenerTodas() {
        logger.info("REST Request para obtener todas las asignaciones de empleado a zona activas.");
        List<AsignacionEmpleadoZonaResponse> lista = asignacionService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene la lista completa de asignaciones, incluyendo registros inactivos (borrado lógico).
     * Uso exclusivo administrativo para auditar el sistema.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @return ResponseEntity con la lista completa de {@link AsignacionEmpleadoZonaResponse} y código HTTP 200 OK.
     */
    @GetMapping("/admin/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> obtenerTodasIncluyendoInactivas() {
        logger.info("REST Request para obtener todas las asignaciones (incluyendo inactivas).");
        List<AsignacionEmpleadoZonaResponse> lista = asignacionService.listarTodasIncluyendoInactivas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna una asignación activa por su identificador único.
     * Permitido para roles: SOCIO, EMPLEADO, ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación.
     * @return ResponseEntity con la {@link AsignacionEmpleadoZonaResponse} encontrada y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<AsignacionEmpleadoZonaResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para obtener la asignación con ID: {}", id);
        AsignacionEmpleadoZonaResponse response = asignacionService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una nueva asignación de empleado a zona con la cantidad de vehículos a cargo.
     * La validación Bean Validation se ejecuta automáticamente mediante la anotación {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param request Datos de la asignación a crear ({@link AsignacionEmpleadoZonaRequest}).
     * @return ResponseEntity con la {@link AsignacionEmpleadoZonaResponse} creada y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionEmpleadoZonaResponse> crear(
            @Valid @RequestBody AsignacionEmpleadoZonaRequest request) {
        logger.info("REST Request para crear una nueva asignación entre empleado ID: {} y zona ID: {}",
                request.getEmpleadoId(), request.getZonaId());
        AsignacionEmpleadoZonaResponse nuevaAsignacion = asignacionService.crearAsignacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }

    /**
     * Actualiza los datos de una asignación existente.
     * La validación Bean Validation se ejecuta automáticamente mediante la anotación {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación a modificar.
     * @param update Datos actualizados de la asignación ({@link AsignacionEmpleadoZonaUpdate}).
     * @return ResponseEntity con la {@link AsignacionEmpleadoZonaResponse} actualizada y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionEmpleadoZonaResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AsignacionEmpleadoZonaUpdate update) {
        logger.info("REST Request para actualizar la asignación con ID: {}", id);
        AsignacionEmpleadoZonaResponse asignacionActualizada = asignacionService.actualizarAsignacion(id, update);
        return ResponseEntity.ok(asignacionActualizada);
    }

    /**
     * Realiza el borrado lógico de una asignación cambiando su estado a inactivo (activo = false).
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación a desactivar.
     * @return ResponseEntity con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para eliminar (borrado lógico) la asignación con ID: {}", id);
        asignacionService.eliminarAsignacion(id);
        return ResponseEntity.noContent().build();
    }
}
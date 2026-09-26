package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;
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
 * Controlador RESTful para la gestión de asignaciones entre vehículos y garajes (AsignacionVehiculoGarage).
 * Proporciona endpoints REST limpios para operaciones CRUD sobre la relación asociativa 1 a 1 de asignación,
 * respetando la matriz de seguridad medianteSpring Security y anotaciones @PreAuthorize.
 *
 * @author Franco Tomás Buyatti
 * @version 1.0
 */
@RestController
@RequestMapping("/api/asignaciones-vehiculo-garage")
@RequiredArgsConstructor
public class AsignacionVehiculoGarageRestController {

    private static final Logger logger = LoggerFactory.getLogger(AsignacionVehiculoGarageRestController.class);

    private final AsignacionVehiculoGarageService asignacionService;

    /**
     * Obtiene el listado completo de asignaciones activas de vehículos a garajes.
     * Permitido para roles: SOCIO, EMPLEADO, ADMINISTRADOR.
     *
     * @return ResponseEntity conteniendo la lista de {@link AsignacionVehiculoGarageResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionVehiculoGarageResponse>> obtenerTodas() {
        logger.info("REST Request para listar todas las asignaciones de vehículo a garaje activas.");
        List<AsignacionVehiculoGarageResponse> lista = asignacionService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene el listado completo de asignaciones incluyendo registros inactivos (borrado lógico).
     * Exclusivo para la auditoría de la administración.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @return ResponseEntity conteniendo la lista completa de {@link AsignacionVehiculoGarageResponse} y código HTTP 200 OK.
     */
    @GetMapping("/admin/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionVehiculoGarageResponse>> obtenerTodasIncluyendoInactivas() {
        logger.info("REST Request para listar todas las asignaciones de vehículo a garaje (incluyendo inactivas).");
        List<AsignacionVehiculoGarageResponse> lista = asignacionService.listarTodasIncluyendoInactivas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna una asignación activa por su identificador único.
     * Permitido para roles: SOCIO, EMPLEADO, ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación.
     * @return ResponseEntity con la {@link AsignacionVehiculoGarageResponse} encontrada y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<AsignacionVehiculoGarageResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para obtener la asignación de vehículo a garaje por ID: {}", id);
        AsignacionVehiculoGarageResponse response = asignacionService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca la asignación activa asociada a un vehículo específico utilizando su ID.
     * Permitido para roles: SOCIO, EMPLEADO, ADMINISTRADOR.
     *
     * @param vehiculoId Identificador único del vehículo.
     * @return ResponseEntity con la {@link AsignacionVehiculoGarageResponse} asociada al vehículo y código HTTP 200 OK.
     */
    @GetMapping("/vehiculo/{vehiculoId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<AsignacionVehiculoGarageResponse> buscarPorVehiculo(@PathVariable Integer vehiculoId) {
        logger.info("REST Request para consultar la asignación asociada al vehículo ID: {}", vehiculoId);
        AsignacionVehiculoGarageResponse response = asignacionService.buscarPorVehiculo(vehiculoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una nueva asignación de un vehículo a un garaje.
     * La validación estructural y sintáctica se realiza automáticamente mediante {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param request DTO con la información requerida para registrar la asignación ({@link AsignacionVehiculoGarageRequest}).
     * @return ResponseEntity con la {@link AsignacionVehiculoGarageResponse} creada y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionVehiculoGarageResponse> crear(
            @Valid @RequestBody AsignacionVehiculoGarageRequest request) {
        logger.info("REST Request para registrar una asignación del vehículo ID: {} al garaje ID: {}",
                request.getIdVehiculo(), request.getIdGarage());
        AsignacionVehiculoGarageResponse nuevaAsignacion = asignacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }

    /**
     * Actualiza los datos de una asignación de vehículo a garaje existente.
     * La validación se ejecuta de forma automática con la anotación {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación a actualizar.
     * @param update DTO con los datos actualizados de la asignación ({@link AsignacionVehiculoGarageUpdate}).
     * @return ResponseEntity con la {@link AsignacionVehiculoGarageResponse} actualizada y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionVehiculoGarageResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AsignacionVehiculoGarageUpdate update) {
        logger.info("REST Request para actualizar la asignación de vehículo a garaje ID: {}", id);
        AsignacionVehiculoGarageResponse asignacionActualizada = asignacionService.actualizar(id, update);
        return ResponseEntity.ok(asignacionActualizada);
    }

    /**
     * Ejecuta el borrado lógico de una asignación marcándola como inactiva (activo = false).
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único de la asignación a desactivar.
     * @return ResponseEntity con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para eliminar (borrado lógico) la asignación con ID: {}", id);
        asignacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
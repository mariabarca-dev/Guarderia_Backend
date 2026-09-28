package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
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
 * Controlador RESTful para la gestión y administración de cuentas de Empleados.
 * Proporciona endpoints para la gestión de usuarios de tipo Empleado por parte del SYSADMIN,
 * así como endpoints de consulta de zonas y vehículos bajo responsabilidad del empleado.
 *
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoRestController {

    private static final Logger logger = LoggerFactory.getLogger(EmpleadoRestController.class);

    private final EmpleadoService empleadoService;

    /**
     * Obtiene el listado de todos los empleados activos en el sistema.
     * Permitido para rol: SYSADMIN.
     *
     * @return ResponseEntity conteniendo la lista de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> obtenerTodos() {
        logger.info("REST Request para listar todos los empleados activos.");
        List<EmpleadoResponse> lista = empleadoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene el listado de todos los empleados, incluyendo aquellos dados de baja lógicamente (activo = false).
     * Permitido para rol: SYSADMIN.
     *
     * @return ResponseEntity conteniendo la lista completa de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> obtenerTodosIncluyendoInactivos() {
        logger.info("REST Request para listar todos los empleados (incluyendo inactivos).");
        List<EmpleadoResponse> lista = empleadoService.listarTodosIncluyendoInactivos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna un empleado activo según su ID.
     * Permitido para rol: SYSADMIN.
     *
     * @param id Identificador único del empleado.
     * @return ResponseEntity con la información de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para buscar el empleado con ID: {}", id);
        EmpleadoResponse response = empleadoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo empleado en el sistema.
     * La validación estructural y de sintaxis se realiza automáticamente mediante la anotación {@code @Valid}.
     * Permitido para rol: SYSADMIN.
     *
     * @param request Objeto con la información requerida para el alta del empleado ({@link EmpleadoRequest}).
     * @return ResponseEntity con el {@link EmpleadoResponse} creado y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
        logger.info("REST Request para dar de alta un nuevo empleado con código: {}", request.getCodigo());
        EmpleadoResponse nuevoEmpleado = empleadoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
    }

    /**
     * Actualiza la información de un empleado existente.
     * La validación se ejecuta automáticamente con {@code @Valid}.
     * Permitido para rol: SYSADMIN.
     *
     * @param id Identificador único del empleado a modificar.
     * @param update Objeto con los datos actualizados del empleado ({@link EmpleadoUpdate}).
     * @return ResponseEntity con el {@link EmpleadoResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoUpdate update) {
        logger.info("REST Request para actualizar el empleado con ID: {}", id);
        EmpleadoResponse empleadoActualizado = empleadoService.actualizar(id, update);
        return ResponseEntity.ok(empleadoActualizado);
    }

    /**
     * Desactiva (borrado lógico) la cuenta de un empleado.
     * Permitido para rol: SYSADMIN.
     *
     * @param id Identificador único del empleado a dar de baja.
     * @return ResponseEntity con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para dar de baja lógicamente al empleado con ID: {}", id);
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el listado de zonas asignadas a un empleado específico.
     * Permitido para roles: EMPLEADO, ADMINISTRADOR.
     *
     * @param empleadoId Identificador único del empleado.
     * @return ResponseEntity con la lista de {@link ZonaDTO} asociadas y código HTTP 200 OK.
     */
    @GetMapping("/{empleadoId}/zonas")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<ZonaDTO>> listarZonasAsignadas(@PathVariable int empleadoId) {
        logger.info("REST Request para obtener las zonas asignadas al empleado ID: {}", empleadoId);
        List<ZonaDTO> zonas = empleadoService.listarZonasAsignadas(empleadoId);
        return ResponseEntity.ok(zonas);
    }

    /**
     * Obtiene el listado de vehículos bajo la responsabilidad directa de un empleado específico.
     * Permitido para roles: EMPLEADO, ADMINISTRADOR.
     *
     * @param empleadoId Identificador único del empleado.
     * @return ResponseEntity con la lista de {@link VehiculoDTO} bajo su responsabilidad y código HTTP 200 OK.
     */
    @GetMapping("/{empleadoId}/vehiculos-a-cargo")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoDTO>> listarVehiculosBajoResponsabilidad(@PathVariable int empleadoId) {
        logger.info("REST Request para obtener los vehículos bajo la responsabilidad del empleado ID: {}", empleadoId);
        List<VehiculoDTO> vehiculos = empleadoService.listarVehiculosBajoResponsabilidad(empleadoId);
        return ResponseEntity.ok(vehiculos);
    }
}
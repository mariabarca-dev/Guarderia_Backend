package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador RESTful para la gestión y administración de Empleados.
 * Proporciona endpoints para el CRUD de las cuentas de empleado (ADMINISTRADOR y SYSADMIN), la asignación de
 * vehículos a empleados (ADMINISTRADOR) y los endpoints {@code /me}, que permiten al EMPLEADO consultar únicamente
 * sus propios datos: el empleado se identifica por el token y nunca por un ID de la URL.
 *
 * @author Cátedra Guardería Central
 * @version 1.1
 */
@Slf4j
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoRestController {

    private final EmpleadoService empleadoService;

    // ------------------------------------------------------------------
    // Endpoints del empleado autenticado ("solo lo suyo")
    // ------------------------------------------------------------------

    /**
     * Devuelve el perfil del empleado autenticado.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual (el nombre sale del token).
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<EmpleadoResponse> miPerfil(Authentication authentication) {
        log.info("Petición REST: consulta del perfil del empleado autenticado.");
        return ResponseEntity.ok(empleadoService.buscarMiPerfil(authentication.getName()));
    }

    /**
     * Devuelve las zonas asignadas al empleado autenticado, con el número de vehículos a cargo en cada una.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link AsignacionEmpleadoZonaResponse} y código HTTP 200 OK.
     */
    @GetMapping("/me/zonas")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> misZonas(Authentication authentication) {
        log.info("Petición REST: consulta de las zonas del empleado autenticado.");
        return ResponseEntity.ok(empleadoService.listarMisZonas(authentication.getName()));
    }

    /**
     * Devuelve los vehículos que tiene a cargo el empleado autenticado.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/me/vehiculos")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<VehiculoResponse>> misVehiculos(Authentication authentication) {
        log.info("Petición REST: consulta de los vehículos a cargo del empleado autenticado.");
        return ResponseEntity.ok(empleadoService.listarMisVehiculos(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Gestión administrativa de empleados
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado de todos los empleados activos en el sistema.
     * Permitido para los roles: ADMINISTRADOR, SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> obtenerTodos() {
        log.info("Petición REST para listar todos los empleados activos.");
        List<EmpleadoResponse> lista = empleadoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene el listado completo de empleados, incluyendo aquellos dados de baja lógicamente (activo = false).
     * Permitido exclusivamente para los roles: ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> obtenerTodosIncluyendoInactivos() {
        log.info("Petición REST para listar todos los empleados (incluyendo inactivos).");
        List<EmpleadoResponse> lista = empleadoService.listarTodosIncluyendoInactivos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna un empleado activo según su ID.
     * Permitido para los roles: ADMINISTRADOR, SYSADMIN.
     *
     * @param id Identificador único del empleado.
     * @return {@link ResponseEntity} con la información de {@link EmpleadoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> obtenerPorId(@PathVariable Integer id) {
        log.info("Petición REST para buscar el empleado con ID: {}", id);
        EmpleadoResponse response = empleadoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo empleado en el sistema o reactiva uno previamente inactivo.
     * La validación sintáctica se realiza automáticamente con {@code @Valid}.
     * Permitido para los roles: ADMINISTRADOR y SYSADMIN.
     *
     * @param request Objeto con la información requerida para el alta del empleado ({@link EmpleadoRequest}).
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} creado/reactivado y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
        log.info("Petición REST para dar de alta un nuevo empleado con código: {}", request.getCodigo());
        EmpleadoResponse nuevoEmpleado = empleadoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
    }

    /**
     * Actualiza la información de un empleado existente.
     * Permitido para los roles: ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador único del empleado a modificar.
     * @param update Objeto con los datos actualizados del empleado ({@link EmpleadoUpdate}).
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoUpdate update) {
        log.info("Petición REST para actualizar el empleado con ID: {}", id);
        EmpleadoResponse empleadoActualizado = empleadoService.actualizar(id, update);
        return ResponseEntity.ok(empleadoActualizado);
    }

    /**
     * Realiza la baja lógica (activo = false) de un empleado.
     * Permitido para los roles: ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador único del empleado a desactivar.
     * @return {@link ResponseEntity} con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("Petición REST para dar de baja lógicamente al empleado con ID: {}", id);
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el listado de asignaciones de zona asociadas a un empleado específico.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el empleado usa {@code /me/zonas}).
     *
     * @param empleadoId Identificador único del empleado.
     * @return {@link ResponseEntity} con la lista de {@link AsignacionEmpleadoZonaResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{empleadoId}/zonas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> listarZonasAsignadas(@PathVariable Integer empleadoId) {
        log.info("Petición REST para obtener las zonas asignadas al empleado ID: {}", empleadoId);
        List<AsignacionEmpleadoZonaResponse> zonas = empleadoService.listarZonasAsignadas(empleadoId);
        return ResponseEntity.ok(zonas);
    }

    /**
     * Obtiene el listado de vehículos que un empleado específico tiene a cargo.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el empleado usa {@code /me/vehiculos}).
     *
     * @param empleadoId Identificador único del empleado.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{empleadoId}/vehiculos-a-cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosBajoResponsabilidad(@PathVariable Integer empleadoId) {
        log.info("Petición REST para obtener los vehículos a cargo del empleado ID: {}", empleadoId);
        List<VehiculoResponse> vehiculos = empleadoService.listarVehiculosBajoResponsabilidad(empleadoId);
        return ResponseEntity.ok(vehiculos);
    }

    /**
     * Asigna un vehículo a un empleado. El vehículo debe estar en una zona a la que el empleado esté asignado,
     * y el empleado no puede superar la cantidad de vehículos a cargo definida para esa zona.
     * Permitido exclusivamente para el rol ADMINISTRADOR.
     *
     * @param empleadoId Identificador único del empleado.
     * @param vehiculoId Identificador único del vehículo.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} asignado y código HTTP 200 OK.
     */
    @PutMapping("/{empleadoId}/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> asignarVehiculo(
            @PathVariable Integer empleadoId,
            @PathVariable Integer vehiculoId) {
        log.info("Petición REST para asignar el vehículo ID {} al empleado ID {}", vehiculoId, empleadoId);
        return ResponseEntity.ok(empleadoService.asignarVehiculo(empleadoId, vehiculoId));
    }

    /**
     * Quita un vehículo del cargo de un empleado.
     * Permitido exclusivamente para el rol ADMINISTRADOR.
     *
     * @param empleadoId Identificador único del empleado.
     * @param vehiculoId Identificador único del vehículo.
     * @return {@link ResponseEntity} con código HTTP 204 No Content.
     */
    @DeleteMapping("/{empleadoId}/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desasignarVehiculo(
            @PathVariable Integer empleadoId,
            @PathVariable Integer vehiculoId) {
        log.info("Petición REST para quitar el vehículo ID {} del cargo del empleado ID {}", vehiculoId, empleadoId);
        empleadoService.desasignarVehiculo(empleadoId, vehiculoId);
        return ResponseEntity.noContent().build();
    }
}
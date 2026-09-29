package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoUpdate;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

/**
 * Controlador REST para la gestión integral de la entidad Vehículo.
 * Expone endpoints para la consulta de vehículos (accesibles por Administradores, Empleados y Socios)
 * y operaciones de creación, modificación y eliminación (exclusivas del rol ADMINISTRADOR).
 *
 * @author GuarderiaCentral
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoRestController {

    private final VehiculoService vehiculoService;

    /**
     * Obtiene el listado completo de vehículos activos registrados en el sistema.
     *
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarTodosLosVehiculos() {
        log.info("REST Request: Consulta para listar todos los vehículos");
        List<VehiculoResponse> vehiculos = vehiculoService.listarTodos();
        return ResponseEntity.ok(vehiculos);
    }

    /**
     * Busca y retorna la información de un vehículo según su identificador único.
     *
     * @param id Identificador numérico del vehículo.
     * @return {@link ResponseEntity} con los detalles del {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el vehículo no existe.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<VehiculoResponse> buscarVehiculoPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de vehículo por ID: {}", id);
        VehiculoResponse vehiculo = vehiculoService.buscarPorId(id);
        return ResponseEntity.ok(vehiculo);
    }

    /**
     * Obtiene el listado de vehículos asignados a garages ubicados dentro de una zona específica.
     *
     * @param zonaId Identificador numérico de la zona.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} de la zona y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si la zona no existe.
     */
    @GetMapping("/zona/{zonaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorZona(@PathVariable Integer zonaId) {
        log.info("REST Request: Consulta de vehículos en la zona ID: {}", zonaId);
        List<VehiculoResponse> vehiculos = vehiculoService.listarPorZona(zonaId);
        return ResponseEntity.ok(vehiculos);
    }

    /**
     * Registra un nuevo vehículo en el sistema.
     * Operación restringida exclusivamente al rol ADMINISTRADOR.
     *
     * @param request Objeto {@link VehiculoRequest} con los datos del vehículo a crear, validado mediante anotaciones Jakarta.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} creado y estado HTTP 201 (CREATED).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException Si la matrícula ya se encuentra registrada.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio o empleado indicado no existen.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> registrarVehiculo(@Valid @RequestBody VehiculoRequest request) {
        log.info("REST Request: Alta de nuevo vehículo con matrícula: {}", request.getMatricula());
        VehiculoResponse nuevoVehiculo = vehiculoService.registrarVehiculo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoVehiculo);
    }

    /**
     * Actualiza la información de un vehículo existente en el sistema.
     * Operación restringida exclusivamente al rol ADMINISTRADOR.
     *
     * @param id Identificador del vehículo a modificar.
     * @param update Objeto {@link VehiculoUpdate} validado con los nuevos datos a actualizar.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} actualizado y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el vehículo no existe.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException Si la nueva matrícula colisiona con otro registro.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> modificarVehiculo(
            @PathVariable Integer id,
            @Valid @RequestBody VehiculoUpdate update) {
        log.info("REST Request: Actualización de vehículo con ID: {}", id);
        VehiculoResponse vehiculoActualizado = vehiculoService.actualizarVehiculo(id, update);
        return ResponseEntity.ok(vehiculoActualizado);
    }

    /**
     * Realiza la baja lógica de un vehículo en el sistema marcándolo como inactivo.
     * Operación restringida exclusivamente al rol ADMINISTRADOR.
     *
     * @param id Identificador único del vehículo a dar de baja.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el vehículo no existe.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Integer id) {
        log.info("REST Request: Baja lógica de vehículo con ID: {}", id);
        vehiculoService.eliminarVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
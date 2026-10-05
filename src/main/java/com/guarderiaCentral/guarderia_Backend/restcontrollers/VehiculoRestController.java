package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
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
 * Controlador REST para la gestión de la entidad Vehículo.
 * Las consultas están abiertas a ADMINISTRADOR, EMPLEADO y SOCIO (solo registros activos);
 * el alta, la modificación, la baja y la consulta de inactivos son exclusivas del ADMINISTRADOR.
 * El rol SYSADMIN no tiene acceso a este recurso.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 * @version 1.1
 */
@Slf4j
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoRestController {

    private final VehiculoService vehiculoService;

    /**
     * Obtiene el listado de vehículos activos.
     *
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarTodosLosVehiculos() {
        log.info("REST Request: Consulta para listar todos los vehículos");
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    /**
     * Obtiene el listado completo de vehículos, incluyendo los dados de baja lógica.
     * Operación restringida al rol ADMINISTRADOR.
     *
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosIncluyendoInactivos() {
        log.info("REST Request: Consulta administrativa de vehículos incluyendo inactivos");
        return ResponseEntity.ok(vehiculoService.listarTodosIncluyendoInactivos());
    }

    /**
     * Busca un vehículo activo según su identificador único.
     *
     * @param id Identificador numérico del vehículo.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws RegistroNoEncontradoException Si el vehículo no existe o está inactivo.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<VehiculoResponse> buscarVehiculoPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de vehículo por ID: {}", id);
        return ResponseEntity.ok(vehiculoService.buscarPorId(id));
    }

    /**
     * Busca un vehículo activo según su matrícula.
     *
     * @param matricula Matrícula del vehículo.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con esa matrícula.
     */
    @GetMapping("/matricula/{matricula}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<VehiculoResponse> buscarVehiculoPorMatricula(@PathVariable String matricula) {
        log.info("REST Request: Consulta de vehículo por matrícula: {}", matricula);
        return ResponseEntity.ok(vehiculoService.buscarPorMatricula(matricula));
    }

    /**
     * Obtiene los vehículos activos de un tipo determinado.
     *
     * @param tipo Tipo de vehículo (MOTORHOME, CASA_RODANTE_DE_ARRASTRE, CARAVANA o TRAILER).
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorTipo(@PathVariable TipoVehiculo tipo) {
        log.info("REST Request: Consulta de vehículos por tipo: {}", tipo);
        return ResponseEntity.ok(vehiculoService.buscarPorTipo(tipo));
    }

    /**
     * Obtiene los vehículos activos pertenecientes a un socio.
     *
     * @param socioId Identificador numérico del socio propietario.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de vehículos para el socio ID: {}", socioId);
        return ResponseEntity.ok(vehiculoService.listarPorSocio(socioId));
    }

    /**
     * Registra un nuevo vehículo (o reactiva uno dado de baja con la misma matrícula).
     * Operación restringida al rol ADMINISTRADOR.
     *
     * @param request {@link VehiculoRequest} con los datos del vehículo, validado con Bean Validation.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} creado y estado HTTP 201 (CREATED).
     * @throws MatriculaDuplicadaException   Si la matrícula pertenece a un vehículo activo.
     * @throws RegistroNoEncontradoException Si el socio indicado no existe o está inactivo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> registrarVehiculo(@Valid @RequestBody VehiculoRequest request) {
        log.info("REST Request: Alta de nuevo vehículo con matrícula: {}", request.getMatricula());
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(request));
    }

    /**
     * Actualiza los datos de un vehículo activo.
     * Operación restringida al rol ADMINISTRADOR.
     *
     * @param id     Identificador del vehículo a modificar.
     * @param update {@link VehiculoUpdate} validado con los nuevos datos.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} actualizado y estado HTTP 200 (OK).
     * @throws RegistroNoEncontradoException Si el vehículo o el nuevo socio no existen o están inactivos.
     * @throws MatriculaDuplicadaException   Si la nueva matrícula pertenece a otro vehículo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> modificarVehiculo(
            @PathVariable Integer id,
            @Valid @RequestBody VehiculoUpdate update) {
        log.info("REST Request: Actualización de vehículo con ID: {}", id);
        return ResponseEntity.ok(vehiculoService.actualizar(id, update));
    }

    /**
     * Da de baja lógicamente un vehículo y libera su garage.
     * Operación restringida al rol ADMINISTRADOR.
     *
     * @param id Identificador único del vehículo a dar de baja.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     * @throws RegistroNoEncontradoException Si el vehículo no existe o ya está inactivo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Integer id) {
        log.info("REST Request: Baja lógica de vehículo con ID: {}", id);
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
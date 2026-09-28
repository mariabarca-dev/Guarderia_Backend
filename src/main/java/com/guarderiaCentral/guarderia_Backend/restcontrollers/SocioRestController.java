package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
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
 * Controlador REST para la gestión integral de la entidad Socio.
 * Proporciona endpoints para la administración de usuarios tipo Socio por parte del SYSADMIN,
 * así como consultas de información y activos asociados (vehículos y garages) para la gestión del sistema.
 *
 * @author GuarderiaCentral
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/socios")
@RequiredArgsConstructor
public class SocioRestController {

    private final SocioService socioService;

    /**
     * Obtiene el listado completo de todos los socios activos registrados en el sistema.
     * Accesible por SYSADMIN para administración de cuentas de usuario, y por ADMINISTRADOR, EMPLEADO y SOCIO
     * para operaciones de consulta autorizadas.
     *
     * @return {@link ResponseEntity} que contiene la lista de {@link SocioResponse} y el estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<SocioResponse>> listarTodosLosSocios() {
        log.info("REST Request: Consulta para listar todos los socios");
        List<SocioResponse> socios = socioService.listarTodos();
        return ResponseEntity.ok(socios);
    }

    /**
     * Busca y retorna un socio específico a través de su identificador único ID.
     *
     * @param id Identificador numérico del socio.
     * @return {@link ResponseEntity} con la información detallada del {@link SocioResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el ID no existe en el sistema.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<SocioResponse> buscarSocioPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de socio por ID: {}", id);
        SocioResponse socio = socioService.buscarPorId(id);
        return ResponseEntity.ok(socio);
    }

    /**
     * Busca y retorna la información de un socio utilizando su Documento Nacional de Identidad (DNI).
     *
     * @param dni Cadena de texto representando el DNI del socio a consultar.
     * @return {@link ResponseEntity} con el {@link SocioResponse} encontrado y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el DNI no corresponde a ningún registro activo.
     */
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<SocioResponse> buscarSocioPorDni(@PathVariable String dni) {
        log.info("REST Request: Consulta de socio por DNI: {}", dni);
        SocioResponse socio = socioService.buscarPorDni(dni);
        return ResponseEntity.ok(socio);
    }

    /**
     * Registra un nuevo socio en el sistema.
     * Operación exclusiva del rol SYSADMIN (gestión de usuarios del sistema).
     *
     * @param request Objeto {@link SocioRequest} con las validaciones de Beans aplicadas.
     * @return {@link ResponseEntity} con el {@link SocioResponse} creado y el estado HTTP 201 (CREATED).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException Si el DNI ya está registrado.
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<SocioResponse> registrarSocio(@Valid @RequestBody SocioRequest request) {
        log.info("REST Request: Alta de nuevo socio con DNI: {}", request.getDni());
        SocioResponse nuevoSocio = socioService.registrarSocio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSocio);
    }

    /**
     * Actualiza la información de un socio existente en el sistema.
     * Operación exclusiva del rol SYSADMIN.
     *
     * @param id Identificador numérico del socio a modificar.
     * @param update Objeto {@link SocioUpdate} validado con los nuevos datos a aplicar.
     * @return {@link ResponseEntity} con el {@link SocioResponse} actualizado y el estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<SocioResponse> modificarSocio(
            @PathVariable Integer id,
            @Valid @RequestBody SocioUpdate update) {
        log.info("REST Request: Actualización de socio con ID: {}", id);
        SocioResponse socioActualizado = socioService.actualizarSocio(id, update);
        return ResponseEntity.ok(socioActualizado);
    }

    /**
     * Ejecuta el borrado lógico de un socio en el sistema cambiando su estado a inactivo.
     * Operación exclusiva del rol SYSADMIN.
     *
     * @param id Identificador único del socio a dar de baja.
     * @return {@link ResponseEntity} con estado HTTP 204 (NO_CONTENT).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio no existe.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminarSocio(@PathVariable Integer id) {
        log.info("REST Request: Baja lógica de socio con ID: {}", id);
        socioService.eliminarSocio(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el listado de vehículos pertenecientes o asociados a un socio en particular.
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} asociados y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe.
     */
    @GetMapping("/{socioId}/vehiculos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de vehículos asociados al socio ID: {}", socioId);
        List<VehiculoResponse> vehiculos = socioService.listarVehiculosPorSocio(socioId);
        return ResponseEntity.ok(vehiculos);
    }

    /**
     * Obtiene el listado de garages/propiedades pertenecientes a un socio determinado.
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link GarageResponse} del socio y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe.
     */
    @GetMapping("/{socioId}/garages")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponse>> listarGarajesPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de garages pertenecientes al socio ID: {}", socioId);
        List<GarageResponse> garages = socioService.listarGarajesPorSocio(socioId);
        return ResponseEntity.ok(garages);
    }
}
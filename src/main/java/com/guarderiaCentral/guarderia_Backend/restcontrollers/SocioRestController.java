package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.GarageOcupadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
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
 * Controlador REST para la gestión y administración de la entidad Socio.
 * Proporciona endpoints para la gestión de las cuentas de socio por parte del ADMINISTRADOR y del SYSADMIN,
 * la consulta de los activos asociados a un socio (vehículos y garajes) por parte del ADMINISTRADOR y los
 * endpoints {@code /me}, que permiten al SOCIO consultar únicamente sus propios datos: el socio se identifica
 * por el token y nunca por un ID de la URL.
 *
 * @author GuarderiaCentral
 * @version 1.1
 */
@Slf4j
@RestController
@RequestMapping("/api/socios")
@RequiredArgsConstructor
public class SocioRestController {

    private final SocioService socioService;

    // ------------------------------------------------------------------
    // Endpoints del socio autenticado ("solo lo suyo")
    // ------------------------------------------------------------------

    /**
     * Devuelve el perfil del socio autenticado.
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param authentication Autenticación del usuario actual (el nombre sale del token).
     * @return {@link ResponseEntity} con el {@link SocioResponse} del socio y estado HTTP 200 (OK).
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<SocioResponse> miPerfil(Authentication authentication) {
        log.info("REST Request: Consulta del perfil del socio autenticado");
        return ResponseEntity.ok(socioService.buscarMiPerfil(authentication.getName()));
    }

    /**
     * Devuelve los vehículos del socio autenticado.
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/vehiculos")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> misVehiculos(Authentication authentication) {
        log.info("REST Request: Consulta de los vehículos del socio autenticado");
        return ResponseEntity.ok(socioService.listarMisVehiculos(authentication.getName()));
    }

    /**
     * Devuelve un vehículo del socio autenticado, verificando que sea suyo.
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param vehiculoId     Identificador del vehículo solicitado.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.AccesoDenegadoException Si el vehículo pertenece a otro socio (403).
     */
    @GetMapping("/me/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<VehiculoResponse> miVehiculo(@PathVariable Integer vehiculoId, Authentication authentication) {
        log.info("REST Request: Consulta del vehículo ID {} por el socio autenticado", vehiculoId);
        return ResponseEntity.ok(socioService.buscarMiVehiculo(vehiculoId, authentication.getName()));
    }

    /**
     * Devuelve los garages que el socio autenticado posee (fecha de compra).
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link PropiedadGarageResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/propiedades")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<PropiedadGarageResponse>> misPropiedades(Authentication authentication) {
        log.info("REST Request: Consulta de las propiedades de garage del socio autenticado");
        return ResponseEntity.ok(socioService.listarMisPropiedades(authentication.getName()));
    }

    /**
     * Devuelve los garages que ocupan los vehículos del socio autenticado, con lectura de luz,
     * servicio de mantenimiento y fecha de asignación.
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link GarageOcupadoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/garages-ocupados")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<GarageOcupadoResponse>> misGaragesOcupados(Authentication authentication) {
        log.info("REST Request: Consulta de los garages ocupados por los vehículos del socio autenticado");
        return ResponseEntity.ok(socioService.listarMisGaragesOcupados(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Gestión administrativa de socios
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado completo de todos los socios activos registrados en el sistema.
     * Accesible por roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} que contiene la lista de {@link SocioResponse} y el estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<SocioResponse>> listarTodosLosSocios() {
        log.info("REST Request: Consulta para listar todos los socios activos");
        List<SocioResponse> socios = socioService.listarTodos();
        return ResponseEntity.ok(socios);
    }

    /**
     * Obtiene el listado completo de socios, incluyendo aquellos en estado inactivo (borrado lógico).
     * Endpoint exclusivo para auditoría y administración por parte de los roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} que contiene la lista de {@link SocioResponse} y el estado HTTP 200 (OK).
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<SocioResponse>> listarSociosInactivos() {
        log.info("REST Request: Consulta de listado completo de socios incluyendo inactivos");
        List<SocioResponse> socios = socioService.listarTodosIncluyendoInactivos();
        return ResponseEntity.ok(socios);
    }

    /**
     * Busca y retorna un socio específico a través de su identificador único (ID).
     * Accesible por roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador numérico del socio.
     * @return {@link ResponseEntity} con la información detallada del {@link SocioResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el ID no existe o se encuentra inactivo.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> buscarSocioPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de socio por ID: {}", id);
        SocioResponse socio = socioService.buscarPorId(id);
        return ResponseEntity.ok(socio);
    }

    /**
     * Busca y retorna la información de un socio utilizando su Documento Nacional de Identidad (DNI).
     * Accesible por roles ADMINISTRADOR y SYSADMIN.
     *
     * @param dni Cadena de texto representando el DNI del socio a consultar.
     * @return {@link ResponseEntity} con el {@link SocioResponse} encontrado y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el DNI no corresponde a ningún registro activo.
     */
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> buscarSocioPorDni(@PathVariable String dni) {
        log.info("REST Request: Consulta de socio por DNI: {}", dni);
        SocioResponse socio = socioService.buscarPorDni(dni);
        return ResponseEntity.ok(socio);
    }

    /**
     * Registra un nuevo socio en el sistema o reactiva uno inactivo existente.
     * Operación exclusiva de los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param request Objeto {@link SocioRequest} validado.
     * @return {@link ResponseEntity} con el {@link SocioResponse} creado y estado HTTP 201 (CREATED).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException Si el DNI pertenece a otro socio activo.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException Si el nombre de usuario ya está registrado.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> registrarSocio(@Valid @RequestBody SocioRequest request) {
        log.info("REST Request: Alta de nuevo socio con DNI: {}", request.getDni());
        SocioResponse nuevoSocio = socioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSocio);
    }

    /**
     * Actualiza la información de un socio existente en el sistema.
     * Operación exclusiva de los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador numérico del socio a modificar.
     * @param update Objeto {@link SocioUpdate} validado con los nuevos datos a aplicar.
     * @return {@link ResponseEntity} con el {@link SocioResponse} actualizado y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> modificarSocio(
            @PathVariable Integer id,
            @Valid @RequestBody SocioUpdate update) {
        log.info("REST Request: Actualización de socio con ID: {}", id);
        SocioResponse socioActualizado = socioService.actualizar(id, update);
        return ResponseEntity.ok(socioActualizado);
    }

    /**
     * Ejecuta el borrado lógico de un socio en el sistema cambiando su estado a inactivo.
     * Operación exclusiva de los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador único del socio a dar de baja.
     * @return {@link ResponseEntity} con estado HTTP 204 (NO_CONTENT).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException Si el socio tiene vehículos activos o garajes a su nombre.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminarSocio(@PathVariable Integer id) {
        log.info("REST Request: Baja lógica de socio con ID: {}", id);
        socioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el listado de vehículos pertenecientes o asociados a un socio en particular.
     * Accesible solo por el rol ADMINISTRADOR (el socio usa {@code /me/vehiculos}).
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} asociados y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
     */
    @GetMapping("/{socioId}/vehiculos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de vehículos asociados al socio ID: {}", socioId);
        List<VehiculoResponse> vehiculos = socioService.listarVehiculosPorSocio(socioId);
        return ResponseEntity.ok(vehiculos);
    }

    /**
     * Obtiene el listado de garajes en propiedad pertenecientes a un socio determinado.
     * Accesible solo por el rol ADMINISTRADOR (el socio usa {@code /me/propiedades}).
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link PropiedadGarageResponse} del socio y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
     */
    @GetMapping("/{socioId}/garages")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PropiedadGarageResponse>> listarGarajesPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de propiedades de garaje pertenecientes al socio ID: {}", socioId);
        List<PropiedadGarageResponse> garajes = socioService.listarGarajesPorSocio(socioId);
        return ResponseEntity.ok(garajes);
    }
}
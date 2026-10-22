package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.GarageOcupadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.UsuarioService;
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
 * Controlador REST centralizado para todas las cuentas de usuario (socios, empleados y administradores).
 * <p>
 * Todas las rutas cuelgan de {@code /api/usuarios} y el tipo de cuenta lo define el segmento de la ruta
 * ({@code /socios}, {@code /empleados}, {@code /administradores}). Se organiza en cuatro grupos:
 * </p>
 * <ul>
 *     <li><b>Cuenta propia ({@code /me})</b>: cada usuario autenticado consulta solo lo suyo. Se identifica por la
 *     sesión y nunca por un ID de la URL. {@code /me} es para cualquier rol; {@code /me/socio/**} solo para SOCIO y
 *     {@code /me/empleado/**} solo para EMPLEADO.</li>
 *     <li><b>Consultas generales</b>: listado y búsqueda de cuentas sin importar el tipo (solo SYSADMIN).</li>
 *     <li><b>Gestión de cuentas</b>: alta, consulta, modificación y baja de socios, empleados y administradores
 *     (ADMINISTRADOR y SYSADMIN). La protección de las cuentas SYSADMIN frente al ADMINISTRADOR se resuelve en
 *     {@link AdministradorService} según quién llama.</li>
 *     <li><b>Consultas y asignaciones operativas</b>: vehículos y garages de un socio, zonas y vehículos a cargo de
 *     un empleado y su asignación (solo ADMINISTRADOR).</li>
 * </ul>
 *
 * @author GuarderiaCentral
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final SocioService socioService;
    private final EmpleadoService empleadoService;
    private final AdministradorService administradorService;

    // ------------------------------------------------------------------
    // Cuenta propia (cualquier rol autenticado)
    // ------------------------------------------------------------------

    /**
     * Devuelve los datos básicos del usuario autenticado. Permitido para cualquier usuario autenticado.
     * Se declara antes de {@code /{id}} para que la ruta fija no choque con el parámetro de ruta.
     *
     * @param authentication Autenticación del usuario actual (el nombre sale de la sesión).
     * @return {@link ResponseEntity} con el {@link UsuarioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> miUsuario(Authentication authentication) {
        log.info("REST Request: Consulta de los datos del usuario autenticado");
        return ResponseEntity.ok(usuarioService.buscarMiUsuario(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Socio autenticado ("solo lo suyo")
    // ------------------------------------------------------------------

    /**
     * Devuelve el perfil del socio autenticado.
     * Permitido exclusivamente para el rol SOCIO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link SocioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/socio")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<SocioResponse> miPerfilSocio(Authentication authentication) {
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
    @GetMapping("/me/socio/vehiculos")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<VehiculoResponse>> misVehiculosSocio(Authentication authentication) {
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
    @GetMapping("/me/socio/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<VehiculoResponse> miVehiculoSocio(@PathVariable Integer vehiculoId,
                                                            Authentication authentication) {
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
    @GetMapping("/me/socio/propiedades")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<PropiedadGarageResponse>> misPropiedadesSocio(Authentication authentication) {
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
    @GetMapping("/me/socio/garages-ocupados")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<List<GarageOcupadoResponse>> misGaragesOcupadosSocio(Authentication authentication) {
        log.info("REST Request: Consulta de los garages ocupados por los vehículos del socio autenticado");
        return ResponseEntity.ok(socioService.listarMisGaragesOcupados(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Empleado autenticado ("solo lo suyo")
    // ------------------------------------------------------------------

    /**
     * Devuelve el perfil del empleado autenticado.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/empleado")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<EmpleadoResponse> miPerfilEmpleado(Authentication authentication) {
        log.info("REST Request: Consulta del perfil del empleado autenticado");
        return ResponseEntity.ok(empleadoService.buscarMiPerfil(authentication.getName()));
    }

    /**
     * Devuelve las zonas asignadas al empleado autenticado, con el número de vehículos a cargo en cada una.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link AsignacionEmpleadoZonaResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/empleado/zonas")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> misZonasEmpleado(Authentication authentication) {
        log.info("REST Request: Consulta de las zonas del empleado autenticado");
        return ResponseEntity.ok(empleadoService.listarMisZonas(authentication.getName()));
    }

    /**
     * Devuelve los vehículos que tiene a cargo el empleado autenticado.
     * Permitido exclusivamente para el rol EMPLEADO.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me/empleado/vehiculos")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<VehiculoResponse>> misVehiculosEmpleado(Authentication authentication) {
        log.info("REST Request: Consulta de los vehículos a cargo del empleado autenticado");
        return ResponseEntity.ok(empleadoService.listarMisVehiculos(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Consultas generales (cualquier tipo de cuenta)
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado de todas las cuentas activas del sistema, sin importar su rol.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link UsuarioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        log.info("REST Request: Consulta de todos los usuarios activos");
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Obtiene el listado completo de cuentas, incluyendo las dadas de baja lógica.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link UsuarioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodosIncluyendoInactivos() {
        log.info("REST Request: Consulta de todos los usuarios incluyendo inactivos");
        return ResponseEntity.ok(usuarioService.listarTodosIncluyendoInactivos());
    }

    /**
     * Busca una cuenta activa por su identificador único.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id Identificador numérico de la cuenta.
     * @return {@link ResponseEntity} con el {@link UsuarioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de usuario por ID: {}", id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // ------------------------------------------------------------------
    // Socios: gestión de cuentas (ADMINISTRADOR y SYSADMIN)
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado de todos los socios activos.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link SocioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/socios")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<SocioResponse>> listarSocios() {
        log.info("REST Request: Consulta para listar todos los socios activos");
        return ResponseEntity.ok(socioService.listarTodos());
    }

    /**
     * Obtiene el listado completo de socios, incluyendo los dados de baja lógica.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link SocioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/socios/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<SocioResponse>> listarSociosInactivos() {
        log.info("REST Request: Consulta de listado completo de socios incluyendo inactivos");
        return ResponseEntity.ok(socioService.listarTodosIncluyendoInactivos());
    }

    /**
     * Busca un socio activo por su identificador único.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador numérico del socio.
     * @return {@link ResponseEntity} con el {@link SocioResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el ID no existe o está inactivo.
     */
    @GetMapping("/socios/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> buscarSocioPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de socio por ID: {}", id);
        return ResponseEntity.ok(socioService.buscarPorId(id));
    }

    /**
     * Busca un socio activo por su Documento Nacional de Identidad (DNI).
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param dni DNI del socio a consultar.
     * @return {@link ResponseEntity} con el {@link SocioResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el DNI no corresponde a un socio activo.
     */
    @GetMapping("/socios/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> buscarSocioPorDni(@PathVariable String dni) {
        log.info("REST Request: Consulta de socio por DNI: {}", dni);
        return ResponseEntity.ok(socioService.buscarPorDni(dni));
    }

    /**
     * Registra una nueva cuenta de socio (o reactiva una inactiva).
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param request {@link SocioRequest} validado.
     * @return {@link ResponseEntity} con el {@link SocioResponse} creado y estado HTTP 201 (CREATED).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException Si el DNI ya pertenece a otra cuenta.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException Si el nombre de usuario ya está registrado.
     */
    @PostMapping("/socios")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> crearSocio(@Valid @RequestBody SocioRequest request) {
        log.info("REST Request: Alta de socio con DNI: {}", request.getDni());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearSocio(request));
    }

    /**
     * Actualiza la cuenta de un socio.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id     Identificador del socio.
     * @param update {@link SocioUpdate} validado.
     * @return {@link ResponseEntity} con el {@link SocioResponse} actualizado y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @PutMapping("/socios/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<SocioResponse> actualizarSocio(
            @PathVariable Integer id,
            @Valid @RequestBody SocioUpdate update) {
        log.info("REST Request: Actualización de socio ID {}", id);
        return ResponseEntity.ok(usuarioService.actualizarSocio(id, update));
    }

    /**
     * Da de baja lógica la cuenta de un socio.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador del socio.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio no existe o está inactivo.
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException Si el socio tiene vehículos activos o garages a su nombre.
     */
    @DeleteMapping("/socios/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminarSocio(@PathVariable Integer id) {
        log.info("REST Request: Baja de socio ID {}", id);
        usuarioService.eliminarSocio(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Socios: consultas operativas (solo ADMINISTRADOR)
    // ------------------------------------------------------------------

    /**
     * Obtiene los vehículos asociados a un socio en particular.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el socio usa {@code /me/socio/vehiculos}).
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @GetMapping("/socios/{socioId}/vehiculos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de vehículos asociados al socio ID: {}", socioId);
        return ResponseEntity.ok(socioService.listarVehiculosPorSocio(socioId));
    }

    /**
     * Obtiene los garages en propiedad de un socio determinado.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el socio usa {@code /me/socio/propiedades}).
     *
     * @param socioId Identificador del socio a consultar.
     * @return {@link ResponseEntity} con la lista de {@link PropiedadGarageResponse} y estado HTTP 200 (OK).
     * @throws com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @GetMapping("/socios/{socioId}/garages")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PropiedadGarageResponse>> listarGaragesPorSocio(@PathVariable Integer socioId) {
        log.info("REST Request: Consulta de propiedades de garage del socio ID: {}", socioId);
        return ResponseEntity.ok(socioService.listarGarajesPorSocio(socioId));
    }

    // ------------------------------------------------------------------
    // Empleados: gestión de cuentas (ADMINISTRADOR y SYSADMIN)
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado de todos los empleados activos.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link EmpleadoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/empleados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> listarEmpleados() {
        log.info("REST Request: Consulta para listar todos los empleados activos");
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    /**
     * Obtiene el listado completo de empleados, incluyendo los dados de baja lógica.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @return {@link ResponseEntity} con la lista de {@link EmpleadoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/empleados/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<EmpleadoResponse>> listarEmpleadosInactivos() {
        log.info("REST Request: Consulta de listado completo de empleados incluyendo inactivos");
        return ResponseEntity.ok(empleadoService.listarTodosIncluyendoInactivos());
    }

    /**
     * Busca un empleado activo por su identificador único.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador del empleado.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/empleados/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> buscarEmpleadoPorId(@PathVariable Integer id) {
        log.info("REST Request: Consulta de empleado por ID: {}", id);
        return ResponseEntity.ok(empleadoService.buscarPorId(id));
    }

    /**
     * Registra una nueva cuenta de empleado (o reactiva una inactiva).
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param request {@link EmpleadoRequest} validado.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} creado y estado HTTP 201 (CREATED).
     */
    @PostMapping("/empleados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> crearEmpleado(@Valid @RequestBody EmpleadoRequest request) {
        log.info("REST Request: Alta de empleado con código: {}", request.getCodigo());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearEmpleado(request));
    }

    /**
     * Actualiza la cuenta de un empleado.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id     Identificador del empleado.
     * @param update {@link EmpleadoUpdate} validado.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} actualizado y estado HTTP 200 (OK).
     */
    @PutMapping("/empleados/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> actualizarEmpleado(
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoUpdate update) {
        log.info("REST Request: Actualización de empleado ID {}", id);
        return ResponseEntity.ok(usuarioService.actualizarEmpleado(id, update));
    }

    /**
     * Da de baja lógica la cuenta de un empleado.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id Identificador del empleado.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/empleados/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Integer id) {
        log.info("REST Request: Baja de empleado ID {}", id);
        usuarioService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Empleados: consultas y asignaciones operativas (solo ADMINISTRADOR)
    // ------------------------------------------------------------------

    /**
     * Obtiene las asignaciones de zona activas de un empleado específico.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el empleado usa {@code /me/empleado/zonas}).
     *
     * @param empleadoId Identificador del empleado.
     * @return {@link ResponseEntity} con la lista de {@link AsignacionEmpleadoZonaResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/empleados/{empleadoId}/zonas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponse>> listarZonasAsignadas(@PathVariable Integer empleadoId) {
        log.info("REST Request: Consulta de las zonas asignadas al empleado ID: {}", empleadoId);
        return ResponseEntity.ok(empleadoService.listarZonasAsignadas(empleadoId));
    }

    /**
     * Obtiene los vehículos que un empleado específico tiene a cargo.
     * Permitido exclusivamente para el rol ADMINISTRADOR (el empleado usa {@code /me/empleado/vehiculos}).
     *
     * @param empleadoId Identificador del empleado.
     * @return {@link ResponseEntity} con la lista de {@link VehiculoResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/empleados/{empleadoId}/vehiculos-a-cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculosBajoResponsabilidad(@PathVariable Integer empleadoId) {
        log.info("REST Request: Consulta de los vehículos a cargo del empleado ID: {}", empleadoId);
        return ResponseEntity.ok(empleadoService.listarVehiculosBajoResponsabilidad(empleadoId));
    }

    /**
     * Asigna un vehículo a un empleado. El vehículo debe estar en una zona a la que el empleado esté asignado,
     * y el empleado no puede superar la cantidad de vehículos a cargo definida para esa zona.
     * Permitido exclusivamente para el rol ADMINISTRADOR.
     *
     * @param empleadoId Identificador del empleado.
     * @param vehiculoId Identificador del vehículo.
     * @return {@link ResponseEntity} con el {@link VehiculoResponse} asignado y estado HTTP 200 (OK).
     */
    @PutMapping("/empleados/{empleadoId}/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponse> asignarVehiculo(
            @PathVariable Integer empleadoId,
            @PathVariable Integer vehiculoId) {
        log.info("REST Request: Asignación del vehículo ID {} al empleado ID {}", vehiculoId, empleadoId);
        return ResponseEntity.ok(empleadoService.asignarVehiculo(empleadoId, vehiculoId));
    }

    /**
     * Quita un vehículo del cargo de un empleado.
     * Permitido exclusivamente para el rol ADMINISTRADOR.
     *
     * @param empleadoId Identificador del empleado.
     * @param vehiculoId Identificador del vehículo.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/empleados/{empleadoId}/vehiculos/{vehiculoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desasignarVehiculo(
            @PathVariable Integer empleadoId,
            @PathVariable Integer vehiculoId) {
        log.info("REST Request: Baja de la asignación del vehículo ID {} al empleado ID {}", vehiculoId, empleadoId);
        empleadoService.desasignarVehiculo(empleadoId, vehiculoId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Administradores: gestión de cuentas (ADMINISTRADOR y SYSADMIN)
    // ------------------------------------------------------------------

    /**
     * Obtiene el listado de administradores activos.
     * Si quien consulta es un ADMINISTRADOR, el listado no incluye las cuentas SYSADMIN.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param authentication Autenticación del usuario actual (el nombre sale de la sesión).
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/administradores")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarAdministradores(Authentication authentication) {
        log.info("REST Request: Consulta del listado de administradores activos");
        return ResponseEntity.ok(administradorService.listarTodos(authentication.getName()));
    }

    /**
     * Obtiene el listado completo de administradores, incluyendo los dados de baja lógica.
     * Si quien consulta es un ADMINISTRADOR, el listado no incluye las cuentas SYSADMIN.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/administradores/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarAdministradoresInactivos(Authentication authentication) {
        log.info("REST Request: Consulta del listado de administradores incluyendo inactivos");
        return ResponseEntity.ok(administradorService.listarTodosIncluyendoInactivos(authentication.getName()));
    }

    /**
     * Busca un administrador activo por su identificador único.
     * Un ADMINISTRADOR no puede consultar cuentas SYSADMIN (403).
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id             Identificador del administrador.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/administradores/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> buscarAdministradorPorId(@PathVariable Integer id,
                                                                          Authentication authentication) {
        log.info("REST Request: Consulta de administrador por ID: {}", id);
        return ResponseEntity.ok(administradorService.buscarPorId(id, authentication.getName()));
    }

    /**
     * Registra una nueva cuenta de administrador (o reactiva una inactiva). La cuenta creada siempre es de rol
     * ADMINISTRADOR.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param request {@link AdministradorRequest} validado.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} creado y estado HTTP 201 (CREATED).
     */
    @PostMapping("/administradores")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> crearAdministrador(@Valid @RequestBody AdministradorRequest request) {
        log.info("REST Request: Alta de administrador con usuario: {}", request.getNombreUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearAdministrador(request));
    }

    /**
     * Actualiza la cuenta de un administrador.
     * Un ADMINISTRADOR no puede modificar cuentas SYSADMIN (403).
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id             Identificador del administrador.
     * @param update         {@link AdministradorUpdate} validado.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} actualizado y estado HTTP 200 (OK).
     */
    @PutMapping("/administradores/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> actualizarAdministrador(
            @PathVariable Integer id,
            @Valid @RequestBody AdministradorUpdate update,
            Authentication authentication) {
        log.info("REST Request: Actualización de administrador ID {}", id);
        return ResponseEntity.ok(usuarioService.actualizarAdministrador(id, update, authentication.getName()));
    }

    /**
     * Da de baja lógica la cuenta de un administrador.
     * Un ADMINISTRADOR no puede dar de baja cuentas SYSADMIN (403) y el SYSADMIN no puede dar de baja la suya.
     * Permitido para los roles ADMINISTRADOR y SYSADMIN.
     *
     * @param id             Identificador del administrador.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/administradores/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Integer id, Authentication authentication) {
        log.info("REST Request: Baja de administrador ID {}", id);
        usuarioService.eliminarAdministrador(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
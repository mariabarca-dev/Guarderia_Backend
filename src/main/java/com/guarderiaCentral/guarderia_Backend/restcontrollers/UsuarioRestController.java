package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioResponse;
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
 * Controlador REST para las cuentas de usuario.
 * Expone {@code /me}, abierto a cualquier usuario autenticado, para que cada uno consulte sus propios datos
 * básicos sin importar su rol. El resto es la vía unificada del SYSADMIN para consultar todas las cuentas y para
 * crear, modificar y dar de baja socios, empleados y administradores: el tipo de cuenta lo define la ruta y
 * cada operación se delega en {@link UsuarioService}. Esas operaciones son exclusivas del rol SYSADMIN.
 *
 * @author GuarderiaCentral
 * @version 1.1
 */
@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService usuarioService;

    // ------------------------------------------------------------------
    // Usuario autenticado (cualquier rol)
    // ------------------------------------------------------------------

    /**
     * Devuelve los datos básicos del usuario autenticado. Permitido para cualquier usuario autenticado.
     * Se declara antes de {@code /{id}} para que la ruta fija no choque con el parámetro de ruta.
     *
     * @param authentication Autenticación del usuario actual (el nombre sale del token).
     * @return {@link ResponseEntity} con el {@link UsuarioResponse} y estado HTTP 200 (OK).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> miUsuario(Authentication authentication) {
        log.info("REST Request: Consulta de los datos del usuario autenticado");
        return ResponseEntity.ok(usuarioService.buscarMiUsuario(authentication.getName()));
    }

    // ------------------------------------------------------------------
    // Consultas generales
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
    // Socios
    // ------------------------------------------------------------------

    /**
     * Registra una nueva cuenta de socio (o reactiva una inactiva).
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param request {@link SocioRequest} validado.
     * @return {@link ResponseEntity} con el {@link SocioResponse} creado y estado HTTP 201 (CREATED).
     */
    @PostMapping("/socios")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<SocioResponse> crearSocio(@Valid @RequestBody SocioRequest request) {
        log.info("REST Request: Alta de socio por SYSADMIN con DNI: {}", request.getDni());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearSocio(request));
    }

    /**
     * Actualiza la cuenta de un socio.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id     Identificador del socio.
     * @param update {@link SocioUpdate} validado.
     * @return {@link ResponseEntity} con el {@link SocioResponse} actualizado y estado HTTP 200 (OK).
     */
    @PutMapping("/socios/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<SocioResponse> actualizarSocio(
            @PathVariable Integer id,
            @Valid @RequestBody SocioUpdate update) {
        log.info("REST Request: Actualización de socio ID {} por SYSADMIN", id);
        return ResponseEntity.ok(usuarioService.actualizarSocio(id, update));
    }

    /**
     * Da de baja lógica la cuenta de un socio.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id Identificador del socio.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/socios/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminarSocio(@PathVariable Integer id) {
        log.info("REST Request: Baja de socio ID {} por SYSADMIN", id);
        usuarioService.eliminarSocio(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Empleados
    // ------------------------------------------------------------------

    /**
     * Registra una nueva cuenta de empleado (o reactiva una inactiva).
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param request {@link EmpleadoRequest} validado.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} creado y estado HTTP 201 (CREATED).
     */
    @PostMapping("/empleados")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> crearEmpleado(@Valid @RequestBody EmpleadoRequest request) {
        log.info("REST Request: Alta de empleado por SYSADMIN con código: {}", request.getCodigo());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearEmpleado(request));
    }

    /**
     * Actualiza la cuenta de un empleado.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id     Identificador del empleado.
     * @param update {@link EmpleadoUpdate} validado.
     * @return {@link ResponseEntity} con el {@link EmpleadoResponse} actualizado y estado HTTP 200 (OK).
     */
    @PutMapping("/empleados/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<EmpleadoResponse> actualizarEmpleado(
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoUpdate update) {
        log.info("REST Request: Actualización de empleado ID {} por SYSADMIN", id);
        return ResponseEntity.ok(usuarioService.actualizarEmpleado(id, update));
    }

    /**
     * Da de baja lógica la cuenta de un empleado.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id Identificador del empleado.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/empleados/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Integer id) {
        log.info("REST Request: Baja de empleado ID {} por SYSADMIN", id);
        usuarioService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Administradores
    // ------------------------------------------------------------------

    /**
     * Registra una nueva cuenta de administrador (o reactiva una inactiva).
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param request {@link AdministradorRequest} validado.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} creado y estado HTTP 201 (CREATED).
     */
    @PostMapping("/administradores")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AdministradorResponse> crearAdministrador(@Valid @RequestBody AdministradorRequest request) {
        log.info("REST Request: Alta de administrador por SYSADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearAdministrador(request));
    }

    /**
     * Actualiza la cuenta de un administrador.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id     Identificador del administrador.
     * @param update {@link AdministradorUpdate} validado.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} actualizado y estado HTTP 200 (OK).
     */
    @PutMapping("/administradores/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AdministradorResponse> actualizarAdministrador(
            @PathVariable Integer id,
            @Valid @RequestBody AdministradorUpdate update) {
        log.info("REST Request: Actualización de administrador ID {} por SYSADMIN", id);
        return ResponseEntity.ok(usuarioService.actualizarAdministrador(id, update));
    }

    /**
     * Da de baja lógica la cuenta de un administrador.
     * Permitido exclusivamente para el rol SYSADMIN.
     *
     * @param id Identificador del administrador.
     * @return {@link ResponseEntity} sin contenido y estado HTTP 204 (NO_CONTENT).
     */
    @DeleteMapping("/administradores/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Integer id) {
        log.info("REST Request: Baja de administrador ID {} por SYSADMIN", id);
        usuarioService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}
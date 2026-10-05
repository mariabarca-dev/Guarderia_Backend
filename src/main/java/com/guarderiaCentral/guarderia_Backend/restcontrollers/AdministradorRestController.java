package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
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
 * Controlador RESTful para la gestión exclusiva de cuentas de Administradores del sistema.
 * <p>
 * De acuerdo a la matriz de permisos de la arquitectura, la gestión de usuarios
 * del sistema es acceso exclusivo y limitado para el rol <b>SYSADMIN</b>.
 * </p>
 *
 * @author Cátedra Guardería Central
 */
@Slf4j
@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorRestController {

    private final AdministradorService administradorService;

    /**
     * Registra un nuevo administrador en el sistema o reactiva uno inactivo existente.
     * <p>
     * Requiere el rol exclusivo <b>SYSADMIN</b>.
     * </p>
     *
     * @param request DTO con los datos para la creación del administrador.
     * @return {@link ResponseEntity} conteniendo el {@link AdministradorResponse} y el código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AdministradorResponse> registrarAdministrador(@Valid @RequestBody AdministradorRequest request) {
        log.info("REST Request para registrar un nuevo Administrador con usuario: {}", request.getNombreUsuario());
        AdministradorResponse nuevoAdmin = administradorService.crear(request);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

    /**
     * Obtiene el listado completo de administradores activos del sistema.
     * <p>
     * Accesible por el rol <b>SYSADMIN</b>.
     * </p>
     *
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} activos y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarTodos() {
        log.info("REST Request para obtener el listado de administradores activos.");
        List<AdministradorResponse> administradores = administradorService.listarTodos();
        return ResponseEntity.ok(administradores);
    }

    /**
     * Obtiene el listado completo de administradores incluyendo registros inactivos.
     * <p>
     * Accesible exclusivamente por el rol <b>SYSADMIN</b>.
     * </p>
     *
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} y código HTTP 200 OK.
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarTodosIncluyendoInactivos() {
        log.info("REST Request para obtener el listado de administradores incluyendo inactivos.");
        List<AdministradorResponse> administradores = administradorService.listarTodosIncluyendoInactivos();
        return ResponseEntity.ok(administradores);
    }

    /**
     * Obtiene los detalles de un administrador activo por su identificador.
     * <p>
     * Accesible por el rol <b>SYSADMIN</b>.
     * </p>
     *
     * @param id Identificador único del administrador a consultar.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} encontrado y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AdministradorResponse> buscarPorId(@PathVariable Integer id) {
        log.info("REST Request para obtener el administrador con ID: {}", id);
        AdministradorResponse admin = administradorService.buscarPorId(id);
        return ResponseEntity.ok(admin);
    }

    /**
     * Actualiza la información general de un administrador existente.
     * <p>
     * Requiere el rol exclusivo <b>SYSADMIN</b>.
     * </p>
     *
     * @param id     Identificador único del administrador a actualizar.
     * @param update DTO con la información actualizada.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AdministradorResponse> actualizarAdministrador(
            @PathVariable Integer id,
            @Valid @RequestBody AdministradorUpdate update) {
        log.info("REST Request para actualizar el administrador con ID: {}", id);
        AdministradorResponse adminActualizado = administradorService.actualizar(id, update);
        return ResponseEntity.ok(adminActualizado);
    }

    /**
     * Ejecuta el borrado lógico de un administrador cambiando su estado activo a {@code false}.
     * <p>
     * Requiere el rol exclusivo <b>SYSADMIN</b>.
     * </p>
     *
     * @param id Identificador único del administrador a desactivar.
     * @return {@link ResponseEntity} con el código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Integer id) {
        log.info("REST Request para realizar el borrado lógico del administrador con ID: {}", id);
        administradorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
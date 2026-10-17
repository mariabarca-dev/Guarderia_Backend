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
 * Controlador RESTful para la gestión de cuentas de Administradores del sistema.
 * <p>
 * Todos los endpoints son accesibles por los roles <b>ADMINISTRADOR</b> y <b>SYSADMIN</b>. La diferencia
 * se resuelve en el service según quién llama: el SYSADMIN ve y gestiona todas las cuentas, mientras que
 * el ADMINISTRADOR no puede ver, modificar ni dar de baja cuentas con rol SYSADMIN.
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 1.1
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
     * Requiere el rol <b>ADMINISTRADOR</b> o <b>SYSADMIN</b>. La cuenta creada siempre es de rol ADMINISTRADOR.
     * </p>
     *
     * @param request DTO con los datos para la creación del administrador.
     * @return {@link ResponseEntity} conteniendo el {@link AdministradorResponse} y el código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> registrarAdministrador(@Valid @RequestBody AdministradorRequest request) {
        log.info("REST Request para registrar un nuevo Administrador con usuario: {}", request.getNombreUsuario());
        AdministradorResponse nuevoAdmin = administradorService.crear(request);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

    /**
     * Obtiene el listado de administradores activos.
     * <p>
     * Si quien consulta es un ADMINISTRADOR, el listado no incluye las cuentas SYSADMIN.
     * </p>
     *
     * @param authentication Autenticación del usuario actual (el nombre sale del token).
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} activos y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarTodos(Authentication authentication) {
        log.info("REST Request para obtener el listado de administradores activos.");
        List<AdministradorResponse> administradores = administradorService.listarTodos(authentication.getName());
        return ResponseEntity.ok(administradores);
    }

    /**
     * Obtiene el listado completo de administradores incluyendo registros inactivos.
     * <p>
     * Si quien consulta es un ADMINISTRADOR, el listado no incluye las cuentas SYSADMIN.
     * </p>
     *
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con la lista de {@link AdministradorResponse} y código HTTP 200 OK.
     */
    @GetMapping("/inactivos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<List<AdministradorResponse>> listarTodosIncluyendoInactivos(Authentication authentication) {
        log.info("REST Request para obtener el listado de administradores incluyendo inactivos.");
        List<AdministradorResponse> administradores =
                administradorService.listarTodosIncluyendoInactivos(authentication.getName());
        return ResponseEntity.ok(administradores);
    }

    /**
     * Obtiene los detalles de un administrador activo por su identificador.
     * <p>
     * Un ADMINISTRADOR no puede consultar cuentas SYSADMIN (403).
     * </p>
     *
     * @param id             Identificador único del administrador a consultar.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} encontrado y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> buscarPorId(@PathVariable Integer id, Authentication authentication) {
        log.info("REST Request para obtener el administrador con ID: {}", id);
        AdministradorResponse admin = administradorService.buscarPorId(id, authentication.getName());
        return ResponseEntity.ok(admin);
    }

    /**
     * Actualiza la información general de un administrador existente.
     * <p>
     * Un ADMINISTRADOR no puede modificar cuentas SYSADMIN (403).
     * </p>
     *
     * @param id             Identificador único del administrador a actualizar.
     * @param update         DTO con la información actualizada.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el {@link AdministradorResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<AdministradorResponse> actualizarAdministrador(
            @PathVariable Integer id,
            @Valid @RequestBody AdministradorUpdate update,
            Authentication authentication) {
        log.info("REST Request para actualizar el administrador con ID: {}", id);
        AdministradorResponse adminActualizado = administradorService.actualizar(id, update, authentication.getName());
        return ResponseEntity.ok(adminActualizado);
    }

    /**
     * Ejecuta el borrado lógico de un administrador cambiando su estado activo a {@code false}.
     * <p>
     * Un ADMINISTRADOR no puede dar de baja cuentas SYSADMIN (403).
     * </p>
     *
     * @param id             Identificador único del administrador a desactivar.
     * @param authentication Autenticación del usuario actual.
     * @return {@link ResponseEntity} con el código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SYSADMIN')")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Integer id, Authentication authentication) {
        log.info("REST Request para realizar el borrado lógico del administrador con ID: {}", id);
        administradorService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.CredencialesInvalidasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.SysAdminProtegidoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link UsuarioService} para la autenticación y administración centralizada de cuentas.
 * Delega la lógica de negocio y guardado inteligente en {@link SocioService}, {@link EmpleadoService}
 * y {@link AdministradorService} para garantizar un único camino de validación por tipo de cuenta.
 *
 * @author GuarderiaCentral
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SocioService socioService;
    private final EmpleadoService empleadoService;
    private final AdministradorService administradorService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario validarLogin(String nombreUsuario, String clave) throws CredencialesInvalidasException {
        log.info("Intentando validar credenciales para el usuario: {}", nombreUsuario);

        Usuario u = usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("Fallo de autenticación: Usuario '{}' no encontrado o inactivo.", nombreUsuario);
                    return new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
                });

        if (!passwordEncoder.matches(clave, u.getClave())) {
            log.warn("Fallo de autenticación: Contraseña incorrecta para el usuario '{}'.", nombreUsuario);
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }

        log.info("Autenticación exitosa para el usuario: {}", nombreUsuario);
        return u;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tieneRol(Usuario usuario, Rol rolRequerido) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        return usuario.getRol() == rolRequerido;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) throws RegistroNoEncontradoException {
        log.info("Buscando usuario activo con ID: {}", id);
        return usuarioRepository.findById(id)
                .filter(Usuario::getActivo)
                .orElseThrow(() -> {
                    log.warn("No se encontró el usuario activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        log.info("Listando todos los usuarios activos.");
        return usuarioRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodosIncluyendoInactivos() {
        log.info("Listando todos los usuarios (incluyendo inactivos).");
        return usuarioRepository.listarTodosIncluyendoInactivos();
    }

    // --- Métodos delegados para gestión de Socios ---

    @Override
    @Transactional
    public SocioResponse crearSocio(SocioRequest request) {
        log.info("UsuarioService delegando creación de socio a SocioService.");
        return socioService.crear(request);
    }

    @Override
    @Transactional
    public SocioResponse actualizarSocio(Integer id, SocioUpdate update) {
        log.info("UsuarioService delegando actualización de socio ID {} a SocioService.", id);
        return socioService.actualizar(id, update);
    }

    @Override
    @Transactional
    public void eliminarSocio(Integer id) {
        log.info("UsuarioService delegando eliminación de socio ID {} a SocioService.", id);
        socioService.eliminar(id);
    }

    // --- Métodos delegados para gestión de Empleados ---

    @Override
    @Transactional
    public EmpleadoResponse crearEmpleado(EmpleadoRequest request) {
        log.info("UsuarioService delegando creación de empleado a EmpleadoService.");
        return empleadoService.crear(request);
    }

    @Override
    @Transactional
    public EmpleadoResponse actualizarEmpleado(Integer id, EmpleadoUpdate update) {
        log.info("UsuarioService delegando actualización de empleado ID {} a EmpleadoService.", id);
        return empleadoService.actualizar(id, update);
    }

    @Override
    @Transactional
    public void eliminarEmpleado(Integer id) {
        log.info("UsuarioService delegando eliminación de empleado ID {} a EmpleadoService.", id);
        empleadoService.eliminar(id);
    }

    // --- Métodos delegados para gestión de Administradores ---

    @Override
    @Transactional
    public AdministradorResponse crearAdministrador(AdministradorRequest request) {
        log.info("UsuarioService delegando creación de administrador a AdministradorService.");
        return administradorService.crear(request);
    }

    @Override
    @Transactional
    public AdministradorResponse actualizarAdministrador(Integer id, AdministradorUpdate update) throws SysAdminProtegidoException {
        log.info("UsuarioService delegando actualización de administrador ID {} a AdministradorService.", id);
        validarNoEsSysAdmin(id);
        return administradorService.actualizar(id, update);
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Integer id) throws SysAdminProtegidoException {
        log.info("UsuarioService delegando eliminación de administrador ID {} a AdministradorService.", id);
        validarNoEsSysAdmin(id);
        administradorService.eliminar(id);
    }

    /**
     * Valida que la cuenta objetivo no tenga rol SYSADMIN para proteger cuentas del sistema.
     *
     * @param id Identificador único del usuario.
     * @throws SysAdminProtegidoException Si el usuario objetivo es SYSADMIN.
     */
    private void validarNoEsSysAdmin(Integer id) {
        Usuario u = buscarPorId(id);
        if (u.getRol() == Rol.SYSADMIN) {
            throw new SysAdminProtegidoException("No se permite realizar esta operación sobre un usuario con rol SYSADMIN.");
        }
    }
}
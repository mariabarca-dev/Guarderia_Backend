package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioResponse;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de {@link UsuarioService} para la consulta y administración centralizada de cuentas.
 * Delega la lógica de negocio y guardado inteligente en {@link SocioService}, {@link EmpleadoService}
 * y {@link AdministradorService} para garantizar un único camino de validación por tipo de cuenta.
 * Las consultas devuelven {@link UsuarioResponse}, que nunca incluye la clave.
 * La protección de las cuentas SYSADMIN frente al ADMINISTRADOR vive en {@link AdministradorService}.
 *
 * @author GuarderiaCentral
 * @version 1.4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SocioRepository socioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AdministradorRepository administradorRepository;
    private final SocioService socioService;
    private final EmpleadoService empleadoService;
    private final AdministradorService administradorService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) throws RegistroNoEncontradoException {
        log.info("Buscando usuario activo con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::getActivo)
                .orElseThrow(() -> {
                    log.warn("No se encontró el usuario activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id);
                });
        return usuarioRepository.fromEntity(usuario);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarMiUsuario(String nombreUsuario) throws RegistroNoEncontradoException {
        log.info("Consultando los datos del usuario autenticado: {}", nombreUsuario);
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("No se encontró el usuario autenticado '{}'.", nombreUsuario);
                    return new RegistroNoEncontradoException("No se encontró el usuario autenticado.");
                });
        return usuarioRepository.fromEntity(usuario);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        log.info("Listando todos los usuarios activos.");
        return usuarioRepository.findAll().stream()
                .map(usuarioRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Como la herencia es JOINED, la lista se arma juntando las cuentas de cada subtipo: sus consultas
     * nativas ya hacen el JOIN con la tabla raíz y no filtran por borrado lógico. Las cuentas SYSADMIN
     * están incluidas entre los administradores. El resultado se ordena por ID.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodosIncluyendoInactivos() {
        log.info("Listando todos los usuarios (incluyendo inactivos).");
        List<Usuario> todos = new ArrayList<>();
        todos.addAll(socioRepository.findAllIncludingInactive());
        todos.addAll(empleadoRepository.findAllIncludingInactive());
        todos.addAll(administradorRepository.findAllIncludingInactive());
        return todos.stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(usuarioRepository::fromEntity)
                .collect(Collectors.toList());
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
    public AdministradorResponse actualizarAdministrador(Integer id, AdministradorUpdate update, String nombreUsuarioLlamador) {
        log.info("UsuarioService delegando actualización de administrador ID {} a AdministradorService.", id);
        return administradorService.actualizar(id, update, nombreUsuarioLlamador);
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Integer id, String nombreUsuarioLlamador) {
        log.info("UsuarioService delegando eliminación de administrador ID {} a AdministradorService.", id);
        administradorService.eliminar(id, nombreUsuarioLlamador);
    }
}
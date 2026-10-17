package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
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

import java.util.List;

/**
 * Interfaz de servicio que define las operaciones transversales de consulta de usuarios y la
 * administración centralizada de cuentas por parte del SYSADMIN. Además permite a cualquier usuario
 * autenticado consultar sus propios datos básicos, sin importar su rol.
 * Delega la gestión específica de cuentas en SocioService, EmpleadoService y AdministradorService.
 *
 * @author GuarderiaCentral
 * @version 1.3
 */
public interface UsuarioService {

    /**
     * Busca una cuenta de usuario activa por su identificador único en la tabla raíz.
     *
     * @param id Identificador único del usuario.
     * @return {@link UsuarioResponse} con los datos de la cuenta (sin clave).
     * @throws RegistroNoEncontradoException Si no existe un usuario activo con dicho ID.
     */
    UsuarioResponse buscarPorId(Integer id) throws RegistroNoEncontradoException;

    /**
     * Devuelve los datos básicos del usuario autenticado, sea cual sea su rol.
     *
     * @param nombreUsuario Nombre de usuario del autenticado (viene del token, no del cliente).
     * @return {@link UsuarioResponse} con los datos de la cuenta (sin clave).
     * @throws RegistroNoEncontradoException Si no existe un usuario activo con ese nombre de usuario.
     */
    UsuarioResponse buscarMiUsuario(String nombreUsuario) throws RegistroNoEncontradoException;

    /**
     * Recupera el listado completo de todos los usuarios activos del sistema sin importar su rol.
     *
     * @return Lista de {@link UsuarioResponse} activos.
     */
    List<UsuarioResponse> listarTodos();

    /**
     * Recupera el listado completo de usuarios registrados, incluyendo aquellos en estado inactivo.
     * Uso exclusivo para tareas de auditoría por parte del SYSADMIN.
     *
     * @return Lista de {@link UsuarioResponse} activos e inactivos.
     */
    List<UsuarioResponse> listarTodosIncluyendoInactivos();

    // --- Métodos delegados para gestión de Socios desde UsuarioRestController ---

    SocioResponse crearSocio(SocioRequest request);

    SocioResponse actualizarSocio(Integer id, SocioUpdate update);

    void eliminarSocio(Integer id);

    // --- Métodos delegados para gestión de Empleados desde UsuarioRestController ---

    EmpleadoResponse crearEmpleado(EmpleadoRequest request);

    EmpleadoResponse actualizarEmpleado(Integer id, EmpleadoUpdate update);

    void eliminarEmpleado(Integer id);

    // --- Métodos delegados para gestión de Administradores desde UsuarioRestController ---

    AdministradorResponse crearAdministrador(AdministradorRequest request);

    AdministradorResponse actualizarAdministrador(Integer id, AdministradorUpdate update, String nombreUsuarioLlamador);

    void eliminarAdministrador(Integer id, String nombreUsuarioLlamador);
}
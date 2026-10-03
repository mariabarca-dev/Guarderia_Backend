package com.guarderiaCentral.guarderia_Backend.services;

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

import java.util.List;

/**
 * Interfaz de servicio que define las operaciones transversales de autenticación,
 * verificación de roles y administración centralizada de usuarios por parte del SYSADMIN.
 * Delega la gestión específica de cuentas en SocioService, EmpleadoService y AdministradorService.
 *
 * @author GuarderiaCentral
 * @version 1.0
 */
public interface UsuarioService {

    /**
     * Valida las credenciales de un usuario contra la base de datos comparando su clave encriptada.
     *
     * @param nombreUsuario Nombre de usuario o identificador de acceso.
     * @param clave Clave en texto plano proporcionada.
     * @return Entidad {@link Usuario} autenticada.
     * @throws CredencialesInvalidasException Si el usuario no existe, está inactivo o la contraseña es incorrecta.
     */
    Usuario validarLogin(String nombreUsuario, String clave) throws CredencialesInvalidasException;

    /**
     * Verifica si un usuario posee un rol determinado.
     *
     * @param usuario Instancia del usuario a verificar.
     * @param rolRequerido Rol contra el cual se compara.
     * @return {@code true} si el usuario posee el rol solicitado; {@code false} en caso contrario.
     */
    boolean tieneRol(Usuario usuario, Rol rolRequerido);

    /**
     * Busca una cuenta de usuario activa por su identificador único en la tabla raíz.
     *
     * @param id Identificador único del usuario.
     * @return Entidad {@link Usuario} encontrada.
     * @throws RegistroNoEncontradoException Si no existe un usuario activo con dicho ID.
     */
    Usuario buscarPorId(Integer id) throws RegistroNoEncontradoException;

    /**
     * Recupera el listado completo de todos los usuarios activos del sistema sin importar su rol.
     *
     * @return Lista de entidades {@link Usuario} activas.
     */
    List<Usuario> listarTodos();

    /**
     * Recupera el listado completo de usuarios registrados, incluyendo aquellos en estado inactivo.
     * Uso exclusivo para tareas de auditoría por parte del SYSADMIN.
     *
     * @return Lista de entidades {@link Usuario} activas e inactivas.
     */
    List<Usuario> listarTodosIncluyendoInactivos();

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

    AdministradorResponse actualizarAdministrador(Integer id, AdministradorUpdate update) throws SysAdminProtegidoException;

    void eliminarAdministrador(Integer id) throws SysAdminProtegidoException;
}
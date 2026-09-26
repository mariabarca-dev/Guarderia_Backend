package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.CredencialesInvalidasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;

import java.util.List;

/**
 * Interfaz de servicio que define las operaciones de negocio para la gestión de {@link Usuario}.
 *
 * @author GuarderiaCentral
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
     * Busca un usuario activo por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario a buscar.
     * @return {@link UsuarioDTO} con los datos limpios.
     * @throws RegistroNoEncontradoException Si no se encuentra un usuario activo con dicho nombre.
     */
    UsuarioDTO buscarPorNombreUsuario(String nombreUsuario) throws RegistroNoEncontradoException;

    /**
     * Busca un usuario activo por su ID.
     *
     * @param id Identificador único del usuario.
     * @return {@link UsuarioDTO} correspondiente.
     * @throws RegistroNoEncontradoException Si no existe un usuario activo con ese ID.
     */
    UsuarioDTO buscarUsuarioPorId(Integer id) throws RegistroNoEncontradoException;

    /**
     * Recupera la lista completa de todos los usuarios activos en el sistema.
     *
     * @return Lista de {@link UsuarioDTO}.
     */
    List<UsuarioDTO> listarTodos();

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param dto DTO con los datos actualizados del usuario.
     * @throws RegistroNoEncontradoException Si el usuario a actualizar no existe o está inactivo.
     */
    void actualizarUsuario(UsuarioDTO dto) throws RegistroNoEncontradoException;

    /**
     * Ejecuta el borrado lógico de un usuario estableciendo su campo {@code activo} en {@code false}.
     *
     * @param id Identificador del usuario a desactivar.
     * @throws RegistroNoEncontradoException Si no existe el usuario activo.
     */
    void eliminarUsuario(Integer id) throws RegistroNoEncontradoException;
}
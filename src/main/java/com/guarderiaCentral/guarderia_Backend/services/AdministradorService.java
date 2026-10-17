package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.SysAdminProtegidoException;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de administradores.
 * Define las operaciones de negocio permitidas a los roles ADMINISTRADOR y SYSADMIN.
 * El SYSADMIN opera sobre todas las cuentas; el ADMINISTRADOR no puede ver, modificar ni dar de baja
 * cuentas con rol SYSADMIN. Por eso las consultas y modificaciones reciben el nombre de usuario de quien llama.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
public interface AdministradorService {

    /**
     * Registra un nuevo administrador en el sistema o reactiva uno inactivo existente.
     * Las cuentas SYSADMIN inactivas no se reactivan por esta vía: su nombre de usuario se considera ocupado.
     *
     * @param request Datos de la solicitud de creación.
     * @return {@link AdministradorResponse} con el registro creado o reactivado.
     * @throws NombreUsuarioDuplicadoException Si el nombre de usuario pertenece a otra cuenta activa, a una cuenta
     *                                         SYSADMIN inactiva o a otro tipo de usuario inactivo.
     */
    AdministradorResponse crear(AdministradorRequest request);

    /**
     * Busca un administrador activo por su ID.
     *
     * @param id                    Identificador único del administrador.
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la consulta (viene del token).
     * @return {@link AdministradorResponse} correspondiente al ID.
     * @throws RegistroNoEncontradoException Si no se encuentra un administrador activo con dicho ID.
     * @throws SysAdminProtegidoException    Si quien consulta es ADMINISTRADOR y la cuenta es SYSADMIN.
     */
    AdministradorResponse buscarPorId(Integer id, String nombreUsuarioLlamador);

    /**
     * Lista todos los administradores activos. Si quien consulta es ADMINISTRADOR, excluye las cuentas SYSADMIN.
     *
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la consulta (viene del token).
     * @return Lista de {@link AdministradorResponse} activos visibles para quien consulta.
     */
    List<AdministradorResponse> listarTodos(String nombreUsuarioLlamador);

    /**
     * Lista todos los administradores incluyendo inactivos. Si quien consulta es ADMINISTRADOR, excluye
     * las cuentas SYSADMIN.
     *
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la consulta (viene del token).
     * @return Lista de {@link AdministradorResponse} con los registros visibles para quien consulta.
     */
    List<AdministradorResponse> listarTodosIncluyendoInactivos(String nombreUsuarioLlamador);

    /**
     * Actualiza los datos de un administrador existente.
     *
     * @param id                    Identificador del administrador a actualizar.
     * @param update                Datos a modificar.
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la operación (viene del token).
     * @return {@link AdministradorResponse} actualizado.
     * @throws RegistroNoEncontradoException   Si el administrador no existe o está inactivo.
     * @throws SysAdminProtegidoException      Si quien opera es ADMINISTRADOR y la cuenta es SYSADMIN.
     * @throws NombreUsuarioDuplicadoException Si el nuevo nombre de usuario pertenece a otra cuenta.
     */
    AdministradorResponse actualizar(Integer id, AdministradorUpdate update, String nombreUsuarioLlamador);

    /**
     * Realiza el borrado lógico de un administrador cambiando su estado activo a false.
     * El SYSADMIN no puede dar de baja su propia cuenta, para que el sistema no se quede sin SYSADMIN.
     *
     * @param id                    Identificador del administrador.
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la operación (viene del token).
     * @throws RegistroNoEncontradoException Si no existe un administrador activo con el ID proporcionado.
     * @throws SysAdminProtegidoException    Si quien opera es ADMINISTRADOR y la cuenta es SYSADMIN.
     * @throws BusinessException             Si el SYSADMIN intenta dar de baja su propia cuenta (409).
     */
    void eliminar(Integer id, String nombreUsuarioLlamador);
}
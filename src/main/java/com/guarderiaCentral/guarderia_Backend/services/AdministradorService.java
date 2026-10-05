package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.SysAdminProtegidoException;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de administradores.
 * Define las operaciones de negocio permitidas exclusivamente para el rol SYSADMIN.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
public interface AdministradorService {

    /**
     * Registra un nuevo administrador en el sistema o reactiva uno inactivo existente.
     *
     * @param request Datos de la solicitud de creación.
     * @return {@link AdministradorResponse} con el registro creado o reactivado.
     * @throws NombreUsuarioDuplicadoException Si el nombre de usuario pertenece a otra cuenta activa o inactiva.
     */
    AdministradorResponse crear(AdministradorRequest request);

    /**
     * Busca un administrador activo por su ID.
     *
     * @param id Identificador único del administrador.
     * @return {@link AdministradorResponse} correspondiente al ID.
     * @throws RegistroNoEncontradoException Si no se encuentra un administrador activo con dicho ID.
     */
    AdministradorResponse buscarPorId(Integer id);

    /**
     * Lista todos los administradores activos.
     *
     * @return Lista de {@link AdministradorResponse} activos.
     */
    List<AdministradorResponse> listarTodos();

    /**
     * Lista todos los administradores incluyendo inactivos (uso exclusivo de SYSADMIN).
     *
     * @return Lista de {@link AdministradorResponse} con todos los registros.
     */
    List<AdministradorResponse> listarTodosIncluyendoInactivos();

    /**
     * Actualiza los datos de un administrador existente.
     *
     * @param id     Identificador del administrador a actualizar.
     * @param update Datos a modificar.
     * @return {@link AdministradorResponse} actualizado.
     * @throws RegistroNoEncontradoException    Si el administrador no existe o está inactivo.
     * @throws SysAdminProtegidoException       Si se intenta modificar una cuenta SYSADMIN.
     * @throws NombreUsuarioDuplicadoException Si el nuevo nombre de usuario pertenece a otra cuenta.
     */
    AdministradorResponse actualizar(Integer id, AdministradorUpdate update);

    /**
     * Realiza el borrado lógico de un administrador cambiando su estado activo a false.
     *
     * @param id Identificador del administrador.
     * @throws RegistroNoEncontradoException Si no existe un administrador activo con el ID proporcionado.
     * @throws SysAdminProtegidoException    Si se intenta eliminar una cuenta SYSADMIN.
     */
    void eliminar(Integer id);
}
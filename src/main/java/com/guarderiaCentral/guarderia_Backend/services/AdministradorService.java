package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.AdministradorDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorResponse;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de administradores.
 * Define las operaciones de negocio permitidas exclusivamente para el rol SYSADMIN.
 */
public interface AdministradorService {

    /**
     * Lista todos los administradores activos.
     *
     * @return Lista de AdministradorDTO activos.
     */
    List<AdministradorDTO> listarActivos();

    /**
     * Lista todos los administradores incluyendo inactivos (uso administrativo).
     *
     * @return Lista de AdministradorResponse con todos los registros.
     */
    List<AdministradorResponse> listarTodosAdmin();

    /**
     * Busca un administrador por su ID.
     *
     * @param id Identificador único del administrador.
     * @return AdministradorDTO correspondiente al ID.
     */
    AdministradorDTO buscarPorId(int id);

    /**
     * Registra un nuevo administrador en el sistema.
     *
     * @param request Datos de la solicitud de creación.
     * @return AdministradorResponse con el registro creado.
     */
    AdministradorResponse registrarAdministrador(AdministradorRequest request);

    /**
     * Actualiza los datos de un administrador existente.
     *
     * @param id      Identificador del administrador a actualizar.
     * @param update  Datos a modificar.
     * @return AdministradorResponse actualizado.
     */
    AdministradorResponse actualizarAdministrador(int id, AdministradorUpdate update);

    /**
     * Realiza el borrado lógico de un administrador cambiando su estado activo a false.
     *
     * @param id Identificador del administrador.
     */
    void eliminarAdministrador(int id);
}
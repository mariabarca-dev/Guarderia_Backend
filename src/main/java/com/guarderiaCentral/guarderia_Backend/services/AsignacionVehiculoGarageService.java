package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaOcupadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de asignaciones de vehículos a garajes.
 * Define las operaciones de negocio, validaciones de pertenencia, compatibilidad y capacidad.
 */
public interface AsignacionVehiculoGarageService {

    /**
     * Registra una nueva asignación de un vehículo a un garaje.
     * Aplica validaciones de disponibilidad del garaje, pertenencia al socio propietario,
     * compatibilidad de tipo de vehículo con la zona y capacidad disponible en la zona.
     *
     * @param request DTO de entrada con los IDs del vehículo, garaje y fecha de asignación.
     * @return DTO de respuesta con la asignación registrada.
     * @throws RegistroNoEncontradoException Si el vehículo o el garaje no existen o están inactivos.
     * @throws GarageYaOcupadoException      Si el garaje ya posee una asignación activa.
     * @throws ZonaSinCapacidadException     Si la zona asociada al garaje alcanzó su capacidad máxima.
     * @throws BusinessException             Si el vehículo ya está asignado, no pertenece al dueño del garaje
     *                                       o su tipo no es compatible con la zona.
     */
    AsignacionVehiculoGarageResponse crearAsignacion(AsignacionVehiculoGarageRequest request);

    /**
     * Recupera todas las asignaciones activas de vehículos a garajes.
     *
     * @return Lista de DTOs de respuesta de asignaciones activas.
     */
    List<AsignacionVehiculoGarageResponse> listarTodas();

    /**
     * Recupera todas las asignaciones de vehículos a garajes, incluyendo inactivas (uso administrativo).
     *
     * @return Lista completa de DTOs de respuesta de asignaciones.
     */
    List<AsignacionVehiculoGarageResponse> listarTodasIncluyendoInactivas();

    /**
     * Busca la información de una asignación activa por su identificador único.
     *
     * @param id Identificador de la asignación.
     * @return DTO de respuesta con la asignación encontrada.
     * @throws RegistroNoEncontradoException Si no se encuentra una asignación activa con el ID provisto.
     */
    AsignacionVehiculoGarageResponse buscarPorId(Integer id);

    /**
     * Obtiene la asignación activa vinculada a un garaje específico.
     *
     * @param idGarage Identificador del garaje.
     * @return DTO de respuesta de la asignación.
     * @throws RegistroNoEncontradoException Si el garaje no tiene asignación activa.
     */
    AsignacionVehiculoGarageResponse buscarPorGarage(Integer idGarage);

    /**
     * Obtiene la asignación activa vinculada a un vehículo específico.
     *
     * @param idVehiculo Identificador del vehículo.
     * @return DTO de respuesta de la asignación.
     * @throws RegistroNoEncontradoException Si el vehículo no tiene asignación activa.
     */
    AsignacionVehiculoGarageResponse buscarPorVehiculo(Integer idVehiculo);

    /**
     * Actualiza la información de una asignación existente.
     *
     * @param id     Identificador de la asignación a actualizar.
     * @param update DTO con los datos modificados.
     * @return DTO de respuesta actualizado.
     * @throws RegistroNoEncontradoException Si la asignación no existe o está inactiva.
     * @throws BusinessException             Si los nuevos datos incumplen las reglas de negocio.
     */
    AsignacionVehiculoGarageResponse actualizarAsignacion(Integer id, AsignacionVehiculoGarageUpdate update);

    /**
     * Ejecuta el borrado lógico de una asignación cambiando su estado a inactivo (activo = false).
     *
     * @param id Identificador de la asignación a desactivar.
     * @throws RegistroNoEncontradoException Si no existe una asignación activa con dicho ID.
     */
    void eliminarAsignacion(Integer id);
}
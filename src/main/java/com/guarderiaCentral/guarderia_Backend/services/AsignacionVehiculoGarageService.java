package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaOcupadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.GarageOcupadoResponse;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de asignaciones de vehículos a garajes.
 * Define las operaciones de negocio y las validaciones de disponibilidad, compatibilidad (tipo y
 * dimensiones) y capacidad. Además ofrece la revalidación de las asignaciones vigentes para los casos
 * en que cambian las medidas de un vehículo o de una zona.
 */
public interface AsignacionVehiculoGarageService {

    /**
     * Registra una nueva asignación de un vehículo a un garaje.
     * Aplica validaciones de disponibilidad del garaje, compatibilidad de tipo y dimensiones del vehículo
     * con la zona y capacidad disponible en la zona.
     *
     * @param request DTO de entrada con los IDs del vehículo, garaje y fecha de asignación.
     * @return DTO de respuesta con la asignación registrada.
     * @throws RegistroNoEncontradoException Si el vehículo o el garaje no existen o están inactivos.
     * @throws GarageYaOcupadoException      Si el garaje ya tiene una asignación activa.
     * @throws ZonaSinCapacidadException     Si la zona asociada al garaje ha alcanzado su capacidad máxima.
     * @throws BusinessException             Si el vehículo ya está asignado, su tipo no es compatible con la zona
     *                                       o no entra en las dimensiones de los garages de la zona.
     */
    AsignacionVehiculoGarageResponse crearAsignacion(AsignacionVehiculoGarageRequest request);

    /**
     * Recupera todas las asignaciones activas de vehículos a garajes.
     *
     * @return Lista de DTOs de respuesta de asignaciones activas.
     */
    List<AsignacionVehiculoGarageResponse> listarTodas();

    /**
     * Recupera todas las asignaciones de vehículos a garajes, incluyendo las inactivas (uso administrativo).
     *
     * @return Lista completa de DTOs de respuesta de asignaciones.
     */
    List<AsignacionVehiculoGarageResponse> listarTodasIncluyendoInactivas();

    /**
     * Busca la información de una asignación activa por su identificador único.
     *
     * @param id Identificador de la asignación.
     * @return DTO de respuesta con la asignación encontrada.
     * @throws RegistroNoEncontradoException Si no se encuentra ninguna asignación activa con el ID proporcionado.
     */
    AsignacionVehiculoGarageResponse buscarPorId(Integer id);

    /**
     * Obtiene la asignación activa vinculada a un garaje específico.
     *
     * @param idGarage Identificador del garaje.
     * @return DTO de respuesta de la asignación.
     * @throws RegistroNoEncontradoException Si el garaje no tiene ninguna asignación activa.
     */
    AsignacionVehiculoGarageResponse buscarPorGarage(Integer idGarage);

    /**
     * Obtiene la asignación activa vinculada a un vehículo específico.
     *
     * @param idVehiculo Identificador del vehículo.
     * @return DTO de respuesta de la asignación.
     * @throws RegistroNoEncontradoException Si el vehículo no tiene ninguna asignación activa.
     */
    AsignacionVehiculoGarageResponse buscarPorVehiculo(Integer idVehiculo);

    /**
     * Lista los garages que ocupan los vehículos de un socio, con los datos del garage
     * (lectura de luz, mantenimiento) y la fecha de asignación.
     *
     * @param socioId Identificador del socio propietario de los vehículos.
     * @return Lista de {@link GarageOcupadoResponse}; vacía si ninguno de sus vehículos está asignado.
     */
    List<GarageOcupadoResponse> listarGaragesOcupadosPorSocio(Integer socioId);

    /**
     * Actualiza la información de una asignación existente. Si cambia el vehículo o el garage, aplica las
     * mismas reglas que el alta: garage libre, vehículo sin otra asignación, compatibilidad de tipo y
     * dimensiones, y capacidad de la zona cuando el garage nuevo está en otra zona.
     *
     * @param id     Identificador de la asignación a actualizar.
     * @param update DTO con los datos modificados.
     * @return DTO de respuesta actualizado.
     * @throws RegistroNoEncontradoException Si la asignación, el vehículo o el garage no existen o están inactivos.
     * @throws GarageYaOcupadoException      Si el garage nuevo ya tiene una asignación activa.
     * @throws ZonaSinCapacidadException     Si la zona del garage nuevo alcanzó su capacidad máxima.
     * @throws BusinessException             Si los nuevos datos incumplen las reglas de negocio.
     */
    AsignacionVehiculoGarageResponse actualizarAsignacion(Integer id, AsignacionVehiculoGarageUpdate update);

    /**
     * Ejecuta el borrado lógico de una asignación cambiando su estado a inactivo (activo = false).
     *
     * @param id Identificador de la asignación a desactivar.
     * @throws RegistroNoEncontradoException Si no existe ninguna asignación activa con este ID.
     */
    void eliminarAsignacion(Integer id);

    /**
     * Revalida que un vehículo con asignación activa siga encajando en la zona de su garage (tipo y
     * dimensiones). Se usa al modificar los datos del vehículo. Si no tiene asignación, no hace nada.
     *
     * @param vehiculoId Identificador del vehículo modificado.
     * @throws BusinessException Si el vehículo, con sus nuevos datos, ya no encaja en la zona.
     */
    void validarVehiculoAsignadoSigueEncajando(Integer vehiculoId);

    /**
     * Revalida que todos los vehículos asignados a garages de una zona sigan encajando en ella (tipo y
     * dimensiones). Se usa al modificar las medidas de los garages de la zona.
     *
     * @param zonaId Identificador de la zona modificada.
     * @throws BusinessException Si algún vehículo asignado ya no encaja con las nuevas medidas de la zona.
     */
    void validarVehiculosAsignadosEnZonaSiguenEncajando(Integer zonaId);
}
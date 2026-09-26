package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageUpdate;

import java.util.List;

/**
 * Interfaz de servicio que define la lógica de negocio para la gestión de Garages.
 * Proporciona métodos para registrar, listar, buscar, actualizar, realizar borrado lógico
 * y consultar el reporte de disponibilidad de garajes por zona.
 */
public interface GarageService {

    /**
     * Registra un nuevo garage en el sistema verificando la capacidad disponible en la zona
     * y la unicidad del número de garage.
     *
     * @param request DTO con la información requerida para dar de alta un garage.
     * @return {@link GarageResponse} con la información del garage registrado.
     * @throws RegistroNoEncontradoException Si la zona indicada en la solicitud no existe o está inactiva.
     * @throws ZonaSinCapacidadException     Si la zona ha alcanzado su capacidad máxima permitida de garajes.
     * @throws BusinessException             Si ya existe un garage activo con el número especificado.
     */
    GarageResponse registrarGarage(GarageRequest request);

    /**
     * Obtiene el listado de todos los garages activos en el sistema.
     *
     * @return Lista de {@link GarageResponse} con los garages activos.
     */
    List<GarageResponse> listarTodos();

    /**
     * Obtiene el listado completo de garages, incluyendo aquellos con borrado lógico.
     * Uso exclusivo para tareas administrativas.
     *
     * @return Lista de {@link GarageResponse} incluyendo registros inactivos.
     */
    List<GarageResponse> listarTodosIncluyendoInactivos();

    /**
     * Busca un garage activo por su identificador único.
     *
     * @param id Identificador único del garage.
     * @return {@link GarageResponse} con los datos del garage encontrado.
     * @throws RegistroNoEncontradoException Si no existe un garage activo con el ID proporcionado.
     */
    GarageResponse buscarPorId(Integer id);

    /**
     * Actualiza la información de un garage existente.
     *
     * @param id      Identificador del garage a actualizar.
     * @param update  DTO con los campos a modificar.
     * @return {@link GarageResponse} con los datos actualizados del garage.
     * @throws RegistroNoEncontradoException Si el garage o la zona especificada no existen o están inactivos.
     * @throws BusinessException             Si el nuevo número de garage ya pertenece a otro registro activo.
     */
    GarageResponse actualizarGarage(Integer id, GarageUpdate update);

    /**
     * Realiza el borrado lógico de un garage (cambiando su estado activo a false)
     * y propaga en cascada la inactivación a las asignaciones de vehículos vigentes.
     *
     * @param id Identificador único del garage a eliminar.
     * @throws RegistroNoEncontradoException Si no se encuentra un garage activo con el ID indicado.
     */
    void eliminarGarage(Integer id);

    /**
     * Genera un reporte detallado de disponibilidad de garages organizados por zona,
     * calculando la ocupación actual en base a los vehículos asignados.
     *
     * @return Lista de cadenas de texto con el informe de disponibilidad por zona.
     */
    List<String> consultarDisponibilidadGarages();
}
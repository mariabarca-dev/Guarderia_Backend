package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de asignaciones de empleados a zonas de la guardería.
 * Define las operaciones de negocio, validaciones de capacidad y consultas asociadas.
 */
public interface AsignacionEmpleadoZonaService {

    /**
     * Crea una nueva asignación de un empleado a una zona validando capacidad disponible y restricciones de negocio.
     *
     * @param request DTO con los datos de entrada para crear la asignación.
     * @return DTO de respuesta con la asignación creada.
     * @throws RegistroNoEncontradoException Si el empleado o la zona especificados no existen o están inactivosis.
     * @throws ZonaSinCapacidadException     Si la zona no cuenta con capacidad para los vehículos asignados.
     * @throws BusinessException             Si el empleado ya está asignado a dicha zona o los datos son inválidos.
     */
    AsignacionEmpleadoZonaResponse crearAsignacion(AsignacionEmpleadoZonaRequest request);

    /**
     * Crea una asignación rápida especificando únicamente los IDs del empleado y de la zona junto a la cantidad de vehículos.
     *
     * @param idEmpleado    Identificador del empleado.
     * @param idZona        Identificador de la zona.
     * @param cantVehiculos Cantidad de vehículos a cargo del empleado en dicha zona.
     * @return DTO de respuesta con la asignación creada.
     * @throws RegistroNoEncontradoException Si el empleado o la zona no existen.
     * @throws ZonaSinCapacidadException     Si supera la capacidad máxima de la zona.
     * @throws BusinessException             Si la asignación incumple reglas de negocio.
     */
    AsignacionEmpleadoZonaResponse crearAsignacionPorIds(Integer idEmpleado, Integer idZona, Integer cantVehiculos);

    /**
     * Obtiene el listado completo de asignaciones activas en el sistema.
     *
     * @return Lista de asignaciones activas.
     */
    List<AsignacionEmpleadoZonaResponse> listarTodas();

    /**
     * Obtiene el listado completo de asignaciones, incluyendo las inactivas (uso administrativo).
     *
     * @return Lista de todas las asignaciones existentes.
     */
    List<AsignacionEmpleadoZonaResponse> listarTodasIncluyendoInactivas();

    /**
     * Busca el detalle de una asignación específica por su ID.
     *
     * @param id Identificador de la asignación.
     * @return DTO de respuesta de la asignación encontrada.
     * @throws RegistroNoEncontradoException Si no se encuentra la asignación con dicho ID.
     */
    AsignacionEmpleadoZonaResponse buscarPorId(Integer id);

    /**
     * Busca las asignaciones activas asociadas al código de un empleado.
     *
     * @param codigo Código de legajo del empleado.
     * @return Lista de asignaciones pertenecientes al empleado.
     */
    List<AsignacionEmpleadoZonaResponse> buscarPorCodigoEmpleado(String codigo);

    /**
     * Actualiza la información de una asignación existente (ej. cambio en la cantidad de vehículos a cargo).
     *
     * @param id      Identificador de la asignación a actualizar.
     * @param update  DTO con los nuevos datos.
     * @return DTO de respuesta actualizado.
     * @throws RegistroNoEncontradoException Si no existe la asignación.
     * @throws BusinessException             Si la actualización infringe restricciones de capacidad u otras reglas.
     */
    AsignacionEmpleadoZonaResponse actualizarAsignacion(Integer id, AsignacionEmpleadoZonaUpdate update);

    /**
     * Realiza el borrado lógico de una asignación cambiando su estado a inactivo (activo = false).
     *
     * @param id Identificador de la asignación a desactivar.
     * @throws RegistroNoEncontradoException Si no existe la asignación especificada.
     */
    void eliminarAsignacion(Integer id);
}
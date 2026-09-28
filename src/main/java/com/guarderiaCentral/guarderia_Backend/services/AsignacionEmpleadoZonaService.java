package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de Asignación de Empleados a Zonas.
 */
public interface AsignacionEmpleadoZonaService {

    /**
     * Crea una nueva asignación de empleado a zona validando capacidad y reglas de negocio.
     *
     * @param request Datos de la solicitud de asignación.
     * @return AsignacionEmpleadoZonaResponse con los datos de la asignación creada.
     */
    AsignacionEmpleadoZonaResponse crearAsignacion(AsignacionEmpleadoZonaRequest request);

    /**
     * Lista todas las asignaciones activas.
     *
     * @return Lista de respuestas de asignaciones activas.
     */
    List<AsignacionEmpleadoZonaResponse> listarTodas();

    /**
     * Lista todas las asignaciones incluyendo inactivas (uso administrativo).
     *
     * @return Lista de respuestas de todas las asignaciones.
     */
    List<AsignacionEmpleadoZonaResponse> listarTodasIncluyendoInactivas();

    /**
     * Busca una asignación por su identificador único.
     *
     * @param id Identificador de la asignación.
     * @return AsignacionEmpleadoZonaResponse encontrada.
     */
    AsignacionEmpleadoZonaResponse buscarPorId(Integer id);

    /**
     * Actualiza una asignación existente.
     *
     * @param id Identificador de la asignación a actualizar.
     * @param update Datos de actualización.
     * @return AsignacionEmpleadoZonaResponse actualizada.
     */
    AsignacionEmpleadoZonaResponse actualizarAsignacion(Integer id, AsignacionEmpleadoZonaUpdate update);

    /**
     * Realiza el borrado lógico de una asignación cambiando su estado activo a false.
     *
     * @param id Identificador de la asignación.
     */
    void eliminarAsignacion(Integer id);
}
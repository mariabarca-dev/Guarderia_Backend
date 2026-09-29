package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;

import java.util.List;

/**
 * Interfaz de servicio que define la lógica de negocio para la gestión de Empleados.
 * Proporciona métodos para registrar, actualizar, consultar y realizar el borrado
 * lógico de empleados, así como operaciones de consulta de zonas y vehículos asociados.
 */
public interface EmpleadoService {

    /**
     * Registra un nuevo empleado en el sistema.
     *
     * @param request Datos de creación del empleado.
     * @return {@link EmpleadoResponse} con la información del empleado registrado.
     * @throws CodigoEmpleadoDuplicadoException si ya existe un empleado con el mismo código.
     * @throws DniDuplicadoException            si ya existe un usuario con el mismo DNI.
     */
    EmpleadoResponse registrarEmpleado(EmpleadoRequest request);

    /**
     * Obtiene el listado de todos los empleados activos en el sistema.
     *
     * @return Lista de {@link EmpleadoResponse}.
     */
    List<EmpleadoResponse> listarTodos();

    /**
     * Obtiene el listado de todos los empleados, incluyendo aquellos con borrado lógico (inactivos).
     * Uso exclusivo administrativo/SYSADMIN.
     *
     * @return Lista de {@link EmpleadoResponse} incluidos inactivos.
     */
    List<EmpleadoResponse> listarTodosIncluyendoInactivos();

    /**
     * Busca un empleado activo por su ID.
     *
     * @param id Identificador único del empleado.
     * @return {@link EmpleadoResponse} del empleado encontrado.
     * @throws RegistroNoEncontradoException si no existe el empleado activo con el ID proporcionado.
     */
    EmpleadoResponse buscarEmpleadoPorId(Integer id);

    /**
     * Actualiza la información de un empleado existente.
     *
     * @param id     Identificador del empleado a actualizar.
     * @param update Datos actualizados del empleado.
     * @return {@link EmpleadoResponse} con los datos actualizados.
     * @throws RegistroNoEncontradoException    si no se encuentra el empleado.
     * @throws CodigoEmpleadoDuplicadoException si el nuevo código pertenece a otro empleado.
     * @throws DniDuplicadoException            si el nuevo DNI pertenece a otro usuario.
     */
    EmpleadoResponse actualizarEmpleado(Integer id, EmpleadoUpdate update);

    /**
     * Realiza el borrado lógico de un empleado (activo = false).
     * Propaga la baja lógica en cascada a sus asignaciones de zona activas.
     *
     * @param id Identificador único del empleado a desactivar.
     * @throws RegistroNoEncontradoException si no existe el empleado con el ID especificado.
     */
    void eliminarEmpleado(Integer id);

    /**
     * Obtiene el listado de asignaciones de zona activas asociadas a un empleado.
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de {@link AsignacionEmpleadoZonaResponse}.
     * @throws RegistroNoEncontradoException si el empleado no existe o está inactivo.
     */
    List<AsignacionEmpleadoZonaResponse> listarZonasAsignadas(int empleadoId);

    /**
     * Obtiene los vehículos bajo responsabilidad indirecta de un empleado a través de sus zonas asignadas.
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de {@link VehiculoResponse} a cargo del empleado.
     * @throws RegistroNoEncontradoException si el empleado no existe o está inactivo.
     */
    List<VehiculoResponse> listarVehiculosBajoResponsabilidad(int empleadoId);
}
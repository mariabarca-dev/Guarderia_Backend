package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
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
 * lógico de empleados, así como operaciones de consulta de zonas y vehículos a cargo.
 * El DNI del empleado es opcional; cuando se informa, no puede pertenecer a otra cuenta del sistema.
 *
 * @author Cátedra Guardería Central
 */
public interface EmpleadoService {

    /**
     * Registra un nuevo empleado en el sistema o reactiva uno inactivo existente.
     *
     * @param request Datos de creación del empleado.
     * @return {@link EmpleadoResponse} con la información del empleado registrado o reactivado.
     * @throws CodigoEmpleadoDuplicadoException si ya existe un empleado activo con el mismo código.
     * @throws NombreUsuarioDuplicadoException  si ya existe un usuario registrado con el mismo nombre de usuario.
     * @throws DniDuplicadoException            si el DNI informado ya pertenece a otra cuenta del sistema.
     */
    EmpleadoResponse crear(EmpleadoRequest request);

    /**
     * Busca un empleado activo por su ID.
     *
     * @param id Identificador único del empleado.
     * @return {@link EmpleadoResponse} del empleado encontrado.
     * @throws RegistroNoEncontradoException si no existe el empleado activo con el ID proporcionado.
     */
    EmpleadoResponse buscarPorId(Integer id);

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
     * Actualiza la información de un empleado existente.
     *
     * @param id     Identificador del empleado a actualizar.
     * @param update Datos actualizados del empleado.
     * @return {@link EmpleadoResponse} con los datos actualizados.
     * @throws RegistroNoEncontradoException    si no se encuentra el empleado.
     * @throws CodigoEmpleadoDuplicadoException si el nuevo código pertenece a otro empleado activo.
     * @throws NombreUsuarioDuplicadoException  si el nuevo nombre de usuario pertenece a otra cuenta.
     * @throws DniDuplicadoException            si el nuevo DNI ya pertenece a otra cuenta del sistema.
     */
    EmpleadoResponse actualizar(Integer id, EmpleadoUpdate update);

    /**
     * Realiza el borrado lógico de un empleado (activo = false).
     * Propaga la baja lógica en cascada a sus asignaciones de zona activas y libera los vehículos que tenía a cargo.
     *
     * @param id Identificador único del empleado a desactivar.
     * @throws RegistroNoEncontradoException si no existe el empleado con el ID especificado.
     */
    void eliminar(Integer id);

    /**
     * Obtiene el listado de asignaciones de zona activas asociadas a un empleado.
     * Cada asignación incluye el número de vehículos que el empleado tiene a cargo en la zona.
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de {@link AsignacionEmpleadoZonaResponse}.
     * @throws RegistroNoEncontradoException si el empleado no existe o está inactivo.
     */
    List<AsignacionEmpleadoZonaResponse> listarZonasAsignadas(Integer empleadoId);

    /**
     * Obtiene los vehículos que el empleado tiene efectivamente a cargo
     * (solo los que le fueron asignados, no todos los de sus zonas).
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de {@link VehiculoResponse} a cargo del empleado.
     * @throws RegistroNoEncontradoException si el empleado no existe o está inactivo.
     */
    List<VehiculoResponse> listarVehiculosBajoResponsabilidad(Integer empleadoId);

    /**
     * Asigna un vehículo a un empleado. El vehículo debe estar en un garage de una zona a la que el empleado
     * esté asignado, y el empleado no puede superar la cantidad de vehículos a cargo definida para esa zona.
     * Si el vehículo estaba a cargo de otro empleado, pasa al nuevo.
     *
     * @param empleadoId Identificador del empleado.
     * @param vehiculoId Identificador del vehículo.
     * @return {@link VehiculoResponse} del vehículo asignado.
     * @throws RegistroNoEncontradoException si el empleado o el vehículo no existen o están inactivos.
     * @throws BusinessException             si el vehículo no tiene garage, el empleado no está asignado a su zona
     *                                       o ya tiene a cargo todos los vehículos permitidos en esa zona.
     */
    VehiculoResponse asignarVehiculo(Integer empleadoId, Integer vehiculoId);

    /**
     * Quita un vehículo del cargo de un empleado.
     *
     * @param empleadoId Identificador del empleado.
     * @param vehiculoId Identificador del vehículo.
     * @throws RegistroNoEncontradoException si el empleado o el vehículo no existen, o el vehículo no está a cargo de ese empleado.
     */
    void desasignarVehiculo(Integer empleadoId, Integer vehiculoId);

    /**
     * Cuenta los vehículos que un empleado tiene a cargo dentro de una zona.
     *
     * @param empleadoId Identificador del empleado.
     * @param zonaId     Identificador de la zona.
     * @return Cantidad de vehículos asignados al empleado en esa zona.
     */
    int contarVehiculosAsignadosEnZona(Integer empleadoId, Integer zonaId);

    /**
     * Libera (deja sin empleado a cargo) los vehículos que un empleado tiene en una zona.
     * Se usa cuando la asignación empleado-zona se elimina o cambia.
     *
     * @param empleadoId Identificador del empleado.
     * @param zonaId     Identificador de la zona.
     */
    void liberarVehiculosEnZona(Integer empleadoId, Integer zonaId);

    // --- Consultas del empleado autenticado ("solo lo suyo"): se identifica por el nombre de usuario del token ---

    /**
     * Devuelve el perfil del empleado autenticado.
     *
     * @param nombreUsuario Nombre de usuario del empleado autenticado (viene del token).
     * @return {@link EmpleadoResponse} con sus datos.
     * @throws RegistroNoEncontradoException si no existe un empleado activo con ese nombre de usuario.
     */
    EmpleadoResponse buscarMiPerfil(String nombreUsuario);

    /**
     * Lista las zonas asignadas al empleado autenticado, con la cantidad de vehículos a cargo en cada una.
     *
     * @param nombreUsuario Nombre de usuario del empleado autenticado.
     * @return Lista de {@link AsignacionEmpleadoZonaResponse}.
     * @throws RegistroNoEncontradoException si no existe un empleado activo con ese nombre de usuario.
     */
    List<AsignacionEmpleadoZonaResponse> listarMisZonas(String nombreUsuario);

    /**
     * Lista los vehículos que tiene a cargo el empleado autenticado.
     *
     * @param nombreUsuario Nombre de usuario del empleado autenticado.
     * @return Lista de {@link VehiculoResponse}.
     * @throws RegistroNoEncontradoException si no existe un empleado activo con ese nombre de usuario.
     */
    List<VehiculoResponse> listarMisVehiculos(String nombreUsuario);
}
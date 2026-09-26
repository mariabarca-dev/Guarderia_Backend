package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoUpdate;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoDTO;

import java.util.List;

/**
 * Interfaz de servicio que define el contrato de operaciones de negocio para la entidad Vehiculo.
 */
public interface VehiculoService {

    /**
     * Registra un nuevo vehículo en el sistema verificando que no exista la matrícula.
     *
     * @param request Datos de la solicitud de alta del vehículo.
     * @return DTO de respuesta con la información del vehículo registrado.
     * @throws MatriculaDuplicadaException Si la matrícula ya se encuentra registrada.
     * @throws RegistroNoEncontradoException Si el socio asignado al vehículo no existe o está inactivo.
     */
    VehiculoResponse registrarVehiculo(VehiculoRequest request);

    /**
     * Busca un vehículo activo por su identificador único.
     *
     * @param id Identificador único del vehículo.
     * @return DTO de respuesta con la información del vehículo.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con el ID proporcionado.
     */
    VehiculoResponse buscarPorId(int id);

    /**
     * Busca un vehículo activo por su matrícula.
     *
     * @param matricula Matrícula o dominio del vehículo.
     * @return DTO de respuesta con la información del vehículo.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con dicha matrícula.
     */
    VehiculoResponse buscarPorMatricula(String matricula);

    /**
     * Recupera la lista de todos los vehículos activos en el sistema.
     *
     * @return Lista de DTOs de respuesta de vehículos activos.
     */
    List<VehiculoResponse> listarTodos();

    /**
     * Lista todos los vehículos pertenecientes a un socio determinado.
     *
     * @param socioId Identificador del socio propietario.
     * @return Lista de DTOs simplificados (VehiculoDTO) pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio especificado no existe o se encuentra inactivo.
     */
    List<VehiculoDTO> listarPorSocio(int socioId);

    /**
     * Método alternativo de consulta de vehículos pertenecientes a un socio.
     *
     * @param socioId Identificador del socio propietario.
     * @return Lista de DTOs simplificados (VehiculoDTO).
     * @throws RegistroNoEncontradoException Si el socio especificado no existe o se encuentra inactivo.
     */
    List<VehiculoDTO> buscarVehiculosPorSocio(int socioId);

    /**
     * Filtra los vehículos activos por su tipo (por ejemplo: LANCHA, MOTO_AQUATICA, etc.).
     *
     * @param tipo Tipo de vehículo a filtrar.
     * @return Lista de DTOs de respuesta con los vehículos coincidentes.
     */
    List<VehiculoResponse> buscarPorTipo(TipoVehiculo tipo);

    /**
     * Lista los vehículos asociados a las zonas que están a cargo de un empleado responsable.
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de DTOs simplificados (VehiculoDTO) bajo la responsabilidad del empleado.
     * @throws RegistroNoEncontradoException Si el empleado no existe o está inactivo.
     */
    List<VehiculoDTO> listarVehiculosPorResponsable(int empleadoId);

    /**
     * Actualiza la información de un vehículo existente.
     *
     * @param id Identificador del vehículo a modificar.
     * @param update DTO con los datos actualizados.
     * @return DTO de respuesta con el vehículo actualizado.
     * @throws RegistroNoEncontradoException Si el vehículo o las entidades asociadas no existen.
     * @throws MatriculaDuplicadaException Si la nueva matrícula pertenece a otro vehículo.
     */
    VehiculoResponse actualizarVehiculo(int id, VehiculoUpdate update);

    /**
     * Realiza el borrado lógico de un vehículo a partir de su matrícula.
     *
     * @param matricula Matrícula del vehículo a dar de baja.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con dicha matrícula.
     */
    void eliminarVehiculo(String matricula);

    /**
     * Realiza el borrado lógico de un vehículo a partir de su ID.
     *
     * @param id Identificador del vehículo a dar de baja.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con dicho ID.
     */
    void eliminarVehiculoPorId(int id);
}
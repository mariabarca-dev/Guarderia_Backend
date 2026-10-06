package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoUpdate;

import java.util.List;

/**
 * Interfaz de servicio que define el contrato de operaciones de negocio para la entidad Vehiculo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
public interface VehiculoService {

    /**
     * Registra un nuevo vehículo. Si existe un vehículo inactivo con la misma matrícula, lo reactiva
     * con los datos del request; si existe uno activo, rechaza el alta.
     *
     * @param request Datos de la solicitud de alta del vehículo.
     * @return {@link VehiculoResponse} con el vehículo creado o reactivado.
     * @throws MatriculaDuplicadaException   Si la matrícula pertenece a un vehículo activo.
     * @throws RegistroNoEncontradoException Si el socio indicado no existe o está inactivo.
     */
    VehiculoResponse crear(VehiculoRequest request);

    /**
     * Busca un vehículo activo por su identificador único.
     *
     * @param id Identificador único del vehículo.
     * @return {@link VehiculoResponse} con la información del vehículo.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con ese ID.
     */
    VehiculoResponse buscarPorId(Integer id);

    /**
     * Busca un vehículo activo por su matrícula.
     *
     * @param matricula Matrícula del vehículo.
     * @return {@link VehiculoResponse} con la información del vehículo.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con esa matrícula.
     */
    VehiculoResponse buscarPorMatricula(String matricula);

    /**
     * Lista todos los vehículos activos.
     *
     * @return Lista de {@link VehiculoResponse} activos.
     */
    List<VehiculoResponse> listarTodos();

    /**
     * Lista todos los vehículos, incluyendo los dados de baja lógica.
     * Uso exclusivo del rol ADMINISTRADOR.
     *
     * @return Lista de {@link VehiculoResponse} activos e inactivos.
     */
    List<VehiculoResponse> listarTodosIncluyendoInactivos();

    /**
     * Lista los vehículos activos de un socio.
     *
     * @param socioId Identificador del socio propietario.
     * @return Lista de {@link VehiculoResponse} del socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    List<VehiculoResponse> listarPorSocio(Integer socioId);

    /**
     * Lista los vehículos activos de un tipo determinado.
     *
     * @param tipo Tipo de vehículo a filtrar.
     * @return Lista de {@link VehiculoResponse} que coinciden con el tipo.
     */
    List<VehiculoResponse> buscarPorTipo(TipoVehiculo tipo);

    /**
     * Actualiza los datos de un vehículo activo. Solo se modifican los campos no nulos del update.
     *
     * @param id     Identificador del vehículo a modificar.
     * @param update Datos a actualizar.
     * @return {@link VehiculoResponse} con el vehículo actualizado.
     * @throws RegistroNoEncontradoException Si el vehículo o el nuevo socio no existen o están inactivos.
     * @throws MatriculaDuplicadaException   Si la nueva matrícula pertenece a otro vehículo, activo o inactivo.
     */
    VehiculoResponse actualizar(Integer id, VehiculoUpdate update);

    /**
     * Realiza la baja lógica de un vehículo y, en cascada, la de su asignación de garage activa,
     * liberando el garage.
     *
     * @param id Identificador del vehículo a dar de baja.
     * @throws RegistroNoEncontradoException Si no existe un vehículo activo con ese ID.
     */
    void eliminar(Integer id);
}
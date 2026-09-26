package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.dtos.GarageDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoDTO;

import java.util.List;

/**
 * Interfaz de servicio que define los contratos de negocio para la gestión de Socios.
 */
public interface SocioService {

    /**
     * Registra un nuevo socio en el sistema.
     *
     * @param request DTO con los datos requeridos para el alta del socio.
     * @return DTO de respuesta con la información del socio registrado.
     * @throws DniDuplicadoException Si ya existe un usuario/socio registrado con el mismo DNI.
     */
    SocioResponse registrarSocio(SocioRequest request);

    /**
     * Busca un socio activo por su ID.
     *
     * @param id Identificador único del socio.
     * @return DTO de respuesta con la información del socio.
     * @throws RegistroNoEncontradoException Si no se encuentra un socio activo con el ID proporcionado.
     */
    SocioResponse buscarPorId(int id);

    /**
     * Busca un socio activo por su número de DNI.
     *
     * @param dni Número de documento de identidad.
     * @return DTO de respuesta con los datos del socio.
     * @throws RegistroNoEncontradoException Si no existe un socio activo con dicho DNI.
     */
    SocioResponse buscarPorDni(String dni);

    /**
     * Recupera la lista de todos los socios activos en el sistema.
     *
     * @return Lista de DTOs de respuesta de socios activos.
     */
    List<SocioResponse> listarTodos();

    /**
     * Actualiza la información de un socio existente.
     *
     * @param id Identificador del socio a actualizar.
     * @param update DTO con los nuevos datos del socio.
     * @return DTO de respuesta con la información actualizada.
     * @throws RegistroNoEncontradoException Si el socio no existe o no se encuentra activo.
     * @throws DniDuplicadoException Si el nuevo DNI ya pertenece a otro usuario.
     */
    SocioResponse actualizarSocio(int id, SocioUpdate update);

    /**
     * Realiza el borrado lógico de un socio desactivando su registro.
     * Define explícitamente la propagación o bloqueo ante entidades asociadas.
     *
     * @param id Identificador único del socio a desactivar.
     * @throws RegistroNoEncontradoException Si no se encuentra el socio activo.
     */
    void eliminarSocio(int id);

    /**
     * Obtiene los vehículos asociados a un socio específico.
     *
     * @param socioId Identificador único del socio.
     * @return Lista de DTOs de vehículos pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o no está activo.
     */
    List<VehiculoDTO> listarVehiculosPorSocio(int socioId);

    /**
     * Obtiene los garages asociados en propiedad a un socio específico.
     *
     * @param socioId Identificador único del socio.
     * @return Lista de DTOs de garages pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o no está activo.
     */
    List<GarageDTO> listarGarajesPorSocio(int socioId);

    /**
     * Obtiene una descripción del estado del garage asignado o en propiedad del socio.
     *
     * @param socioId Identificador único del socio.
     * @return Cadena de texto con el resumen o estado del garage.
     * @throws RegistroNoEncontradoException Si el socio no existe o no está activo.
     */
    String obtenerEstadoGarageSocio(int socioId);
}
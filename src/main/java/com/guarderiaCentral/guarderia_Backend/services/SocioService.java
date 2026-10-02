package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;

import java.util.List;

/**
 * Interfaz de servicio que define los contratos de negocio para la gestión de Socios.
 *
 * @author Guardería Central
 */
public interface SocioService {

    /**
     * Registra un nuevo socio en el sistema o reactiva uno inactivo existente.
     *
     * @param request DTO con los datos requeridos para el alta del socio.
     * @return DTO de respuesta con la información del socio registrado o reactivado.
     * @throws DniDuplicadoException            Si ya existe un socio activo con el mismo DNI.
     * @throws NombreUsuarioDuplicadoException Si el nombre de usuario ya pertenece a otra cuenta.
     */
    SocioResponse crear(SocioRequest request);

    /**
     * Busca un socio activo por su ID.
     *
     * @param id Identificador único del socio.
     * @return DTO de respuesta con la información del socio.
     * @throws RegistroNoEncontradoException Si no se encuentra un socio activo con el ID proporcionado.
     */
    SocioResponse buscarPorId(Integer id);

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
     * Recupera la lista completa de socios, incluyendo aquellos con borrado lógico (inactivos).
     *
     * @return Lista de DTOs de respuesta de todos los socios.
     */
    List<SocioResponse> listarTodosIncluyendoInactivos();

    /**
     * Actualiza la información de un socio existente.
     *
     * @param id     Identificador del socio a actualizar.
     * @param update DTO con los nuevos datos del socio.
     * @return DTO de respuesta con la información actualizada.
     * @throws RegistroNoEncontradoException    Si el socio no existe o no se encuentra activo.
     * @throws DniDuplicadoException            Si el nuevo DNI ya pertenece a otro socio activo.
     * @throws NombreUsuarioDuplicadoException Si el nuevo nombre de usuario ya está en uso.
     */
    SocioResponse actualizar(Integer id, SocioUpdate update);

    /**
     * Realiza el borrado lógico de un socio desactivando su registro.
     * Aplica bloqueo si el socio posee vehículos activos o propiedades de garage vigentes.
     *
     * @param id Identificador único del socio a desactivar.
     * @throws RegistroNoEncontradoException Si no se encuentra el socio activo.
     * @throws DependenciasActivasException Si el socio tiene vehículos activos o garajes a su nombre.
     */
    void eliminar(Integer id);

    /**
     * Obtiene los vehículos asociados a un socio específico.
     *
     * @param socioId Identificador único del socio.
     * @return Lista de DTOs de vehículos pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o no está activo.
     */
    List<VehiculoResponse> listarVehiculosPorSocio(Integer socioId);

    /**
     * Obtiene los garajes asociados en propiedad a un socio específico.
     *
     * @param socioId Identificador único del socio.
     * @return Lista de DTOs de propiedades de garaje pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o no está activo.
     */
    List<PropiedadGarageResponse> listarGarajesPorSocio(Integer socioId);

    /**
     * Obtiene la propiedad de garaje activa asignada a un socio específico.
     *
     * @param socioId Identificador único del socio.
     * @return DTO {@link PropiedadGarageResponse} con la información del garaje del socio.
     * @throws RegistroNoEncontradoException Si el socio no existe, está inactivo o no posee garaje.
     */
    PropiedadGarageResponse obtenerEstadoGarageSocio(Integer socioId);
}
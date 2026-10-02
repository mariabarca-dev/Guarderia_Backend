package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaVendidoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageUpdate;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de la entidad asociativa PropiedadGarage.
 * Define las operaciones de negocio para la compra y administración de propiedades de garages por parte de los socios.
 *
 * @author Cátedra
 * @version 1.0
 */
public interface PropiedadGarageService {

    /**
     * Registra la compra de un garage por parte de un socio en la fecha especificada.
     *
     * @param request DTO con la información requerida para registrar la propiedad (idSocio, idGarage, fechaCompra).
     * @return {@link PropiedadGarageResponse} con la propiedad registrada.
     * @throws RegistroNoEncontradoException Si el socio o el garage no existen o están inactivos.
     * @throws GarageYaVendidoException Si el garage ya cuenta con un propietario asignado.
     * @throws BusinessException Si la fecha de compra es anterior a la fecha de ingreso del socio.
     */
    PropiedadGarageResponse registrarPropiedad(PropiedadGarageRequest request);

    /**
     * Obtiene los datos detallados de una propiedad de garage por su identificador único.
     *
     * @param id Identificador único de la propiedad.
     * @return {@link PropiedadGarageResponse} con los datos de la propiedad hallada.
     * @throws RegistroNoEncontradoException Si la propiedad no existe o está dada de baja lógicamente.
     */
    PropiedadGarageResponse obtenerPorId(Integer id);

    /**
     * Lista todas las propiedades de garage activas en el sistema.
     *
     * @return Lista de {@link PropiedadGarageResponse}.
     */
    List<PropiedadGarageResponse> listarTodas();

    /**
     * Lista todas las propiedades de garage, incluyendo aquellas con borrado lógico (inactivas).
     * Uso exclusivo administrativo.
     *
     * @return Lista completa de {@link PropiedadGarageResponse}.
     */
    List<PropiedadGarageResponse> listarTodasIncluyendoInactivas();

    /**
     * Lista todas las propiedades pertenecientes a un socio específico.
     *
     * @param socioId Identificador del socio.
     * @return Lista de {@link PropiedadGarageResponse} vinculadas al socio.
     * @throws RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
     */
    List<PropiedadGarageResponse> listarPorSocio(Integer socioId);

    /**
     * Actualiza los datos de un registro de propiedad existente.
     *
     * @param id Identificador de la propiedad a modificar.
     * @param update DTO con los datos a actualizar.
     * @return {@link PropiedadGarageResponse} con la entidad actualizada.
     * @throws RegistroNoEncontradoException Si la propiedad, el socio o el garage no existen.
     * @throws BusinessException Si la nueva fecha de compra incumple reglas de negocio.
     */
    PropiedadGarageResponse actualizar(Integer id, PropiedadGarageUpdate update);

    /**
     * Realiza el borrado lógico de un registro de propiedad de garage.
     *
     * @param id Identificador único de la propiedad a dar de baja.
     * @throws RegistroNoEncontradoException Si el registro no existe o ya está inactivo.
     */
    void eliminar(Integer id);
}
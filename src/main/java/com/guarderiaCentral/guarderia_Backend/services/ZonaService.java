package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.LetraZonaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaUpdate;

import java.util.List;

/**
 * Interfaz que define las operaciones del servicio de negocio para la gestión de Zonas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
public interface ZonaService {

    /**
     * Registra una nueva zona en el sistema previa validación de reglas de negocio.
     *
     * @param request Datos de la zona a registrar.
     * @return {@link ZonaResponse} Datos de la zona registrada.
     */
    ZonaResponse registrarZona(ZonaRequest request);

    /**
     * Obtiene una zona activa por su identificador único.
     *
     * @param id Identificador único de la zona.
     * @return {@link ZonaResponse} Datos de la zona encontrada.
     */
    ZonaResponse buscarPorId(Integer id);

    /**
     * Obtiene una zona activa por su letra identificadora.
     *
     * @param letra Letra identificadora de la zona.
     * @return {@link ZonaResponse} Datos de la zona encontrada.
     */
    ZonaResponse buscarPorLetra(String letra);

    /**
     * Obtiene el listado de todas las zonas activas en el sistema.
     *
     * @return Lista de {@link ZonaResponse}.
     */
    List<ZonaResponse> listarTodas();

    /**
     * Obtiene el listado de todas las zonas del sistema, incluyendo aquellas con borrado lógico.
     *
     * @return Lista de {@link ZonaResponse}.
     */
    List<ZonaResponse> listarTodasIncluyendoInactivas();

    /**
     * Actualiza la información de una zona existente. No permite cambiar el tipo de vehículo si la zona tiene
     * garages, ni bajar la capacidad por debajo de los garages que ya tiene, ni achicar los garages de modo
     * que un vehículo ya asignado deje de entrar.
     *
     * @param id Identificador único de la zona a actualizar.
     * @param update Datos actualizados para la zona.
     * @return {@link ZonaResponse} Datos de la zona actualizada.
     * @throws RegistroNoEncontradoException Si la zona no existe o está inactiva.
     * @throws LetraZonaDuplicadaException Si la nueva letra ya está asignada a otra zona activa.
     * @throws BusinessException Si el cambio incumple alguna de las reglas anteriores.
     */
    ZonaResponse actualizarZona(Integer id, ZonaUpdate update);

    /**
     * Ejecuta el borrado lógico de una zona si no posee garajes o relaciones activas asociadas.
     *
     * @param id Identificador único de la zona a eliminar.
     */
    void eliminarZona(Integer id);
}
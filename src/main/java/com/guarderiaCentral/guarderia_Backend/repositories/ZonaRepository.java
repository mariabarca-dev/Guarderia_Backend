package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Zona.
 * Filtra por defecto los registros activos e incluye los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface ZonaRepository extends JpaRepository<Zona, Integer> {

    /**
     * Busca todas las zonas cuyo estado activo sea true.
     *
     * @return Lista de zonas activas.
     */
    @Query("SELECT z FROM Zona z WHERE z.activo = true")
    List<Zona> findAllActive();

    /**
     * Busca una zona por su ID asegurando que se encuentre activa.
     *
     * @param id ID de la zona.
     * @return Optional con la zona encontrada si está activa.
     */
    @Query("SELECT z FROM Zona z WHERE z.id = :id AND z.activo = true")
    Optional<Zona> findActiveById(Integer id);

    /**
     * Busca una zona por su letra identificatoria asegurando que esté activa.
     *
     * @param letra Letra de la zona.
     * @return Optional con la zona encontrada.
     */
    Optional<Zona> findByLetraAndActivoTrue(String letra);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de zonas (activas e inactivas).
     */
    @Query("SELECT z FROM Zona z")
    List<Zona> findAllIncludingInactive();

    /**
     * Convierte un {@link ZonaRequest} en una entidad {@link Zona}.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Zona mapeada con activo = true.
     */
    default Zona toEntity(ZonaRequest request) {
        if (request == null) {
            return null;
        }
        Zona zona = new Zona();
        zona.setLetra(request.getLetra());
        zona.setTipoVehiculo(request.getTipoVehiculo());
        zona.setCapacidadVehiculos(request.getCapacidadVehiculos());
        zona.setAnchoGarage(request.getAnchoGarage());
        zona.setLargoGarage(request.getLargoGarage());
        zona.setActivo(true);
        return zona;
    }

    /**
     * Actualiza los campos de una entidad {@link Zona} existente a partir de un {@link ZonaUpdate}.
     * Solo modifica los atributos que no sean nulos.
     *
     * @param zona   Entidad de zona existente.
     * @param update Objeto con los nuevos valores.
     */
    default void updateEntity(Zona zona, ZonaUpdate update) {
        if (zona == null || update == null) {
            return;
        }
        if (update.getLetra() != null) {
            zona.setLetra(update.getLetra());
        }
        if (update.getTipoVehiculo() != null) {
            zona.setTipoVehiculo(update.getTipoVehiculo());
        }
        if (update.getCapacidadVehiculos() != null) {
            zona.setCapacidadVehiculos(update.getCapacidadVehiculos());
        }
        if (update.getAnchoGarage() != null) {
            zona.setAnchoGarage(update.getAnchoGarage());
        }
        if (update.getLargoGarage() != null) {
            zona.setLargoGarage(update.getLargoGarage());
        }
    }

    /**
     * Convierte una entidad {@link Zona} en un {@link ZonaResponse}.
     *
     * @param zona Entidad Zona de origen.
     * @return Objeto de respuesta con la información pública.
     */
    default ZonaResponse fromEntity(Zona zona) {
        if (zona == null) {
            return null;
        }
        ZonaResponse response = new ZonaResponse();
        response.setId(zona.getId());
        response.setLetra(zona.getLetra());
        response.setTipoVehiculo(zona.getTipoVehiculo());
        response.setCapacidadVehiculos(zona.getCapacidadVehiculos());
        response.setAnchoGarage(zona.getAnchoGarage());
        response.setLargoGarage(zona.getLargoGarage());
        response.setActivo(zona.getActivo());
        return response;
    }
}

package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.PropiedadGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad PropiedadGarage.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface PropiedadGarageRepository extends JpaRepository<PropiedadGarage, Integer> {

    /**
     * Busca todas las propiedades de garage cuyo estado activo sea true.
     *
     * @return Lista de propiedades de garage activas.
     */
    List<PropiedadGarage> findAllByActivoTrue();

    /**
     * Busca una propiedad de garage por su ID asegurando que se encuentre activa.
     *
     * @param id ID de la propiedad de garage.
     * @return Optional con la propiedad encontrada si está activa.
     */
    Optional<PropiedadGarage> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de una propiedad de garage activa por su ID.
     *
     * @param id ID de la propiedad de garage.
     * @return true si existe y está activa, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico), utilizando consulta nativa.
     *
     * @return Lista completa de propiedades de garage (activas e inactivas).
     */
    @Query(value = "SELECT * FROM propiedades_garage", nativeQuery = true)
    List<PropiedadGarage> findAllIncludingInactive();

    /**
     * Convierte un {@link PropiedadGarageRequest} en una entidad {@link PropiedadGarage}.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad PropiedadGarage mapeada con activo = true.
     */
    default PropiedadGarage toEntity(PropiedadGarageRequest request) {
        if (request == null) {
            return null;
        }
        PropiedadGarage propiedad = new PropiedadGarage();

        if (request.getSocioId() != null) {
            Socio socio = new Socio();
            socio.setId(request.getSocioId());
            propiedad.setSocio(socio);
        }

        if (request.getGarageId() != null) {
            Garage garage = new Garage();
            garage.setId(request.getGarageId());
            propiedad.setGarage(garage);
        }

        propiedad.setFechaCompraGarage(request.getFechaCompraGarage());
        propiedad.setActivo(true);
        return propiedad;
    }

    /**
     * Actualiza los campos de una entidad {@link PropiedadGarage} existente.
     *
     * @param propiedad Entidad existente.
     * @param update    Objeto con los nuevos valores.
     */
    default void updateEntity(PropiedadGarage propiedad, PropiedadGarageUpdate update) {
        if (propiedad == null || update == null) {
            return;
        }
        if (update.getSocioId() != null) {
            Socio socio = new Socio();
            socio.setId(update.getSocioId());
            propiedad.setSocio(socio);
        }
        if (update.getGarageId() != null) {
            Garage garage = new Garage();
            garage.setId(update.getGarageId());
            propiedad.setGarage(garage);
        }
        if (update.getFechaCompraGarage() != null) {
            propiedad.setFechaCompraGarage(update.getFechaCompraGarage());
        }
    }

    /**
     * Convierte una entidad {@link PropiedadGarage} en un {@link PropiedadGarageResponse}.
     *
     * @param propiedad Entidad de origen.
     * @return Objeto de respuesta con los IDs y datos públicos.
     */
    default PropiedadGarageResponse fromEntity(PropiedadGarage propiedad) {
        if (propiedad == null) {
            return null;
        }
        PropiedadGarageResponse response = new PropiedadGarageResponse();
        response.setId(propiedad.getId());
        if (propiedad.getSocio() != null) {
            response.setSocioId(propiedad.getSocio().getId());
        }
        if (propiedad.getGarage() != null) {
            response.setGarageId(propiedad.getGarage().getId());
        }
        response.setFechaCompraGarage(propiedad.getFechaCompraGarage());
        response.setActivo(propiedad.getActivo());
        return response;
    }
}
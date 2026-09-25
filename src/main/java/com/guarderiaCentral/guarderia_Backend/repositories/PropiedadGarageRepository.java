package com.guarderiaCentral.guarderia_Backend.repositories;

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
 * Filtra por defecto los registros activos e incluye los métodos default de mapeo.
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
    @Query("SELECT p FROM PropiedadGarage p WHERE p.activo = true")
    List<PropiedadGarage> findAllActive();

    /**
     * Busca una propiedad de garage por su ID asegurando que se encuentre activa.
     *
     * @param id ID de la propiedad de garage.
     * @return Optional con la propiedad encontrada si está activa.
     */
    @Query("SELECT p FROM PropiedadGarage p WHERE p.id = :id AND p.activo = true")
    Optional<PropiedadGarage> findActiveById(int id);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de propiedades de garage (activas e inactivas).
     */
    @Query("SELECT p FROM PropiedadGarage p")
    List<PropiedadGarage> findAllIncludingInactive();

    /**
     * Convierte un {@link PropiedadGarageRequest} en una entidad {@link PropiedadGarage}.
     *
     * @param request Objeto con los datos de entrada.
     * @param socio   Entidad Socio asociada.
     * @param garage  Entidad Garage asociada.
     * @return Entidad PropiedadGarage mapeada con activo = true.
     */
    default PropiedadGarage toEntity(PropiedadGarageRequest request, Socio socio, Garage garage) {
        if (request == null) {
            return null;
        }
        PropiedadGarage propiedad = new PropiedadGarage();
        propiedad.setSocio(socio);
        propiedad.setGarage(garage);
        propiedad.setFechaCompraGarage(request.getFechaCompraGarage());
        propiedad.setActivo(true);
        return propiedad;
    }

    /**
     * Actualiza los campos de una entidad {@link PropiedadGarage} existente.
     *
     * @param propiedad Entidad existente.
     * @param update    Objeto con los nuevos valores.
     * @param socio     Nueva entidad Socio (opcional).
     * @param garage    Nueva entidad Garage (opcional).
     */
    default void updateEntity(PropiedadGarage propiedad, PropiedadGarageUpdate update, Socio socio, Garage garage) {
        if (propiedad == null || update == null) {
            return;
        }
        if (socio != null) {
            propiedad.setSocio(socio);
        }
        if (garage != null) {
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
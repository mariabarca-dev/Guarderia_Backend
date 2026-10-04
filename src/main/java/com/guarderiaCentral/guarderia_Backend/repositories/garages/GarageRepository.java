package com.guarderiaCentral.guarderia_Backend.repositories.garages;

import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Garage.
 * Por el @SQLRestriction de la entidad, los métodos heredados como findAll() y findById()
 * solo devuelven registros activos.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {

    /**
     * Busca un garage por su número único, incluyendo registros inactivos (baja lógica).
     * Utilizado en el flujo de guardado inteligente para detectar reactivaciones.
     *
     * @param numeroGarage Número de garage a buscar.
     * @return Optional con la entidad Garage si existe (activa o inactiva).
     */
    @Query(value = "SELECT * FROM garages WHERE numero_garage = ?1", nativeQuery = true)
    Optional<Garage> findByNumeroGarageIncludingInactive(int numeroGarage);

    /**
     * Verifica la existencia de garajes activos asociados a una zona específica.
     *
     * @param zonaId ID de la zona.
     * @return true si existen garajes activos en la zona, false en caso contrario.
     */
    @Query("SELECT COUNT(g) > 0 FROM Garage g WHERE g.zona.id = :zonaId AND g.activo = true")
    boolean existsByZonaIdAndActivoTrue(@Param("zonaId") Integer zonaId);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico), mediante una consulta nativa.
     *
     * @return Lista completa de garages (activos e inactivos).
     */
    @Query(value = "SELECT * FROM garages", nativeQuery = true)
    List<Garage> findAllIncludingInactive();

    /**
     * Convierte un {@link GarageRequest} en una entidad {@link Garage}.
     * Las relaciones se configuran como referencias vacías utilizando únicamente sus IDs.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Garage mapeada con activo = true.
     */
    default Garage toEntity(GarageRequest request) {
        if (request == null) {
            return null;
        }
        Garage garage = new Garage();
        garage.setNumeroGarage(request.getNumeroGarage());
        garage.setLecturaLuz(request.getLecturaLuz());
        garage.setServicioMantenimiento(request.getServicioMantenimiento());

        if (request.getZonaId() != null) {
            Zona zona = new Zona();
            zona.setId(request.getZonaId());
            garage.setZona(zona);
        }

        garage.setActivo(true);
        return garage;
    }

    /**
     * Actualiza los campos de una entidad {@link Garage} existente a partir de un {@link GarageUpdate}.
     * Solo modifica los atributos que no sean nulos.
     *
     * @param garage Entidad de garage existente.
     * @param update Objeto con los nuevos valores.
     */
    default void updateEntity(Garage garage, GarageUpdate update) {
        if (garage == null || update == null) {
            return;
        }
        if (update.getNumeroGarage() != null) {
            garage.setNumeroGarage(update.getNumeroGarage());
        }
        if (update.getLecturaLuz() != null) {
            garage.setLecturaLuz(update.getLecturaLuz());
        }
        if (update.getServicioMantenimiento() != null) {
            garage.setServicioMantenimiento(update.getServicioMantenimiento());
        }
        if (update.getZonaId() != null) {
            Zona zona = new Zona();
            zona.setId(update.getZonaId());
            garage.setZona(zona);
        }
    }

    /**
     * Convierte una entidad {@link Garage} en un {@link GarageResponse}.
     *
     * @param garage Entidad Garage de origen.
     * @return Objeto de respuesta con la información pública y la referencia a la zona.
     */
    default GarageResponse fromEntity(Garage garage) {
        if (garage == null) {
            return null;
        }
        GarageResponse response = new GarageResponse();
        response.setId(garage.getId());
        response.setNumeroGarage(garage.getNumeroGarage());
        response.setLecturaLuz(garage.getLecturaLuz());
        response.setServicioMantenimiento(garage.isServicioMantenimiento());
        if (garage.getZona() != null) {
            response.setZonaId(garage.getZona().getId());
        }
        response.setActivo(garage.getActivo());
        return response;
    }
}
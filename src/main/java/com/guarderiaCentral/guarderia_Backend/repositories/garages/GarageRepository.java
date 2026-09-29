package com.guarderiaCentral.guarderia_Backend.repositories.garages;

import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Garage.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {

    /**
     * Busca todos los garages cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de garages activos.
     */
    List<Garage> findAllByActivoTrue();

    /**
     * Busca un garage por su ID asegurando que se encuentre activo (Convención Spring Data).
     *
     * @param id ID del garage.
     * @return Optional con el garage encontrado si está activo.
     */
    Optional<Garage> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un garage activo por su ID.
     *
     * @param id ID del garage.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de garages (activos e inactivos).
     */
    @Query("SELECT g FROM Garage g")
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
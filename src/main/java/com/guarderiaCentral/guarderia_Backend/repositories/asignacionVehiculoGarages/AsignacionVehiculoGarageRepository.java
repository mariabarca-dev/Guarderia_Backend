package com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages;

import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//chequeo euge
/**
 * Repositorio Spring Data JPA para la entidad AsignacionVehiculoGarage.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface AsignacionVehiculoGarageRepository extends JpaRepository<AsignacionVehiculoGarage, Integer> {

    /**
     * Busca todas las asignaciones de vehículo a garage cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de asignaciones activas.
     */
    List<AsignacionVehiculoGarage> findAllByActivoTrue();

    /**
     * Busca una asignación por su ID asegurando que se encuentre activa (Convención Spring Data).
     *
     * @param id ID de la asignación.
     * @return Optional con la asignación encontrada si está activa.
     */
    Optional<AsignacionVehiculoGarage> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de una asignación activa por su ID.
     *
     * @param id ID de la asignación.
     * @return true si existe y está activa, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de asignaciones (activas e inactivas).
     */
    @Query("SELECT a FROM AsignacionVehiculoGarage a")
    List<AsignacionVehiculoGarage> findAllIncludingInactive();

    /**
     * Convierte un {@link AsignacionVehiculoGarageRequest} en una entidad {@link AsignacionVehiculoGarage}.
     * Las relaciones se configuran como referencias vacías utilizando únicamente sus IDs.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad AsignacionVehiculoGarage mapeada con activo = true.
     */
    default AsignacionVehiculoGarage toEntity(AsignacionVehiculoGarageRequest request) {
        if (request == null) {
            return null;
        }
        AsignacionVehiculoGarage asignacion = new AsignacionVehiculoGarage();

        if (request.getVehiculoId() != null) {
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setId(request.getVehiculoId());
            asignacion.setVehiculo(vehiculo);
        }

        if (request.getGarageId() != null) {
            Garage garage = new Garage();
            garage.setId(request.getGarageId());
            asignacion.setGarage(garage);
        }

        asignacion.setFechaAsignacionGarage(request.getFechaAsignacionGarage());
        asignacion.setActivo(true);
        return asignacion;
    }

    /**
     * Actualiza los campos de una entidad {@link AsignacionVehiculoGarage} existente.
     *
     * @param asignacion Entidad existente.
     * @param update     Objeto con los nuevos valores.
     */
    default void updateEntity(AsignacionVehiculoGarage asignacion, AsignacionVehiculoGarageUpdate update) {
        if (asignacion == null || update == null) {
            return;
        }
        if (update.getVehiculoId() != null) {
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setId(update.getVehiculoId());
            asignacion.setVehiculo(vehiculo);
        }
        if (update.getGarageId() != null) {
            Garage garage = new Garage();
            garage.setId(update.getGarageId());
            asignacion.setGarage(garage);
        }
        if (update.getFechaAsignacionGarage() != null) {
            asignacion.setFechaAsignacionGarage(update.getFechaAsignacionGarage());
        }
    }

    /**
     * Convierte una entidad {@link AsignacionVehiculoGarage} en un {@link AsignacionVehiculoGarageResponse}.
     *
     * @param asignacion Entidad de origen.
     * @return Objeto de respuesta con los IDs y datos públicos.
     */
    default AsignacionVehiculoGarageResponse fromEntity(AsignacionVehiculoGarage asignacion) {
        if (asignacion == null) {
            return null;
        }
        AsignacionVehiculoGarageResponse response = new AsignacionVehiculoGarageResponse();
        response.setId(asignacion.getId());
        if (asignacion.getVehiculo() != null) {
            response.setVehiculoId(asignacion.getVehiculo().getId());
        }
        if (asignacion.getGarage() != null) {
            response.setGarageId(asignacion.getGarage().getId());
        }
        response.setFechaAsignacionGarage(asignacion.getFechaAsignacionGarage());
        response.setActivo(asignacion.getActivo());
        return response;
    }
}
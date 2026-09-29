package com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas;

import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad AsignacionEmpleadoZona.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface AsignacionEmpleadoZonaRepository extends JpaRepository<AsignacionEmpleadoZona, Integer> {

    /**
     * Busca todas las asignaciones cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de asignaciones activas.
     */
    List<AsignacionEmpleadoZona> findAllByActivoTrue();

    /**
     * Busca una asignación por su ID asegurando que se encuentre activa (Convención Spring Data).
     *
     * @param id ID de la asignación.
     * @return Optional con la asignación encontrada si está activa.
     */
    Optional<AsignacionEmpleadoZona> findByIdAndActivoTrue(Integer id);

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
    @Query("SELECT a FROM AsignacionEmpleadoZona a")
    List<AsignacionEmpleadoZona> findAllIncludingInactive();

    /**
     * Convierte un {@link AsignacionEmpleadoZonaRequest} en una entidad {@link AsignacionEmpleadoZona}.
     * Las relaciones se configuran como referencias vacías utilizando únicamente sus IDs.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad AsignacionEmpleadoZona mapeada con activo = true.
     */
    default AsignacionEmpleadoZona toEntity(AsignacionEmpleadoZonaRequest request) {
        if (request == null) {
            return null;
        }
        AsignacionEmpleadoZona asignacion = new AsignacionEmpleadoZona();

        if (request.getEmpleadoId() != null) {
            Empleado empleado = new Empleado();
            empleado.setId(request.getEmpleadoId());
            asignacion.setEmpleado(empleado);
        }

        if (request.getZonaId() != null) {
            Zona zona = new Zona();
            zona.setId(request.getZonaId());
            asignacion.setZona(zona);
        }

        asignacion.setCantVehiculosACargo(request.getCantVehiculosACargo());
        asignacion.setActivo(true);
        return asignacion;
    }

    /**
     * Actualiza los campos de una entidad {@link AsignacionEmpleadoZona} existente.
     *
     * @param asignacion Entidad existente.
     * @param update     Objeto con los nuevos valores.
     */
    default void updateEntity(AsignacionEmpleadoZona asignacion, AsignacionEmpleadoZonaUpdate update) {
        if (asignacion == null || update == null) {
            return;
        }
        if (update.getEmpleadoId() != null) {
            Empleado empleado = new Empleado();
            empleado.setId(update.getEmpleadoId());
            asignacion.setEmpleado(empleado);
        }
        if (update.getZonaId() != null) {
            Zona zona = new Zona();
            zona.setId(update.getZonaId());
            asignacion.setZona(zona);
        }
        if (update.getCantVehiculosACargo() != null) {
            asignacion.setCantVehiculosACargo(update.getCantVehiculosACargo());
        }
    }

    /**
     * Convierte una entidad {@link AsignacionEmpleadoZona} en un {@link AsignacionEmpleadoZonaResponse}.
     *
     * @param asignacion Entidad de origen.
     * @return Objeto de respuesta con los IDs y datos públicos.
     */
    default AsignacionEmpleadoZonaResponse fromEntity(AsignacionEmpleadoZona asignacion) {
        if (asignacion == null) {
            return null;
        }
        AsignacionEmpleadoZonaResponse response = new AsignacionEmpleadoZonaResponse();
        response.setId(asignacion.getId());
        if (asignacion.getEmpleado() != null) {
            response.setEmpleadoId(asignacion.getEmpleado().getId());
        }
        if (asignacion.getZona() != null) {
            response.setZonaId(asignacion.getZona().getId());
        }
        response.setCantVehiculosACargo(asignacion.getCantVehiculosACargo());
        response.setActivo(asignacion.getActivo());
        return response;
    }
}
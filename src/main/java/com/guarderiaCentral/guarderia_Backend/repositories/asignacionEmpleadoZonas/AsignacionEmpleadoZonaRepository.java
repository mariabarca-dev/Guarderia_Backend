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
 * Filtra por defecto los registros activos mediante convención y encapsula métodos de mapeo por defecto.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface AsignacionEmpleadoZonaRepository extends JpaRepository<AsignacionEmpleadoZona, Integer> {

    /**
     * Busca todas las asignaciones activas.
     * @return Lista de asignaciones activas.
     */
    List<AsignacionEmpleadoZona> findAllByActivoTrue();

    /**
     * Busca una asignación activa por su ID.
     * @param id ID de la asignación.
     * @return Optional con la asignación encontrada si está activa.
     */
    Optional<AsignacionEmpleadoZona> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de una asignación activa por ID.
     * @param id ID de la asignación.
     * @return true si existe y está activa.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Busca todas las asignaciones activas asociadas a un empleado específico.
     * @param empleadoId ID del empleado.
     * @return Lista de asignaciones activas del empleado.
     */
    List<AsignacionEmpleadoZona> findAllByEmpleadoIdAndActivoTrue(Integer empleadoId);

    /**
     * Busca todas las asignaciones activas asociadas a una zona específica.
     * @param zonaId ID de la zona.
     * @return Lista de asignaciones activas de la zona.
     */
    List<AsignacionEmpleadoZona> findAllByZonaIdAndActivoTrue(Integer zonaId);

    /**
     * Método explícito para uso administrativo que devuelve todas las asignaciones (activas e inactivas).
     * @return Lista completa de asignaciones.
     */
    @Query("SELECT a FROM AsignacionEmpleadoZona a")
    List<AsignacionEmpleadoZona> findAllIncludingInactive();

    /**
     * Convierte un {@link AsignacionEmpleadoZonaRequest} en una entidad {@link AsignacionEmpleadoZona}.
     *
     * @param request Objeto con los datos de entrada.
     * @param empleado Entidad Empleado asociada.
     * @param zona Entidad Zona asociada.
     * @return Entidad mapeada con activo = true.
     */
    default AsignacionEmpleadoZona toEntity(AsignacionEmpleadoZonaRequest request, Empleado empleado, Zona zona) {
        if (request == null) {
            return null;
        }
        AsignacionEmpleadoZona asignacion = new AsignacionEmpleadoZona();
        asignacion.setEmpleado(empleado);
        asignacion.setZona(zona);
        asignacion.setCantVehiculosACargo(request.getCantVehiculosACargo() != null ? request.getCantVehiculosACargo() : 0);
        asignacion.setActivo(true);
        return asignacion;
    }

    /**
     * Actualiza los campos de una entidad {@link AsignacionEmpleadoZona} existente a partir de un {@link AsignacionEmpleadoZonaUpdate}.
     * Solo modifica los atributos no nulos.
     *
     * @param asignacion Entidad existente.
     * @param update Objeto con los nuevos valores.
     * @param empleadoNuevo Nuevo empleado asociado (opcional).
     * @param zonaNueva Nueva zona asociada (opcional).
     */
    default void updateEntity(AsignacionEmpleadoZona asignacion, AsignacionEmpleadoZonaUpdate update, Empleado empleadoNuevo, Zona zonaNueva) {
        if (asignacion == null || update == null) {
            return;
        }
        if (update.getCantVehiculosACargo() != null) {
            asignacion.setCantVehiculosACargo(update.getCantVehiculosACargo());
        }
        if (empleadoNuevo != null) {
            asignacion.setEmpleado(empleadoNuevo);
        }
        if (zonaNueva != null) {
            asignacion.setZona(zonaNueva);
        }
    }

    /**
     * Convierte una entidad {@link AsignacionEmpleadoZona} en un {@link AsignacionEmpleadoZonaResponse}.
     *
     * @param asignacion Entidad de origen.
     * @return DTO de respuesta con la información.
     */
    default AsignacionEmpleadoZonaResponse fromEntity(AsignacionEmpleadoZona asignacion) {
        if (asignacion == null) {
            return null;
        }
        AsignacionEmpleadoZonaResponse response = new AsignacionEmpleadoZonaResponse();
        response.setId(asignacion.getId());
        response.setEmpleadoId(asignacion.getEmpleado() != null ? asignacion.getEmpleado().getId() : null);
        response.setZonaId(asignacion.getZona() != null ? asignacion.getZona().getId() : null);
        response.setCantVehiculosACargo(asignacion.getCantVehiculosACargo());
        response.setActivo(asignacion.getActivo());
        return response;
    }
}
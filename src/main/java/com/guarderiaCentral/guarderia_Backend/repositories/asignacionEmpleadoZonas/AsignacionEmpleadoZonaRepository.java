package com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas;

import com.guarderiaCentral.guarderia_Backend.repositories.RelacionSegura;
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
     * Historial administrativo: todas las asignaciones, activas y dadas de baja, con los datos legibles del empleado
     * y de la zona ya resueltos en la misma consulta.
     * <p>
     * Se usa SQL nativo porque {@code @SQLRestriction("activo = true")} de las entidades también se aplica a las
     * consultas JPQL y a las relaciones, y dejaría afuera las bajas. Con los {@code LEFT JOIN} el código, nombre
     * y apellido del empleado y la letra de la zona se obtienen aunque el empleado o la zona estén dados de baja.
     * </p>
     * Orden de las columnas: id, empleado_id, codigo, nombre, apellido, zona_id, letra, cant_vehiculos_a_cargo, activo.
     *
     * @return filas crudas; convertirlas con {@link #filaAResponse(Object[])}.
     */
    @Query(value = "SELECT a.id, a.empleado_id, e.codigo, u.nombre, u.apellido, a.zona_id, z.letra, "
            + "a.cant_vehiculos_a_cargo, a.activo "
            + "FROM asignaciones_empleado_zona a "
            + "LEFT JOIN empleados e ON e.usuario_id = a.empleado_id "
            + "LEFT JOIN usuarios u ON u.id = a.empleado_id "
            + "LEFT JOIN zonas z ON z.id = a.zona_id "
            + "ORDER BY a.id", nativeQuery = true)
    List<Object[]> historialCompleto();

    /**
     * Convierte una fila de {@link #historialCompleto()} en un {@link AsignacionEmpleadoZonaResponse}.
     *
     * @param fila columnas en el orden indicado en {@link #historialCompleto()}
     * @return DTO de respuesta con la información legible
     */
    default AsignacionEmpleadoZonaResponse filaAResponse(Object[] fila) {
        AsignacionEmpleadoZonaResponse response = new AsignacionEmpleadoZonaResponse();
        response.setId(fila[0] != null ? ((Number) fila[0]).intValue() : null);
        response.setEmpleadoId(fila[1] != null ? ((Number) fila[1]).intValue() : null);
        response.setEmpleadoCodigo(fila[2] != null ? fila[2].toString() : null);
        response.setEmpleadoNombre(fila[3] != null ? fila[3].toString() : null);
        response.setEmpleadoApellido(fila[4] != null ? fila[4].toString() : null);
        response.setZonaId(fila[5] != null ? ((Number) fila[5]).intValue() : null);
        response.setZonaLetra(fila[6] != null ? fila[6].toString() : null);
        response.setCantVehiculosACargo(fila[7] != null ? ((Number) fila[7]).intValue() : 0);
        Object activo = fila[8];
        response.setActivo(activo instanceof Boolean ? (Boolean) activo : activo != null && ((Number) activo).intValue() != 0);
        return response;
    }

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
        if (asignacion.getEmpleado() != null) {
            response.setEmpleadoId(asignacion.getEmpleado().getId());
            response.setEmpleadoCodigo(RelacionSegura.leer(() -> asignacion.getEmpleado().getCodigo()));
            response.setEmpleadoNombre(RelacionSegura.leer(() -> asignacion.getEmpleado().getNombre()));
            response.setEmpleadoApellido(RelacionSegura.leer(() -> asignacion.getEmpleado().getApellido()));
        }
        if (asignacion.getZona() != null) {
            response.setZonaId(asignacion.getZona().getId());
            response.setZonaLetra(RelacionSegura.leer(() -> asignacion.getZona().getLetra()));
        }
        response.setCantVehiculosACargo(asignacion.getCantVehiculosACargo());
        response.setActivo(asignacion.getActivo());
        return response;
    }
}
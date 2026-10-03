package com.guarderiaCentral.guarderia_Backend.repositories.vehiculos;

import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Vehiculo.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    /**
     * Busca todos los vehículos cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de vehículos activos.
     */
    List<Vehiculo> findAllByActivoTrue();

    /**
     * Busca un vehículo por su ID asegurando que se encuentre activo (Convención Spring Data).
     *
     * @param id ID del vehículo.
     * @return Optional con el vehículo encontrado si está activo.
     */
    Optional<Vehiculo> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un vehículo activo por su ID.
     *
     * @param id ID del vehículo.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un vehículo activo por su matrícula.
     *
     * @param matricula Matrícula del vehículo.
     * @return true si existe y está activo.
     */
    boolean existsByMatriculaAndActivoTrue(String matricula);

    /**
     * Busca un vehículo por su matrícula única asegurando que esté activo.
     *
     * @param matricula Matrícula del vehículo.
     * @return Optional con el vehículo encontrado.
     */
    Optional<Vehiculo> findByMatriculaAndActivoTrue(String matricula);

    /**
     * Busca un vehículo por su matrícula permitiendo encontrar registros inactivos (borrado lógico)
     * para el flujo de guardado inteligente / reactivación.
     *
     * @param matricula Matrícula del vehículo.
     * @return Optional con el vehículo encontrado (activo o inactivo).
     */
    @Query(value = "SELECT * FROM vehiculos WHERE matricula = ?1", nativeQuery = true)
    Optional<Vehiculo> findByMatriculaIncludingInactive(String matricula);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico) mediante consulta nativa.
     *
     * @return Lista completa de vehículos (activos e inactivos).
     */
    @Query(value = "SELECT * FROM vehiculos", nativeQuery = true)
    List<Vehiculo> findAllIncludingInactive();

    /**
     * Convierte un {@link VehiculoRequest} en una entidad {@link Vehiculo}.
     * Las relaciones se configuran como referencias vacías utilizando únicamente sus IDs.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Vehiculo mapeada con activo = true.
     */
    default Vehiculo toEntity(VehiculoRequest request) {
        if (request == null) {
            return null;
        }
        Vehiculo vehiculo = new Vehiculo();

        if (request.getSocioId() != null) {
            Socio socio = new Socio();
            socio.setId(request.getSocioId());
            vehiculo.setSocio(socio);
        }

        vehiculo.setNombre(request.getNombre());
        vehiculo.setMatricula(request.getMatricula());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setProfundidad(request.getProfundidad());
        vehiculo.setAncho(request.getAncho());
        vehiculo.setActivo(true);
        return vehiculo;
    }

    /**
     * Actualiza los campos de una entidad {@link Vehiculo} existente a partir de un {@link VehiculoUpdate}.
     * Solo modifica los atributos que no sean nulos.
     *
     * @param vehiculo Entidad de vehículo existente.
     * @param update   Objeto con los nuevos valores.
     */
    default void updateEntity(Vehiculo vehiculo, VehiculoUpdate update) {
        if (vehiculo == null || update == null) {
            return;
        }
        if (update.getSocioId() != null) {
            Socio socio = new Socio();
            socio.setId(update.getSocioId());
            vehiculo.setSocio(socio);
        }
        if (update.getNombre() != null) {
            vehiculo.setNombre(update.getNombre());
        }
        if (update.getMatricula() != null) {
            vehiculo.setMatricula(update.getMatricula());
        }
        if (update.getTipo() != null) {
            vehiculo.setTipo(update.getTipo());
        }
        if (update.getProfundidad() != null) {
            vehiculo.setProfundidad(update.getProfundidad());
        }
        if (update.getAncho() != null) {
            vehiculo.setAncho(update.getAncho());
        }
    }

    /**
     * Convierte una entidad {@link Vehiculo} en un {@link VehiculoResponse}.
     *
     * @param vehiculo Entidad Vehiculo de origen.
     * @return Objeto de respuesta con la información pública y referencias.
     */
    default VehiculoResponse fromEntity(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        VehiculoResponse response = new VehiculoResponse();
        response.setId(vehiculo.getId());
        if (vehiculo.getSocio() != null) {
            response.setSocioId(vehiculo.getSocio().getId());
        }
        response.setNombre(vehiculo.getNombre());
        response.setMatricula(vehiculo.getMatricula());
        response.setTipo(vehiculo.getTipo());
        response.setProfundidad(vehiculo.getProfundidad());
        response.setAncho(vehiculo.getAncho());
        response.setActivo(vehiculo.getActivo());
        return response;
    }


    /**
     * Busca y retorna una lista de todos los vehículos activos asociados a un socio específico.
     *
     * @param socioId Identificador único del socio cuyos vehículos se desean consultar.
     * @return Una lista de objetos {@link Vehiculo} que pertenecen al socio y se encuentran activos (borrado lógico en true).
     */
    List<Vehiculo> findAllBySocioIdAndActivoTrue(Integer socioId);

    /**
     * Busca todos los vehículos activos filtrados por su tipo.
     *
     * @param tipo Tipo de vehículo (ej.MOTORHOME,CARAVANA,TRAILER ).
     * @return Lista de vehículos que coinciden con el tipo.
     */
    List<Vehiculo> findAllByTipoAndActivoTrue(TipoVehiculo tipo);

    /**
     * Busca todos los vehículos activos cuyas asignaciones de garaje pertenezcan a una lista de IDs de zonas.
     *
     * @param zonaIds Lista de identificadores de zonas.
     * @return Lista de vehículos activos en esas zonas.
     */
    @Query("SELECT v FROM Vehiculo v JOIN AsignacionVehiculoGarage a ON v.id = a.vehiculo.id JOIN a.garage g WHERE g.zona.id IN :zonaIds AND v.activo = true")
    List<Vehiculo> findAllByZonaIdInAndActivoTrue(@Param("zonaIds") List<Integer> zonaIds);
}
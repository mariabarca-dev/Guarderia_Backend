package com.guarderiaCentral.guarderia_Backend.repositories.vehiculos;

import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Vehiculo}.
 * Por el {@code @SQLRestriction} de la entidad, los métodos derivados y {@code findById}/{@code findAll}
 * solo ven registros activos; las consultas nativas se usan cuando hace falta ver también los inactivos.
 * Encapsula los métodos {@code default} de mapeo entre entidad y objetos de datos.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    /**
     * Busca todos los vehículos cuyo estado activo sea true.
     *
     * @return Lista de vehículos activos.
     */
    List<Vehiculo> findAllByActivoTrue();

    /**
     * Busca un vehículo por su ID asegurando que se encuentre activo.
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
     * Busca un vehículo por su matrícula única asegurando que esté activo.
     *
     * @param matricula Matrícula del vehículo.
     * @return Optional con el vehículo encontrado.
     */
    Optional<Vehiculo> findByMatriculaAndActivoTrue(String matricula);

    /**
     * Busca un vehículo por su matrícula incluyendo los registros inactivos (consulta nativa),
     * para el flujo de guardado inteligente (reactivación) y para detectar colisiones de matrícula.
     *
     * @param matricula Matrícula del vehículo.
     * @return Optional con el vehículo encontrado (activo o inactivo).
     */
    @Query(value = "SELECT * FROM vehiculos WHERE matricula = ?1", nativeQuery = true)
    Optional<Vehiculo> findByMatriculaIncludingInactive(String matricula);

    /**
     * Devuelve todos los vehículos, incluyendo los inactivos (borrado lógico), mediante consulta nativa.
     *
     * @return Lista completa de vehículos (activos e inactivos).
     */
    @Query(value = "SELECT * FROM vehiculos", nativeQuery = true)
    List<Vehiculo> findAllIncludingInactive();

    /**
     * Busca todos los vehículos activos que pertenecen a un socio.
     *
     * @param socioId Identificador del socio propietario.
     * @return Lista de vehículos activos del socio.
     */
    List<Vehiculo> findAllBySocioIdAndActivoTrue(Integer socioId);

    /**
     * Busca todos los vehículos activos de un tipo determinado.
     *
     * @param tipo Tipo de vehículo (por ejemplo MOTORHOME, CARAVANA o TRAILER).
     * @return Lista de vehículos activos que coinciden con el tipo.
     */
    List<Vehiculo> findAllByTipoAndActivoTrue(TipoVehiculo tipo);

    /**
     * Convierte un {@link VehiculoRequest} en una entidad {@link Vehiculo}.
     * La relación con el socio se arma asignando únicamente su ID.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Vehiculo mapeada con activo = true, o null si el request es null.
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
     * De la relación con el socio solo lee el ID, para no inicializar proxies LAZY.
     *
     * @param vehiculo Entidad Vehiculo de origen.
     * @return Objeto de respuesta con los datos del vehículo, o null si la entidad es null.
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
}
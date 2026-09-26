package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Vehiculo.
 * Filtra por defecto los registros activos e incluye los métodos default de mapeo.
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
    @Query("SELECT v FROM Vehiculo v WHERE v.activo = true")
    List<Vehiculo> findAllActive();

    /**
     * Busca un vehículo por su ID asegurando que se encuentre activo.
     *
     * @param id ID del vehículo.
     * @return Optional con el vehículo encontrado si está activo.
     */
    @Query("SELECT v FROM Vehiculo v WHERE v.id = :id AND v.activo = true")
    Optional<Vehiculo> findActiveById(Integer id);

    /**
     * Busca un vehículo por su matrícula única asegurando que esté activo.
     *
     * @param matricula Matrícula del vehículo.
     * @return Optional con el vehículo encontrado.
     */
    Optional<Vehiculo> findByMatriculaAndActivoTrue(String matricula);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de vehículos (activos e inactivos).
     */
    @Query("SELECT v FROM Vehiculo v")
    List<Vehiculo> findAllIncludingInactive();

    /**
     * Convierte un {@link VehiculoRequest} en una entidad {@link Vehiculo}.
     *
     * @param request  Objeto con los datos de entrada.
     * @param socio    Entidad Socio asociada.
     * @param empleado Entidad Empleado asociada.
     * @return Entidad Vehiculo mapeada con activo = true.
     */
    default Vehiculo toEntity(VehiculoRequest request, Socio socio, Empleado empleado) {
        if (request == null) {
            return null;
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setSocio(socio);
        vehiculo.setEmpleado(empleado);
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
     * @param socio    Nueva entidad Socio asociada (opcional).
     * @param empleado Nueva entidad Empleado asociada (opcional).
     */
    default void updateEntity(Vehiculo vehiculo, VehiculoUpdate update, Socio socio, Empleado empleado) {
        if (vehiculo == null || update == null) {
            return;
        }
        if (socio != null) {
            vehiculo.setSocio(socio);
        }
        if (empleado != null) {
            vehiculo.setEmpleado(empleado);
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
        if (vehiculo.getEmpleado() != null) {
            response.setEmpleadoId(vehiculo.getEmpleado().getId());
        }
        response.setName(vehiculo.getNombre());
        response.setMatricula(vehiculo.getMatricula());
        response.setTipo(vehiculo.getTipo());
        response.setProfundidad(vehiculo.getProfundidad());
        response.setAncho(vehiculo.getAncho());
        response.setActivo(vehiculo.getActivo());
        return response;
    }
}
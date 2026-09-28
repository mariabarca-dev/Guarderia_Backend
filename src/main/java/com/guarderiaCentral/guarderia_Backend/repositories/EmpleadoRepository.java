package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Empleado.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    /**
     * Busca todos los empleados cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de empleados activos.
     */
    List<Empleado> findAllByActivoTrue();

    /**
     * Busca un empleado por su ID asegurando que se encuentre activo (Convención Spring Data).
     *
     * @param id ID del empleado.
     * @return Optional con el empleado encontrado si está activo.
     */
    Optional<Empleado> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un empleado activo por su ID.
     *
     * @param id ID del empleado.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un empleado activo por su código único.
     *
     * @param codigo Código único del empleado.
     * @return true si existe y está activo.
     */
    boolean existsByCodigoAndActivoTrue(String codigo);

    /**
     * Verifica la existencia de un empleado activo por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario.
     * @return true si existe y está activo.
     */
    boolean existsByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Busca un empleado por su código único asegurando que esté activo.
     *
     * @param codigo Código único del empleado.
     * @return Optional con el empleado encontrado.
     */
    Optional<Empleado> findByCodigoAndActivoTrue(String codigo);

    /**
     * Busca un empleado por su nombre de usuario asegurando que esté activo.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return Optional con el empleado encontrado.
     */
    Optional<Empleado> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de empleados (activos e inactivos).
     */
    @Query("SELECT e FROM Empleado e")
    List<Empleado> findAllIncludingInactive();

    /**
     * Convierte un {@link EmpleadoRequest} en una entidad {@link Empleado}.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Empleado mapeada con activo = true.
     */
    default Empleado toEntity(EmpleadoRequest request) {
        if (request == null) {
            return null;
        }
        Empleado empleado = new Empleado();
        empleado.setNombre(request.getNombre());
        empleado.setApellido(request.getApellido());
        empleado.setDireccion(request.getDireccion());
        empleado.setTelefono(request.getTelefono());
        empleado.setNombreUsuario(request.getNombreUsuario());
        empleado.setClave(request.getClave());
        empleado.setRol(request.getRol());
        empleado.setCodigo(request.getCodigo());
        empleado.setEspecialidad(request.getEspecialidad());
        empleado.setActivo(true);
        return empleado;
    }

    /**
     * Actualiza los campos de una entidad {@link Empleado} existente a partir de un {@link EmpleadoUpdate}.
     * Solo modifica los atributos que no sean nulos.
     *
     * @param empleado Entidad de empleado existente.
     * @param update   Objeto con los nuevos valores.
     */
    default void updateEntity(Empleado empleado, EmpleadoUpdate update) {
        if (empleado == null || update == null) {
            return;
        }
        if (update.getNombre() != null) {
            empleado.setNombre(update.getNombre());
        }
        if (update.getApellido() != null) {
            empleado.setApellido(update.getApellido());
        }
        if (update.getDireccion() != null) {
            empleado.setDireccion(update.getDireccion());
        }
        if (update.getTelefono() != null) {
            empleado.setTelefono(update.getTelefono());
        }
        if (update.getNombreUsuario() != null) {
            empleado.setNombreUsuario(update.getNombreUsuario());
        }
        if (update.getClave() != null) {
            empleado.setClave(update.getClave());
        }
        if (update.getRol() != null) {
            empleado.setRol(update.getRol());
        }
        if (update.getCodigo() != null) {
            empleado.setCodigo(update.getCodigo());
        }
        if (update.getEspecialidad() != null) {
            empleado.setEspecialidad(update.getEspecialidad());
        }
    }

    /**
     * Convierte una entidad {@link Empleado} en un {@link EmpleadoResponse}.
     *
     * @param empleado Entidad Empleado de origen.
     * @return Objeto de respuesta con la información pública.
     */
    default EmpleadoResponse fromEntity(Empleado empleado) {
        if (empleado == null) {
            return null;
        }
        EmpleadoResponse response = new EmpleadoResponse();
        response.setId(empleado.getId());
        response.setNombre(empleado.getNombre());
        response.setApellido(empleado.getApellido());
        response.setDireccion(empleado.getDireccion());
        response.setTelefono(empleado.getTelefono());
        response.setNombreUsuario(empleado.getNombreUsuario());
        response.setRol(empleado.getRol());
        response.setCodigo(empleado.getCodigo());
        response.setEspecialidad(empleado.getEspecialidad());
        response.setActivo(empleado.getActivo());
        return response;
    }
}
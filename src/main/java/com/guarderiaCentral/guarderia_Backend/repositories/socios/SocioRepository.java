package com.guarderiaCentral.guarderia_Backend.repositories.socios;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Socio}.
 * Gestiona la persistencia, consultas específicas por DNI y nombre de usuario,
 * consultas nativas con JOIN para herencia JOINED y métodos por defecto de mapeo
 * entre {@link SocioRequest}, {@link SocioUpdate}, {@link SocioResponse} y la entidad.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 * @version 1.0
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Integer> {

    /**
     * Busca todos los socios cuyo estado activo sea true.
     *
     * @return Lista de socios activos.
     */
    List<Socio> findAllByActivoTrue();

    /**
     * Busca un socio por su ID asegurando que se encuentre activo.
     *
     * @param id ID del socio.
     * @return {@link Optional} con el socio encontrado si está activo.
     */
    Optional<Socio> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un socio activo por su ID.
     *
     * @param id ID del socio.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un socio activo por su DNI.
     *
     * @param dni DNI del socio.
     * @return true si existe y está activo.
     */
    boolean existsByDniAndActivoTrue(String dni);

    /**
     * Verifica la existencia de un socio activo por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario.
     * @return true si existe y está activo.
     */
    boolean existsByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Busca un socio por su DNI único asegurando que esté activo.
     *
     * @param dni DNI del socio.
     * @return {@link Optional} con el socio encontrado.
     */
    Optional<Socio> findByDniAndActivoTrue(String dni);

    /**
     * Busca un socio por su nombre de usuario asegurando que esté activo.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return {@link Optional} con el socio encontrado.
     */
    Optional<Socio> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Búsqueda por identificador único de socio incluyendo registros inactivos.
     * Realiza un JOIN nativo con la tabla raíz usuarios debido a la estrategia JOINED.
     *
     * @param id ID del socio.
     * @return {@link Optional} con la entidad Socio (activa o inactiva).
     */
    @Query(value = "SELECT u.*, s.* FROM usuarios u JOIN socios s ON s.usuario_id = u.id WHERE u.id = ?1", nativeQuery = true)
    Optional<Socio> findByIdIncludingInactive(Integer id);

    /**
     * Busca un socio por su DNI único incluyendo registros inactivos (borrado lógico).
     * Realiza un JOIN nativo con la tabla raíz usuarios debido a la estrategia JOINED.
     *
     * @param dni DNI del socio.
     * @return {@link Optional} con la entidad Socio (activa o inactiva).
     */
    @Query(value = "SELECT u.*, s.* FROM usuarios u JOIN socios s ON s.usuario_id = u.id WHERE s.dni = ?1", nativeQuery = true)
    Optional<Socio> findByDniIncludingInactive(String dni);

    /**
     * Devuelve la lista completa de socios (activos e inactivos).
     * Realiza un JOIN nativo con la tabla raíz usuarios debido a la estrategia JOINED.
     *
     * @return Lista completa de socios.
     */
    @Query(value = "SELECT u.*, s.* FROM usuarios u JOIN socios s ON s.usuario_id = u.id", nativeQuery = true)
    List<Socio> findAllIncludingInactive();

    /**
     * Convierte un {@link SocioRequest} en una entidad {@link Socio}.
     * El rol de la cuenta siempre es {@link Rol#SOCIO}: no se toma del Request, para que
     * quien da de alta un socio no pueda asignarle un rol con más permisos.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Socio mapeada con rol SOCIO y activo = true.
     */
    default Socio toEntity(SocioRequest request) {
        if (request == null) {
            return null;
        }
        Socio socio = new Socio();
        socio.setNombre(request.getNombre());
        socio.setApellido(request.getApellido());
        socio.setDireccion(request.getDireccion());
        socio.setTelefono(request.getTelefono());
        socio.setNombreUsuario(request.getNombreUsuario());
        socio.setClave(request.getClave());
        socio.setRol(Rol.SOCIO);
        socio.setDni(request.getDni());
        socio.setFechaIngreso(request.getFechaIngreso());
        socio.setActivo(true);
        return socio;
    }

    /**
     * Actualiza los campos de una entidad {@link Socio} existente a partir de un {@link SocioUpdate}.
     * Solo modifica los atributos que no sean nulos. El rol de la cuenta nunca se modifica.
     *
     * @param socio  Entidad de socio existente.
     * @param update Objeto con los nuevos valores.
     */
    default void updateEntity(Socio socio, SocioUpdate update) {
        if (socio == null || update == null) {
            return;
        }
        if (update.getNombre() != null) {
            socio.setNombre(update.getNombre());
        }
        if (update.getApellido() != null) {
            socio.setApellido(update.getApellido());
        }
        if (update.getDireccion() != null) {
            socio.setDireccion(update.getDireccion());
        }
        if (update.getTelefono() != null) {
            socio.setTelefono(update.getTelefono());
        }
        if (update.getNombreUsuario() != null) {
            socio.setNombreUsuario(update.getNombreUsuario());
        }
        if (update.getClave() != null) {
            socio.setClave(update.getClave());
        }
        if (update.getDni() != null) {
            socio.setDni(update.getDni());
        }
        if (update.getFechaIngreso() != null) {
            socio.setFechaIngreso(update.getFechaIngreso());
        }
    }

    /**
     * Convierte una entidad {@link Socio} en un {@link SocioResponse}.
     *
     * @param socio Entidad Socio de origen.
     * @return Objeto de respuesta con la información pública.
     */
    default SocioResponse fromEntity(Socio socio) {
        if (socio == null) {
            return null;
        }
        SocioResponse response = new SocioResponse();
        response.setId(socio.getId());
        response.setNombre(socio.getNombre());
        response.setApellido(socio.getApellido());
        response.setDireccion(socio.getDireccion());
        response.setTelefono(socio.getTelefono());
        response.setNombreUsuario(socio.getNombreUsuario());
        response.setRol(socio.getRol());
        response.setDni(socio.getDni());
        response.setFechaIngreso(socio.getFechaIngreso());
        response.setActivo(socio.getActivo());
        return response;
    }
}

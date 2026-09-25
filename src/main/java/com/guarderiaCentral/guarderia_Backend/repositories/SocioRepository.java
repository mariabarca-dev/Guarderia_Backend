package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Socio.
 * Filtra por defecto los registros activos e incluye los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Integer> {

    /**
     * Busca todos los socios cuyo estado activo sea true.
     *
     * @return Lista de socios activos.
     */
    @Query("SELECT s FROM Socio s WHERE s.activo = true")
    List<Socio> findAllActive();

    /**
     * Busca un socio por su ID asegurando que se encuentre activo.
     *
     * @param id ID del socio.
     * @return Optional con el socio encontrado si está activo.
     */
    @Query("SELECT s FROM Socio s WHERE s.id = :id AND s.activo = true")
    Optional<Socio> findActiveById(int id);

    /**
     * Busca un socio por su DNI único asegurando que esté activo.
     *
     * @param dni DNI del socio.
     * @return Optional con el socio encontrado.
     */
    Optional<Socio> findByDniAndActivoTrue(String dni);

    /**
     * Busca un socio por su nombre de usuario asegurando que esté activo.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return Optional con el socio encontrado.
     */
    Optional<Socio> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de socios (activos e inactivos).
     */
    @Query("SELECT s FROM Socio s")
    List<Socio> findAllIncludingInactive();

    /**
     * Convierte un {@link SocioRequest} en una entidad {@link Socio}.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Socio mapeada con activo = true.
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
        socio.setRol(request.getRol());
        socio.setDni(request.getDni());
        socio.setFechaIngreso(request.getFechaIngreso());
        socio.setActivo(true);
        return socio;
    }

    /**
     * Actualiza los campos de una entidad {@link Socio} existente a partir de un {@link SocioUpdate}.
     * Solo modifica los atributos que no sean nulos.
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
        if (update.getRol() != null) {
            socio.setRol(update.getRol());
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

package com.guarderiaCentral.guarderia_Backend.repositories.administradores;

import com.guarderiaCentral.guarderia_Backend.modelos.Administrador;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Administrador.
 * Filtra por defecto los registros activos mediante convención de Spring Data y encapsula los métodos default de mapeo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

    /**
     * Busca todos los administradores cuyo estado activo sea true (Convención Spring Data).
     *
     * @return Lista de administradores activos.
     */
    List<Administrador> findAllByActivoTrue();

    /**
     * Busca un administrador por su ID asegurando que se encuentre activo (Convención Spring Data).
     *
     * @param id ID del administrador.
     * @return Optional con el administrador encontrado si está activo.
     */
    Optional<Administrador> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un administrador activo por su ID.
     *
     * @param id ID del administrador.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Busca un administrador por su nombre de usuario asegurando que esté activo.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return Optional con el administrador encontrado.
     */
    Optional<Administrador> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Método explícito para uso administrativo que devuelve todos los registros,
     * incluyendo aquellos inactivos (borrado lógico).
     *
     * @return Lista completa de administradores (activos e inactivos).
     */
    @Query("SELECT a FROM Administrador a")
    List<Administrador> findAllIncludingInactive();

    /**
     * Convierte un {@link AdministradorRequest} en una entidad {@link Administrador}.
     *
     * @param request Objeto con los datos de entrada.
     * @return Entidad Administrador mapeada con activo = true.
     */
    default Administrador toEntity(AdministradorRequest request) {
        if (request == null) {
            return null;
        }
        Administrador administrador = new Administrador();
        administrador.setNombre(request.getNombre());
        administrador.setApellido(request.getApellido());
        administrador.setDireccion(request.getDireccion());
        administrador.setTelefono(request.getTelefono());
        administrador.setNombreUsuario(request.getNombreUsuario());
        administrador.setClave(request.getClave());
        administrador.setRol(request.getRol());
        administrador.setActivo(true);
        return administrador;
    }

    /**
     * Actualiza los campos de una entidad {@link Administrador} existente a partir de un {@link AdministradorUpdate}.
     * Solo modifica los atributos que no sean nulos.
     *
     * @param administrador Entidad de administrador existente.
     * @param update        Objeto con los nuevos valores.
     */
    default void updateEntity(Administrador administrador, AdministradorUpdate update) {
        if (administrador == null || update == null) {
            return;
        }
        if (update.getNombre() != null) {
            administrador.setNombre(update.getNombre());
        }
        if (update.getApellido() != null) {
            administrador.setApellido(update.getApellido());
        }
        if (update.getDireccion() != null) {
            administrador.setDireccion(update.getDireccion());
        }
        if (update.getTelefono() != null) {
            administrador.setTelefono(update.getTelefono());
        }
        if (update.getNombreUsuario() != null) {
            administrador.setNombreUsuario(update.getNombreUsuario());
        }
        if (update.getClave() != null) {
            administrador.setClave(update.getClave());
        }
        if (update.getRol() != null) {
            administrador.setRol(update.getRol());
        }
    }

    /**
     * Convierte una entidad {@link Administrador} en un {@link AdministradorResponse}.
     *
     * @param administrador Entidad Administrador de origen.
     * @return Objeto de respuesta con la información pública.
     */
    default AdministradorResponse fromEntity(Administrador administrador) {
        if (administrador == null) {
            return null;
        }
        AdministradorResponse response = new AdministradorResponse();
        response.setId(administrador.getId());
        response.setNombre(administrador.getNombre());
        response.setApellido(administrador.getApellido());
        response.setDireccion(administrador.getDireccion());
        response.setTelefono(administrador.getTelefono());
        response.setNombreUsuario(administrador.getNombreUsuario());
        response.setRol(administrador.getRol());
        response.setActivo(administrador.getActivo());
        return response;
    }
}
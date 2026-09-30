package com.guarderiaCentral.guarderia_Backend.repositories.usuarios;

import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de solo lectura para la entidad base Usuario.
 * Utilizado principalmente para consultas transversales como la autenticación (Login).
 * Las operaciones de escritura y gestión específica se realizan en los repositorios de las subclases.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca todos los usuarios activos del sistema sin importar su rol específico (Convención Spring Data).
     *
     * @return Lista de usuarios activos.
     */
    List<Usuario> findAllByActivoTrue();

    /**
     * Busca un usuario por su ID asegurando que se encuentre activo (Convención Spring Data).
     *
     * @param id ID del usuario.
     * @return Optional con el usuario encontrado si está activo.
     */
    Optional<Usuario> findByIdAndActivoTrue(Integer id);

    /**
     * Verifica la existencia de un usuario activo por su ID.
     *
     * @param id ID del usuario.
     * @return true si existe y está activo, false en caso contrario.
     */
    boolean existsByIdAndActivoTrue(Integer id);

    /**
     * Busca un usuario por su nombre de usuario asegurando que esté activo.
     * Esencial para el sistema de seguridad y login (AuthService).
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return Optional con el usuario (puede ser Socio, Empleado o Administrador gracias al polimorfismo).
     */
    Optional<Usuario> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Indica si existe un usuario con ese nombre de usuario, esté activo o dado de baja.
     *
     * @param nombreUsuario nombre de usuario a buscar
     * @return true si ya existe un usuario con ese nombre de usuario
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Busca todos los usuarios activos del sistema sin importar su rol específico (Consulta manual alternativa).
     *
     * @return Lista de usuarios activos.
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true")
    List<Usuario> findAllActive();

    /**
     * Búsqueda general para administración que incluye inactivos.
     *
     * @return Lista completa de usuarios.
     */
    @Query("SELECT u FROM Usuario u")
    List<Usuario> findAllIncludingInactive();
}
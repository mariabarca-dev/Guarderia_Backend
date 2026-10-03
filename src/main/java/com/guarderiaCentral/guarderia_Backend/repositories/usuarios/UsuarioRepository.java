package com.guarderiaCentral.guarderia_Backend.repositories.usuarios;

import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad base abstracta Usuario.
 * Utilizado para consultas transversales de autenticación y validación de unicitad de usuarios
 * en la tabla raíz 'usuarios' independientemente de su subclase concreta.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario activo por su nombre de usuario.
     * Requerido principalmente por AuthService para el proceso de autenticación.
     *
     * @param nombreUsuario Nombre de usuario único.
     * @return {@link Optional} con el usuario encontrado si está activo.
     */
    Optional<Usuario> findByNombreUsuarioAndActivoTrue(String nombreUsuario);

    /**
     * Indica si existe un usuario activo con el nombre de usuario especificado.
     *
     * @param nombreUsuario Nombre de usuario a buscar.
     * @return true si ya existe un usuario activo con ese nombre de usuario.
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Consulta nativa para verificar si un nombre de usuario ya existe en la tabla raíz 'usuarios',
     * considerando registros tanto activos como inactivos.
     *
     * @param nombreUsuario Nombre de usuario a verificar.
     * @return {@link Optional} con el ID del usuario si el nombre de usuario ya está registrado en el sistema.
     */
    @Query(value = "SELECT id FROM usuarios WHERE nombre_usuario = :nombreUsuario", nativeQuery = true)
    Optional<Integer> buscarIdPorNombreUsuarioIncluyendoInactivos(@Param("nombreUsuario") String nombreUsuario);

    /**
     * Consulta nativa para obtener la lista completa de todos los usuarios registrados
     * en la tabla raíz 'usuarios', incluyendo activos e inactivos (borrado lógico).
     *
     * @return Lista con todos los usuarios del sistema sin aplicar el filtro de borrado lógico.
     */
    @Query(value = "SELECT * FROM usuarios", nativeQuery = true)
    List<Usuario> listarTodosIncluyendoInactivos();

    /**
     * Consulta nativa para buscar un usuario por su identificador en la tabla raíz 'usuarios',
     * permitiendo recuperar registros activos e inactivos.
     *
     * @param id Identificador único del usuario.
     * @return {@link Optional} con el usuario encontrado.
     */
    @Query(value = "SELECT * FROM usuarios WHERE id = :id", nativeQuery = true)
    Optional<Usuario> buscarPorIdIncluyendoInactivos(@Param("id") Integer id);
}
package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import com.guarderiaCentral.guarderia_Backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Servicio de autenticación y gestión de detalles de usuario para Spring Security.
 * Implementa UserDetailsService para la carga de credenciales desde MySQL mediante JPA.
 *
 * @author Desarrollador Backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario de la base de datos utilizando su nombre de usuario (nombreUsuario).
     * Valida la existencia del registro y que el mismo se encuentre activo (borrado lógico).
     *
     * @param username el nombre de usuario único en el sistema
     * @return un objeto UserDetails compatible con Spring Security conteniendo los permisos y roles
     * @throws UsernameNotFoundException si el usuario no existe o se encuentra inactivo
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Intento de carga de credenciales para el usuario: {}", username);

        // Busca el usuario considerando que esté activo (filtrado base del repositorio JPA)
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndActivoTrue(username)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado o inactivo en el sistema: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // El rol de la entidad se mapea como GrantedAuthority anteponiendo "ROLE_" para @PreAuthorize
        String rolNombre = usuario.getRol().name();
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + rolNombre)
        );

        log.debug("Usuario cargado exitosamente: {}. Rol asignado: ROLE_{}", username, rolNombre);

        return new User(
                usuario.getNombreUsuario(),
                usuario.getClave(), // Contraseña cifrada con BCrypt en la base de datos
                true,
                true,
                true,
                true,
                authorities
        );
    }
}
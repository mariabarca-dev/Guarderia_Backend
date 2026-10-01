package com.guarderiaCentral.guarderia_Backend.config;

import com.guarderiaCentral.guarderia_Backend.modelos.Administrador;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea automáticamente, al iniciar la aplicación, el primer usuario con rol SYSADMIN
 * si todavía no existe. Las credenciales se leen de la configuración externa y la clave
 * se guarda encriptada con el PasswordEncoder de la aplicación.
 *
 * @author Guardería Central
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysAdminInitializer implements ApplicationRunner {

    private final AppEnvironmentConfig envConfig;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Ejecuta la creación del usuario SYSADMIN inicial. No hace nada si las credenciales
     * configuradas están vacías o si ya existe un usuario con ese nombre de usuario.
     *
     * @param args argumentos de arranque de la aplicación
     */
    @Override
    public void run(ApplicationArguments args) {
        String username = envConfig.getSysadminUsername();
        String password = envConfig.getSysadminPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("Credenciales del SYSADMIN vacías en la configuración. No se crea el usuario inicial.");
            return;
        }

        if (usuarioRepository.existsByNombreUsuario(username)) {
            log.info("El usuario SYSADMIN '{}' ya existe. No se crea nuevamente.", username);
            return;
        }

        Administrador sysadmin = new Administrador();
        sysadmin.setNombre("Sys");
        sysadmin.setApellido("Admin");
        sysadmin.setNombreUsuario(username);
        sysadmin.setClave(passwordEncoder.encode(password));
        sysadmin.setRol(Rol.SYSADMIN);
        sysadmin.setActivo(true);

        administradorRepository.save(sysadmin);
        log.info("Usuario SYSADMIN '{}' creado correctamente.", username);
    }
}
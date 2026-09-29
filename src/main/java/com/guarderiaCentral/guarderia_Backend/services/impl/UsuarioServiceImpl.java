package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.CredencialesInvalidasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de {@link UsuarioService} para la gestión de usuarios del sistema.
 * Maneja la seguridad mediante {@link PasswordEncoder}, consultas vía {@link UsuarioRepository}
 * y la persistencia del borrado lógico.
 *
 * @author GuarderiaCentral
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario validarLogin(String nombreUsuario, String clave) throws CredencialesInvalidasException {
        log.info("Intentando validar credenciales para el usuario: {}", nombreUsuario);

        Usuario u = usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("Fallo de autenticación: Usuario '{}' no encontrado o inactivo.", nombreUsuario);
                    return new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
                });

        if (!passwordEncoder.matches(clave, u.getClave())) {
            log.warn("Fallo de autenticación: Contraseña incorrecta para el usuario '{}'.", nombreUsuario);
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }

        log.info("Autenticación exitosa para el usuario: {}", nombreUsuario);
        return u;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tieneRol(Usuario usuario, Rol rolRequerido) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        return usuario.getRol() == rolRequerido;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorNombreUsuario(String nombreUsuario) throws RegistroNoEncontradoException {
        log.info("Buscando usuario activo por nombreUsuario: {}", nombreUsuario);

        Usuario u = usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("No se encontró el usuario activo: {}", nombreUsuario);
                    return new RegistroNoEncontradoException("No se encontró el usuario: " + nombreUsuario);
                });

        return mapToDTO(u);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarUsuarioPorId(Integer id) throws RegistroNoEncontradoException {
        log.info("Buscando usuario activo con ID: {}", id);

        Usuario u = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró el usuario activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id);
                });

        return mapToDTO(u);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        log.info("Listando todos los usuarios activos.");
        return usuarioRepository.findAllByActivoTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void actualizarUsuario(UsuarioDTO dto) throws RegistroNoEncontradoException {
        log.info("Actualizando datos del usuario con ID: {}", dto.getId());

        Usuario existente = usuarioRepository.findByIdAndActivoTrue(dto.getId())
                .orElseThrow(() -> {
                    log.warn("No se puede actualizar. Usuario con ID {} no encontrado o inactivo.", dto.getId());
                    return new RegistroNoEncontradoException("No se encontró el usuario con ID: " + dto.getId());
                });

        existente.setNombreUsuario(dto.getNombreUsuario());
        existente.setEmail(dto.getEmail());

        // Si el DTO incluye modificación de contraseña, se encripta antes de guardar
        if (dto.getClave() != null && !dto.getClave().isBlank()) {
            existente.setClave(passwordEncoder.encode(dto.getClave()));
        }

        usuarioRepository.save(existente);
        log.info("Usuario con ID {} actualizado correctamente.", dto.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarUsuario(Integer id) throws RegistroNoEncontradoException {
        log.info("Ejecutando borrado lógico para el usuario con ID: {}", id);

        Usuario u = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.warn("No se puede eliminar. Usuario con ID {} no encontrado o ya inactivo.", id);
                    return new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id);
                });

        u.setActivo(false);
        usuarioRepository.save(u);
        log.info("Borrado lógico realizado con éxito para el usuario con ID: {}", id);
    }

    /**
     * Convierte una entidad {@link Usuario} a su DTO correspondiente sin lógica en la clase DTO.
     *
     * @param usuario Entidad a convertir.
     * @return Objeto {@link UsuarioDTO}.
     */
    private UsuarioDTO mapToDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                null, // No se retorna la clave por motivos de seguridad
                usuario.getEmail(),
                usuario.getRol() != null ? usuario.getRol().name() : null
        );
    }
}
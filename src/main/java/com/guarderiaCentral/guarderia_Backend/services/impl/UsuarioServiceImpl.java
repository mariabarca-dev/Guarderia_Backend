package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Usuario;
import com.guarderiaCentral.guarderia_Backend.repositories.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.services.UsuarioService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El DTO de usuario no puede ser nulo.");
        }

        String usernameNorm = dto.getNombreUsuario().trim().toLowerCase();

        if (usuarioRepository.existsByNombreUsuario(usernameNorm)) {
            throw new ErrorNegocio("Error: Ya existe un usuario registrado con el nombre: " + dto.getNombreUsuario());
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(usernameNorm);
        usuario.setClave(passwordEncoder.encode(dto.getClave()));
        usuario.setRol(dto.getRol());

        Usuario guardado = usuarioRepository.save(usuario);
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id));
        return mapearAResponseDTO(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorNombreUsuario(String nombreUsuario) {
        String usernameNorm = nombreUsuario != null ? nombreUsuario.trim().toLowerCase() : "";
        Usuario u = usuarioRepository.findByNombreUsuario(usernameNorm)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el usuario: " + nombreUsuario));
        return mapearAResponseDTO(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id));

        String usernameNorm = dto.getNombreUsuario().trim().toLowerCase();

        usuarioRepository.findByNombreUsuario(usernameNorm).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new ErrorNegocio("Error: Ya existe otro usuario con el nombre: " + dto.getNombreUsuario());
            }
        });

        existente.setNombreUsuario(usernameNorm);
        if (dto.getClave() != null && !dto.getClave().isBlank()) {
            existente.setClave(passwordEncoder.encode(dto.getClave()));
        }
        existente.setRol(dto.getRol());

        Usuario actualizado = usuarioRepository.save(existente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RegistroNoEncontradoException("No se encontró el usuario con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO mapearAResponseDTO(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(u.getId());
        dto.setNombreUsuario(u.getNombreUsuario());
        dto.setRol(u.getRol());
        return dto;
    }
}

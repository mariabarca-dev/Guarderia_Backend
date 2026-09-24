package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto);

    UsuarioResponseDTO buscarUsuarioPorId(Long id);

    UsuarioResponseDTO buscarPorNombreUsuario(String nombreUsuario);

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto);

    void eliminarUsuario(Long id);
}

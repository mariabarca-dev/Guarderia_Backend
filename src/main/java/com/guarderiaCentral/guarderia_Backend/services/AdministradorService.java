package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.repositories.dtos.AdministradorRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.AdministradorResponseDTO;
import java.util.List;

public interface AdministradorService {

    List<AdministradorResponseDTO> listarTodos();

    AdministradorResponseDTO buscarPorId(Long id);

    AdministradorResponseDTO registrarAdministrador(AdministradorRequestDTO requestDTO);

    AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorRequestDTO requestDTO);

    void eliminarAdministrador(Long id);
}

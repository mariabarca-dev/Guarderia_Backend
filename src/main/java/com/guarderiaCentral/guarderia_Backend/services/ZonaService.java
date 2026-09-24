package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.ZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;

import java.util.List;

public interface ZonaService {

    ZonaResponseDTO registrarZona(ZonaRequestDTO dto);

    ZonaResponseDTO buscarPorId(Long id);

    ZonaResponseDTO buscarPorLetra(String letra);

    List<ZonaResponseDTO> listarTodas();

    ZonaResponseDTO actualizarZona(Long id, ZonaRequestDTO dto);

    void eliminarZona(Long id);

    void eliminarPorLetra(String letra);
}

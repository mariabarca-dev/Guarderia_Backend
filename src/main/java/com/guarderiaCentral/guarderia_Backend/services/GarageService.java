package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderiaCentral.guarderia_Backend.dtos.ReporteDisponibilidadZonaDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.GarageRequestDTO;

import java.util.List;

public interface GarageService {

    List<GarageResponseDTO> listarGarages(String usernameActual, boolean esSocio, String dniSocio);

    GarageResponseDTO buscarPorId(Long id);

    GarageResponseDTO buscarPorNumero(Integer numeroGarage, String usernameActual, boolean esSocio, String dniSocio);

    GarageResponseDTO registrarGarage(GarageRequestDTO requestDTO);

    GarageResponseDTO actualizarGarage(Long id, GarageRequestDTO requestDTO);

    void eliminarGarage(Integer numeroGarage);

    List<ReporteDisponibilidadZonaDTO> consultarDisponibilidadGarages();
}

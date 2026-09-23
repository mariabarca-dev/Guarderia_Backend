package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.EstadoGarageSocioResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.PropiedadGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.PropiedadGarageRequestDTO;

import java.util.List;

public interface PropiedadGarageService {

    PropiedadGarageResponseDTO registrarPropiedad(PropiedadGarageRequestDTO requestDTO);

    List<PropiedadGarageResponseDTO> listarTodas();

    EstadoGarageSocioResponseDTO obtenerEstadoGarageSocio(Long socioId, String usernameActual, boolean esSocio);

    List<GarageResponseDTO> listarPorSocio(Long socioId, String usernameActual, boolean esSocio);
}
package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.AsignacionVehiculoGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.AsignacionVehiculoGarageRequestDTO;

import java.util.List;

public interface AsignacionVehiculoGarageService {

    AsignacionVehiculoGarageResponseDTO crearAsignacion(AsignacionVehiculoGarageRequestDTO requestDTO);

    List<AsignacionVehiculoGarageResponseDTO> listarTodas();

    AsignacionVehiculoGarageResponseDTO buscarPorVehiculoId(Long vehiculoId, String usernameActual, boolean esSocio);

    AsignacionVehiculoGarageResponseDTO buscarPorGarageId(Long garageId);
}

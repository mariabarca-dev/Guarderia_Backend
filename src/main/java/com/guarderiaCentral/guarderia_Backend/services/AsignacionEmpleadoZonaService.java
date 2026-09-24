package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdateDTO;

import java.util.List;

public interface AsignacionEmpleadoZonaService {

    List<AsignacionEmpleadoZonaResponseDTO> listarTodas();

    List<AsignacionEmpleadoZonaResponseDTO> buscarPorCodigoEmpleado(String codigo);

    AsignacionEmpleadoZonaResponseDTO crearAsignacionPorIds(Long idEmpleado, Long idZona, Integer cantVehiculos);

    AsignacionEmpleadoZonaResponseDTO crearAsignacion(AsignacionEmpleadoZonaRequestDTO requestDTO);

    List<EmpleadoResponseDTO> obtenerEmpleadosPorZona(Long idZona);
}
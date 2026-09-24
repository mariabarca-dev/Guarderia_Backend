package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.EmpleadoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.EmpleadoRequestDTO;

import java.util.List;

public interface EmpleadoService {

    List<EmpleadoResponseDTO> listarTodos();

    EmpleadoResponseDTO buscarPorId(Long id);

    EmpleadoResponseDTO registrarEmpleado(EmpleadoRequestDTO requestDTO);

    EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO requestDTO);

    void eliminarEmpleado(Long id);

    List<ZonaResponseDTO> listarZonasAsignadas(Long empleadoId, String usernameActual, boolean esAdmin);

    List<VehiculoResponseDTO> listarVehiculosBajoResponsabilidad(Long empleadoId, String usernameActual, boolean esAdmin);
}

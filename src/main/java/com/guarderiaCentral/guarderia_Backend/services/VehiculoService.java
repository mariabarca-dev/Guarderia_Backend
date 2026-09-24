package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;

import java.util.List;

public interface VehiculoService {

    VehiculoResponseDTO registrarVehiculo(VehiculoRequestDTO dto);

    VehiculoResponseDTO buscarPorId(Long id);

    VehiculoResponseDTO buscarPorMatricula(String matricula);

    List<VehiculoResponseDTO> listarTodos();

    List<VehiculoResponseDTO> listarPorSocio(Long socioId);

    List<VehiculoResponseDTO> buscarPorTipo(TipoVehiculo tipo);

    List<VehiculoResponseDTO> listarVehiculosPorResponsable(Long empleadoId);

    List<VehiculoResponseDTO> listarVehiculosPorZona(Long zonaId);

    VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoRequestDTO dto);

    void eliminarVehiculo(Long id);

    void eliminarPorMatricula(String matricula);
}

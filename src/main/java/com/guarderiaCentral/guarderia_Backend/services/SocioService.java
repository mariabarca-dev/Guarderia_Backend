package com.guarderiaCentral.guarderia_Backend.services;

import com.guarderiaCentral.guarderia_Backend.dtos.*;
import java.util.List;

public interface SocioService {
    SocioResponseDTO registrarSocio(SocioRequestDTO dto);
    SocioResponseDTO buscarPorId(Long id);
    SocioResponseDTO buscarPorDni(String dni);
    List<SocioResponseDTO> listarTodos();
    SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO dto);
    void eliminarSocio(Long id);

    List<VehiculoResponseDTO> listarVehiculosPorSocio(Long socioId, String usernameActual, boolean esSocio);
    List<GarageResponseDTO> listarGarajesPorSocio(Long socioId, String usernameActual, boolean esSocio);
    EstadoGarageSocioResponseDTO obtenerEstadoGarageSocio(Long socioId, String usernameActual, boolean esSocio);
}

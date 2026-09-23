package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.ZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.services.ZonaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;
    private final GarageRepository garageRepository;

    public ZonaServiceImpl(ZonaRepository zonaRepository, GarageRepository garageRepository) {
        this.zonaRepository = zonaRepository;
        this.garageRepository = garageRepository;
    }

    @Override
    @Transactional
    public ZonaResponseDTO registrarZona(ZonaRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El objeto DTO no puede ser nulo.");
        }

        String letraNorm = dto.getLetra().trim().toUpperCase();

        if (zonaRepository.existsByLetra(letraNorm)) {
            throw new ErrorNegocio("Error: Ya existe una zona registrada con la letra: " + letraNorm);
        }

        Zona zona = new Zona();
        zona.setLetra(letraNorm);
        zona.setTipoVehiculo(dto.getTipoVehiculo());
        zona.setCapacidadVehiculos(dto.getCapacidadVehiculos());
        zona.setAncho(dto.getAncho());
        zona.setLargo(dto.getLargo());

        Zona guardada = zonaRepository.save(zona);
        return mapearAResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaResponseDTO buscarPorId(Long id) {
        Zona z = zonaRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró la zona con ID: " + id));
        return mapearAResponseDTO(z);
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaResponseDTO buscarPorLetra(String letra) {
        String letraNorm = letra != null ? letra.trim().toUpperCase() : "";
        Zona z = zonaRepository.findByLetra(letraNorm)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró la zona con letra: " + letra));
        return mapearAResponseDTO(z);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponseDTO> listarTodas() {
        return zonaRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ZonaResponseDTO actualizarZona(Long id, ZonaRequestDTO dto) {
        Zona existente = zonaRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede actualizar: La zona con ID " + id + " no existe."));

        String letraNorm = dto.getLetra().trim().toUpperCase();

        // Validar si se está intentando cambiar la letra por una que ya pertenece a otra zona
        zonaRepository.findByLetra(letraNorm).ifPresent(otraZona -> {
            if (!otraZona.getId().equals(id)) {
                throw new ErrorNegocio("Error: Ya existe otra zona con la letra: " + letraNorm);
            }
        });

        if (dto.getTipoVehiculo() != existente.getTipoVehiculo()) {
            boolean tieneGarajes = garageRepository.existsByZonaId(id);
            if (tieneGarajes) {
                throw new ErrorNegocio("Error: No se puede cambiar el tipo de vehículo de la zona '"
                        + letraNorm + "' porque ya posee garajes asociados.");
            }
        }

        existente.setLetra(letraNorm);
        existente.setTipoVehiculo(dto.getTipoVehiculo());
        existente.setCapacidadVehiculos(dto.getCapacidadVehiculos());
        existente.setAncho(dto.getAncho());
        existente.setLargo(dto.getLargo());

        Zona actualizada = zonaRepository.save(existente);
        return mapearAResponseDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminarZona(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede eliminar: La zona con ID " + id + " no existe."));

        boolean tieneGarajes = garageRepository.existsByZonaId(id);
        if (tieneGarajes) {
            throw new ErrorNegocio("Error: No se puede eliminar la zona '" + zona.getLetra()
                    + "' porque existen garajes asociados a ella.");
        }

        zonaRepository.delete(zona);
    }

    @Override
    @Transactional
    public void eliminarPorLetra(String letra) {
        String letraNorm = letra != null ? letra.trim().toUpperCase() : "";
        Zona zona = zonaRepository.findByLetra(letraNorm)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede eliminar: La zona " + letra + " no existe."));

        boolean tieneGarajes = garageRepository.existsByZonaId(zona.getId());
        if (tieneGarajes) {
            throw new ErrorNegocio("Error: No se puede eliminar la zona '" + letraNorm
                    + "' porque existen garajes asociados a ella.");
        }

        zonaRepository.delete(zona);
    }

    private ZonaResponseDTO mapearAResponseDTO(Zona z) {
        ZonaResponseDTO dto = new ZonaResponseDTO();
        dto.setId(z.getId());
        dto.setLetra(z.getLetra());
        dto.setTipoVehiculo(z.getTipoVehiculo());
        dto.setCapacidadVehiculos(z.getCapacidadVehiculos());
        dto.setAncho(z.getAncho());
        dto.setLargo(z.getLargo());
        return dto;
    }
}

package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.AsignacionVehiculoGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaOcupadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.AsignacionVehiculoGarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsignacionVehiculoGarageServiceImpl implements AsignacionVehiculoGarageService {

    private final AsignacionVehiculoGarageRepository asignacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final GarageRepository garageRepository;

    public AsignacionVehiculoGarageServiceImpl(AsignacionVehiculoGarageRepository asignacionRepository,
                                               VehiculoRepository vehiculoRepository,
                                               GarageRepository garageRepository) {
        this.asignacionRepository = asignacionRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.garageRepository = garageRepository;
    }

    @Override
    @Transactional
    public AsignacionVehiculoGarageResponseDTO crearAsignacion(AsignacionVehiculoGarageRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El objeto de asignación no puede ser nulo.");
        }

        if (dto.getFechaAsignacionGarage() == null) {
            throw new ErrorNegocio("La fecha de asignación no puede ser nula.");
        }

        if (dto.getFechaAsignacionGarage().isAfter(LocalDate.now())) {
            throw new ErrorNegocio("La fecha de asignación no puede ser una fecha futura.");
        }

        // 1. Obtención y validación de existencia de entidades
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el vehículo con ID: " + dto.getVehiculoId()));

        Garage garage = garageRepository.findById(dto.getGarageId())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el garaje con ID: " + dto.getGarageId()));

        // 2. REGLA DE NEGOCIO: Excepción específica si el garaje ya está ocupado por un vehículo
        if (asignacionRepository.existsByGarageId(garage.getId())) {
            throw new GarageYaOcupadoException("El garaje N° " + garage.getNumeroGarage()
                    + " ya se encuentra ocupado por otro vehículo.");
        }

        // 3. REGLA DE NEGOCIO: El vehículo no puede tener otra asignación activa
        if (asignacionRepository.existsByVehiculoId(vehiculo.getId())) {
            throw new ErrorNegocio("El vehículo con matrícula " + vehiculo.getMatricula()
                    + " ya está asignado a un garaje en el sistema.");
        }

        // 4. REGLA DE NEGOCIO: El vehículo debe pertenecer al socio dueño del garaje
        if (garage.getSocioPropietario() != null && vehiculo.getSocio() != null) {
            if (!vehiculo.getSocio().getId().equals(garage.getSocioPropietario().getId())) {
                throw new ErrorNegocio("El vehículo no pertenece al socio propietario de este garaje.");
            }
        }

        // 5. REGLA DE NEGOCIO: Compatibilidad de tipo de vehículo con el tipo de la Zona
        if (garage.getZona() != null && vehiculo.getTipo() != garage.getZona().getTipoVehiculo()) {
            throw new ErrorNegocio("El vehículo de tipo " + vehiculo.getTipo()
                    + " no es compatible con la zona asignada a " + garage.getZona().getTipoVehiculo() + ".");
        }

        // 6. REGLA DE NEGOCIO: Capacidad máxima en la Zona
        Zona zona = garage.getZona();
        if (zona != null) {
            int capacidadMaxima = zona.getCapacidadVehiculos();
            int vehiculosActuales = asignacionRepository.countByGarageZonaId(zona.getId());

            if (vehiculosActuales >= capacidadMaxima) {
                throw new ZonaSinCapacidadException("La zona '" + zona.getLetra()
                        + "' ha alcanzado su capacidad máxima permitida de " + capacidadMaxima + " vehículos.");
            }
        }

        // 7. Mapeo y Persistencia
        AsignacionVehiculoGarage nuevaAsignacion = new AsignacionVehiculoGarage();
        nuevaAsignacion.setVehiculo(vehiculo);
        nuevaAsignacion.setGarage(garage);
        nuevaAsignacion.setFechaAsignacionGarage(dto.getFechaAsignacionGarage());

        AsignacionVehiculoGarage guardada = asignacionRepository.save(nuevaAsignacion);
        return mapearAResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionVehiculoGarageResponseDTO> listarTodas() {
        return asignacionRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionVehiculoGarageResponseDTO buscarPorVehiculoId(Long vehiculoId, String usernameActual, boolean esSocio) {
        if (vehiculoId == null || vehiculoId <= 0) {
            throw new ErrorNegocio("El ID de vehículo proporcionado no es válido.");
        }

        AsignacionVehiculoGarage asignacion = asignacionRepository.findByVehiculoId(vehiculoId)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró asignación para el vehículo con ID: " + vehiculoId));

        // Control de Privacidad: Si es socio, verificar que sea el dueño del vehículo
        if (esSocio) {
            if (asignacion.getVehiculo() == null || asignacion.getVehiculo().getSocio() == null ||
                    !asignacion.getVehiculo().getSocio().getNombreUsuario().equalsIgnoreCase(usernameActual)) {
                throw new AccessDeniedException("Acceso denegado: Solo puede consultar la asignación de sus propios vehículos.");
            }
        }

        return mapearAResponseDTO(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionVehiculoGarageResponseDTO buscarPorGarageId(Long garageId) {
        if (garageId == null || garageId <= 0) {
            throw new ErrorNegocio("El ID de garaje proporcionado no es válido.");
        }

        AsignacionVehiculoGarage asignacion = asignacionRepository.findByGarageId(garageId)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró asignación para el garaje con ID: " + garageId));

        return mapearAResponseDTO(asignacion);
    }

    // Auxiliares de Mapeo Interno
    private AsignacionVehiculoGarageResponseDTO mapearAResponseDTO(AsignacionVehiculoGarage asignacion) {
        AsignacionVehiculoGarageResponseDTO dto = new AsignacionVehiculoGarageResponseDTO();
        dto.setId(asignacion.getId());
        dto.setFechaAsignacionGarage(asignacion.getFechaAsignacionGarage());

        if (asignacion.getVehiculo() != null) {
            VehiculoResponseDTO vDto = new VehiculoResponseDTO();
            vDto.setId(asignacion.getVehiculo().getId());
            vDto.setMatricula(asignacion.getVehiculo().getMatricula());
            vDto.setTipo(asignacion.getVehiculo().getTipo());
            dto.setVehiculo(vDto);
        }

        if (asignacion.getGarage() != null) {
            GarageResponseDTO gDto = new GarageResponseDTO();
            gDto.setId(asignacion.getGarage().getId());
            gDto.setNumeroGarage(asignacion.getGarage().getNumeroGarage());
            dto.setGarage(gDto);
        }

        return dto;
    }
}

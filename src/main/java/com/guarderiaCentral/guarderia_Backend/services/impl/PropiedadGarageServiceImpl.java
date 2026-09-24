package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.EstadoGarageSocioResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.PropiedadGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaVendidoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.PropiedadGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.PropiedadGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.PropiedadGarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropiedadGarageServiceImpl implements PropiedadGarageService {

    private final PropiedadGarageRepository propiedadRepository;
    private final SocioRepository socioRepository;
    private final GarageRepository garageRepository;
    private final AsignacionVehiculoGarageRepository asignacionRepository;

    public PropiedadGarageServiceImpl(PropiedadGarageRepository propiedadRepository,
                                      SocioRepository socioRepository,
                                      GarageRepository garageRepository,
                                      AsignacionVehiculoGarageRepository asignacionRepository) {
        this.propiedadRepository = propiedadRepository;
        this.socioRepository = socioRepository;
        this.garageRepository = garageRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional
    public PropiedadGarageResponseDTO registrarPropiedad(PropiedadGarageRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El objeto DTO no puede ser nulo.");
        }

        if (dto.getFechaCompraGarage() == null) {
            throw new ErrorNegocio("La fecha de compra no puede ser nula.");
        }

        if (dto.getFechaCompraGarage().isAfter(LocalDate.now())) {
            throw new ErrorNegocio("La fecha de compra no puede ser una fecha futura.");
        }

        // 1. Validar existencia del socio y del garaje
        Socio socio = socioRepository.findById(dto.getSocioId())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio con ID: " + dto.getSocioId()));

        Garage garage = garageRepository.findByNumeroGarage(dto.getNumeroGarage())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el garaje N°: " + dto.getNumeroGarage()));

        // 2. REGLA DE NEGOCIO: Excepción específica si el garaje ya tiene dueño
        if (garage.getSocioPropietario() != null) {
            throw new GarageYaVendidoException("Error: El garaje N° " + garage.getNumeroGarage()
                    + " ya tiene un socio propietario asignado.");
        }

        // 3. REGLA DE NEGOCIO: La fecha de compra no puede ser anterior a la fecha de ingreso del socio
        if (socio.getFechaIngreso() != null && dto.getFechaCompraGarage().isBefore(socio.getFechaIngreso())) {
            throw new ErrorNegocio("Error de negocio: La fecha de compra (" + dto.getFechaCompraGarage()
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").");
        }

        // 4. Mapear y registrar la propiedad
        PropiedadGarage nuevaPropiedad = new PropiedadGarage();
        nuevaPropiedad.setSocio(socio);
        nuevaPropiedad.setGarage(garage);
        nuevaPropiedad.setFechaCompraGarage(dto.getFechaCompraGarage());

        PropiedadGarage guardada = propiedadRepository.save(nuevaPropiedad);

        // 5. Vinculación bidireccional en el garaje
        garage.setSocioPropietario(socio);
        garageRepository.save(garage);

        return mapearAResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponseDTO> listarTodas() {
        return propiedadRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoGarageSocioResponseDTO obtenerEstadoGarageSocio(Long socioId, String usernameActual, boolean esSocio) {
        Socio socio = validarYObtenerSocio(socioId, usernameActual, esSocio);

        EstadoGarageSocioResponseDTO respuesta = new EstadoGarageSocioResponseDTO();

        Optional<PropiedadGarage> propiedadOpt = propiedadRepository.findBySocioId(socio.getId());
        if (propiedadOpt.isEmpty()) {
            respuesta.setMensaje("No posee ningún garaje registrado como propiedad.");
            return respuesta;
        }

        Garage garage = propiedadOpt.get().getGarage();
        respuesta.setGarageId(garage.getId());
        respuesta.setNumeroGarage(garage.getNumeroGarage());

        Optional<AsignacionVehiculoGarage> asignacionOpt = asignacionRepository.findByGarageId(garage.getId());

        if (asignacionOpt.isPresent()) {
            AsignacionVehiculoGarage asignacion = asignacionOpt.get();
            respuesta.setEstado("OCUPADO");
            if (asignacion.getVehiculo() != null) {
                respuesta.setVehiculoMatricula(asignacion.getVehiculo().getMatricula());
                respuesta.setVehiculoNombre(asignacion.getVehiculo().getNombre());
            }
            respuesta.setMensaje("Garaje ocupado por vehículo asignado.");
        } else {
            respuesta.setEstado("LIBRE");
            respuesta.setMensaje("Puede asignar un vehículo a su garaje.");
        }

        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponseDTO> listarPorSocio(Long socioId, String usernameActual, boolean esSocio) {
        Socio socio = validarYObtenerSocio(socioId, usernameActual, esSocio);

        return garageRepository.findBySocioPropietarioId(socio.getId()).stream()
                .map(g -> {
                    GarageResponseDTO dto = new GarageResponseDTO();
                    dto.setId(g.getId());
                    dto.setNumeroGarage(g.getNumeroGarage());
                    dto.setLecturaLuz(g.getLecturaLuz());
                    if (g.getZona() != null) {
                        dto.setZona(g.getZona().getLetra());
                    }
                    dto.setSocioPropietarioDni(socio.getDni());
                    dto.setSocioPropietarioNombre(socio.getNombre() + " " + socio.getApellido());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Socio validarYObtenerSocio(Long socioId, String usernameActual, boolean esSocio) {
        if (socioId == null || socioId <= 0) {
            throw new ErrorNegocio("El ID de socio proporcionado no es válido.");
        }

        Socio socio = socioRepository.findById(socioId)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio con ID: " + socioId));

        if (esSocio && !socio.getNombreUsuario().equalsIgnoreCase(usernameActual)) {
            throw new AccessDeniedException("Acceso denegado: No tiene permisos para consultar información de otro socio.");
        }

        return socio;
    }

    private PropiedadGarageResponseDTO mapearAResponseDTO(PropiedadGarage p) {
        PropiedadGarageResponseDTO dto = new PropiedadGarageResponseDTO();
        dto.setId(p.getId());
        dto.setFechaCompraGarage(p.getFechaCompraGarage());

        if (p.getSocio() != null) {
            dto.setSocioId(p.getSocio().getId());
            dto.setSocioNombre(p.getSocio().getNombre() + " " + p.getSocio().getApellido());
            dto.setSocioDni(p.getSocio().getDni());
        }

        if (p.getGarage() != null) {
            dto.setGarageId(p.getGarage().getId());
            dto.setNumeroGarage(p.getGarage().getNumeroGarage());
        }

        return dto;
    }
}

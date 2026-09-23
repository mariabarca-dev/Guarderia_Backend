package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ReporteDisponibilidadZonaDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.GarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.GarageService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;
    private final ZonaRepository zonaRepository;
    private final SocioRepository socioRepository;
    private final AsignacionVehiculoGarageRepository asignacionRepository;

    public GarageServiceImpl(GarageRepository garageRepository,
                             ZonaRepository zonaRepository,
                             SocioRepository socioRepository,
                             AsignacionVehiculoGarageRepository asignacionRepository) {
        this.garageRepository = garageRepository;
        this.zonaRepository = zonaRepository;
        this.socioRepository = socioRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponseDTO> listarGarages(String usernameActual, boolean esSocio, String dniSocio) {
        if (esSocio) {
            // Un socio solo lista sus propios garajes
            return garageRepository.findBySocioPropietarioDni(dniSocio).stream()
                    .map(this::mapearAResponseDTO)
                    .collect(Collectors.toList());
        }

        return garageRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GarageResponseDTO buscarPorId(Long id) {
        Garage garage = garageRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el garaje con ID: " + id));
        return mapearAResponseDTO(garage);
    }

    @Override
    @Transactional(readOnly = true)
    public GarageResponseDTO buscarPorNumero(Integer numeroGarage, String usernameActual, boolean esSocio, String dniSocio) {
        Garage garage = garageRepository.findByNumeroGarage(numeroGarage)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el garaje número: " + numeroGarage));

        if (esSocio) {
            if (garage.getSocioPropietario() == null || !garage.getSocioPropietario().getDni().equals(dniSocio)) {
                throw new AccessDeniedException("Acceso denegado: El garaje N° " + numeroGarage + " no está asociado a su cuenta.");
            }
        }

        return mapearAResponseDTO(garage);
    }

    @Override
    @Transactional
    public GarageResponseDTO registrarGarage(GarageRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El objeto de solicitud no puede ser nulo.");
        }

        String codigoZona = dto.getZona().trim().toUpperCase();

        // 1. Validar existencia de la Zona
        Zona zona = zonaRepository.findByLetra(codigoZona)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe la zona especificada: " + codigoZona));

        // 2. Validar capacidad máxima de garajes creados en la zona
        long garajesEnZona = garageRepository.countByZonaLetra(codigoZona);
        if (garajesEnZona >= zona.getCapacidadVehiculos()) {
            throw new ZonaSinCapacidadException("La zona '" + codigoZona + "' alcanzó su capacidad máxima de "
                    + zona.getCapacidadVehiculos() + " garajes.");
        }

        // 3. Validar unicidad del número de garaje
        if (garageRepository.existsByNumeroGarage(dto.getNumeroGarage())) {
            throw new ErrorNegocio("Ya existe un garaje registrado con el número: " + dto.getNumeroGarage());
        }

        // 4. Resolver Socio Propietario (si aplica)
        Socio socioPropietario = resolverSocioPropietario(dto.getSocioPropietario());

        // 5. Instanciar y Persistir
        Garage garage = new Garage();
        garage.setNumeroGarage(dto.getNumeroGarage());
        garage.setLecturaLuz(dto.getLecturaLuz());
        garage.setZona(zona);
        garage.setSocioPropietario(socioPropietario);

        Garage guardado = garageRepository.save(garage);
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional
    public GarageResponseDTO actualizarGarage(Long id, GarageRequestDTO dto) {
        Garage existente = garageRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el garaje con ID: " + id));

        String codigoZona = dto.getZona().trim().toUpperCase();

        Zona zona = zonaRepository.findByLetra(codigoZona)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe la zona especificada: " + codigoZona));

        // Verificar si se cambió el número de garaje y si el nuevo ya existe
        if (!existente.getNumeroGarage().equals(dto.getNumeroGarage()) &&
                garageRepository.existsByNumeroGarage(dto.getNumeroGarage())) {
            throw new ErrorNegocio("Ya existe un garaje registrado con el número: " + dto.getNumeroGarage());
        }

        Socio socioPropietario = resolverSocioPropietario(dto.getSocioPropietario());

        existente.setNumeroGarage(dto.getNumeroGarage());
        existente.setLecturaLuz(dto.getLecturaLuz());
        existente.setZona(zona);
        existente.setSocioPropietario(socioPropietario);

        Garage actualizado = garageRepository.save(existente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarGarage(Integer numeroGarage) {
        Garage existente = garageRepository.findByNumeroGarage(numeroGarage)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede eliminar: El garaje N° " + numeroGarage + " no existe."));

        garageRepository.delete(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDisponibilidadZonaDTO> consultarDisponibilidadGarages() {
        List<ReporteDisponibilidadZonaDTO> reporte = new ArrayList<>();
        List<Zona> zonas = zonaRepository.findAll();

        for (Zona z : zonas) {
            long ocupadosReales = asignacionRepository.countByGarageZonaId(z.getId());
            int disponibles = Math.max(0, z.getCapacidadVehiculos() - (int) ocupadosReales);

            ReporteDisponibilidadZonaDTO dto = new ReporteDisponibilidadZonaDTO();
            dto.setLetraZona(z.getLetra());
            dto.setTipoVehiculo(z.getTipoVehiculo() != null ? z.getTipoVehiculo().name() : "N/A");
            dto.setCapacidadTotal(z.getCapacidadVehiculos());
            dto.setOcupados(ocupadosReales);
            dto.setDisponibles(disponibles);

            reporte.add(dto);
        }

        return reporte;
    }

    private Socio resolverSocioPropietario(String dniSocio) {
        if (dniSocio == null || dniSocio.isBlank() || dniSocio.equalsIgnoreCase("Libre")) {
            return null;
        }
        return socioRepository.findByDni(dniSocio.trim())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio con DNI: " + dniSocio));
    }

    private GarageResponseDTO mapearAResponseDTO(Garage g) {
        GarageResponseDTO dto = new GarageResponseDTO();
        dto.setId(g.getId());
        dto.setNumeroGarage(g.getNumeroGarage());
        dto.setLecturaLuz(g.getLecturaLuz());

        if (g.getZona() != null) {
            dto.setZona(g.getZona().getLetra());
        }

        if (g.getSocioPropietario() != null) {
            dto.setSocioPropietarioDni(g.getSocioPropietario().getDni());
            dto.setSocioPropietarioNombre(g.getSocioPropietario().getNombre() + " " + g.getSocioPropietario().getApellido());
        } else {
            dto.setSocioPropietarioDni("Libre");
        }

        return dto;
    }
}

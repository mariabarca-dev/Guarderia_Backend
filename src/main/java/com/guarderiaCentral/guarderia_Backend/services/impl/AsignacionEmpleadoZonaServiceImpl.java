package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdateDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionEmpleadoZonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AsignacionEmpleadoZonaServiceImpl implements AsignacionEmpleadoZonaService {

    private final AsignacionEmpleadoZonaRepository asignacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ZonaRepository zonaRepository;

    public AsignacionEmpleadoZonaServiceImpl(AsignacionEmpleadoZonaRepository asignacionRepository,
                                             EmpleadoRepository empleadoRepository,
                                             ZonaRepository zonaRepository) {
        this.asignacionRepository = asignacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.zonaRepository = zonaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponseDTO> listarTodas() {
        return asignacionRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponseDTO> buscarPorCodigoEmpleado(String codigo) {
        return asignacionRepository.findByEmpleadoCodigo(codigo).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponseDTO crearAsignacionPorIds(Long idEmpleado, Long idZona, Integer cantVehiculos) {
        if (idEmpleado == null || idEmpleado <= 0 || idZona == null || idZona <= 0) {
            throw new ErrorNegocio("Los IDs de empleado y zona deben ser mayores a cero.");
        }

        AsignacionEmpleadoZonaRequestDTO requestDTO = new AsignacionEmpleadoZonaRequestDTO();
        requestDTO.setIdEmpleado(idEmpleado);
        requestDTO.setIdZona(idZona);
        requestDTO.setCantVehiculosACargo(cantVehiculos);

        return crearAsignacion(requestDTO);
    }

    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponseDTO crearAsignacion(AsignacionEmpleadoZonaRequestDTO dto) {
        if (dto == null || dto.getIdEmpleado() == null || dto.getIdZona() == null) {
            throw new ErrorNegocio("El empleado y la zona son obligatorios.");
        }

        if (dto.getCantVehiculosACargo() == null || dto.getCantVehiculosACargo() < 0) {
            throw new ErrorNegocio("La cantidad de vehículos a cargo no puede ser negativa.");
        }

        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new RegistroNoEncontradoException("El empleado especificado con ID " + dto.getIdEmpleado() + " no existe."));

        Zona zona = zonaRepository.findById(dto.getIdZona())
                .orElseThrow(() -> new RegistroNoEncontradoException("La zona especificada con ID " + dto.getIdZona() + " no existe."));

        // 1. Validar si ya existe la asignación
        boolean yaAsignado = asignacionRepository.existsByEmpleadoIdAndZonaId(empleado.getId(), zona.getId());
        if (yaAsignado) {
            throw new ErrorNegocio("El empleado ya está asignado a la zona " + zona.getLetra() + ".");
        }

        // 2. Validar capacidad de vehículos de la zona
        Integer vehiculosActuales = asignacionRepository.sumVehiculosByZonaId(zona.getId());
        if (vehiculosActuales == null) {
            vehiculosActuales = 0;
        }

        if ((vehiculosActuales + dto.getCantVehiculosACargo()) > zona.getCapacidadVehiculos()) {
            throw new ZonaSinCapacidadException("La zona " + zona.getLetra() +
                    " no tiene capacidad suficiente para gestionar " + dto.getCantVehiculosACargo() + " vehículos más.");
        }

        // 3. Crear y guardar asignación
        AsignacionEmpleadoZona asignacion = new AsignacionEmpleadoZona();
        asignacion.setEmpleado(empleado);
        asignacion.setZona(zona);
        asignacion.setCantVehiculosACargo(dto.getCantVehiculosACargo());

        AsignacionEmpleadoZona guardada = asignacionRepository.save(asignacion);
        return mapearAResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> obtenerEmpleadosPorZona(Long idZona) {
        if (!zonaRepository.existsById(idZona)) {
            throw new RegistroNoEncontradoException("La zona con ID " + idZona + " no existe.");
        }

        return asignacionRepository.findEmpleadosByZonaId(idZona).stream()
                .map(this::mapearAEmpleadoResponseDTO)
                .collect(Collectors.toList());
    }

    // Auxiliares de mapeo interno a DTO de salida
    private AsignacionEmpleadoZonaResponseDTO mapearAResponseDTO(AsignacionEmpleadoZona asignacion) {
        AsignacionEmpleadoZonaResponseDTO dto = new AsignacionEmpleadoZonaResponseDTO();
        dto.setId(asignacion.getId());
        dto.setEmpleado(mapearAEmpleadoResponseDTO(asignacion.getEmpleado()));
        dto.setCantVehiculosACargo(asignacion.getCantVehiculosACargo());
        // El mapeo de ZonaResponseDTO asumimos que lo resuelve la capa DTO correspondiente
        return dto;
    }

    private EmpleadoResponseDTO mapearAEmpleadoResponseDTO(Empleado emp) {
        if (emp == null) return null;
        EmpleadoResponseDTO dto = new EmpleadoResponseDTO();
        dto.setId(emp.getId());
        dto.setNombre(emp.getNombre());
        dto.setApellido(emp.getApellido());
        dto.setNombreUsuario(emp.getNombreUsuario());
        return dto;
    }
}



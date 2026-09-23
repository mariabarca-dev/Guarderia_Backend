package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final SocioRepository socioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionVehiculoGarageRepository asignacionRepository;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository,
                               SocioRepository socioRepository,
                               EmpleadoRepository empleadoRepository,
                               AsignacionVehiculoGarageRepository asignacionRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.socioRepository = socioRepository;
        this.empleadoRepository = empleadoRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional
    public VehiculoResponseDTO registrarVehiculo(VehiculoRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El DTO del vehículo no puede ser nulo.");
        }

        String matriculaFormateada = dto.getMatricula().trim().toUpperCase();

        if (vehiculoRepository.existsByMatricula(matriculaFormateada)) {
            throw new MatriculaDuplicadaException("Error: Ya existe un vehículo con matrícula: " + matriculaFormateada);
        }

        Socio socio = socioRepository.findById(dto.getSocioId())
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe el socio con ID: " + dto.getSocioId()));

        Empleado empleado = null;
        if (dto.getEmpleadoId() != null && dto.getEmpleadoId() > 0) {
            empleado = empleadoRepository.findById(dto.getEmpleadoId())
                    .orElseThrow(() -> new RegistroNoEncontradoException("No existe el empleado con ID: " + dto.getEmpleadoId()));
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula(matriculaFormateada);
        vehiculo.setNombre(dto.getNombre() != null ? dto.getNombre().trim() : null);
        vehiculo.setSocio(socio);
        vehiculo.setEmpleadoResponsable(empleado);
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setProfundidad(dto.getProfundidad());
        vehiculo.setAncho(dto.getAncho());

        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorId(Long id) {
        Vehiculo v = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró vehículo con ID: " + id));
        return mapearAResponseDTO(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO buscarPorMatricula(String matricula) {
        Vehiculo v = vehiculoRepository.findByMatricula(matricula.trim().toUpperCase())
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró vehículo con matrícula: " + matricula));
        return mapearAResponseDTO(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarTodos() {
        return vehiculoRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarPorSocio(Long socioId) {
        return vehiculoRepository.findBySocioId(socioId).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> buscarPorTipo(TipoVehiculo tipo) {
        return vehiculoRepository.findByTipo(tipo).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculosPorResponsable(Long empleadoId) {
        return vehiculoRepository.findByEmpleadoResponsableId(empleadoId).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculosPorZona(Long zonaId) {
        return asignacionRepository.findByGarageZonaId(zonaId).stream()
                .map(asignacion -> mapearAResponseDTO(asignacion.getVehiculo()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoRequestDTO dto) {
        Vehiculo vehiculoExistente = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe vehículo con ID " + id));

        String matriculaFormateada = dto.getMatricula().trim().toUpperCase();

        vehiculoRepository.findByMatricula(matriculaFormateada).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new MatriculaDuplicadaException("Error: Ya existe otro vehículo con la matrícula: " + matriculaFormateada);
            }
        });

        Socio socio = socioRepository.findById(dto.getSocioId())
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe el socio con ID: " + dto.getSocioId()));

        Empleado empleado = null;
        if (dto.getEmpleadoId() != null && dto.getEmpleadoId() > 0) {
            empleado = empleadoRepository.findById(dto.getEmpleadoId())
                    .orElseThrow(() -> new RegistroNoEncontradoException("No existe el empleado con ID: " + dto.getEmpleadoId()));
        }

        vehiculoExistente.setMatricula(matriculaFormateada);
        vehiculoExistente.setNombre(dto.getNombre() != null ? dto.getNombre().trim() : null);
        vehiculoExistente.setSocio(socio);
        vehiculoExistente.setEmpleadoResponsable(empleado);
        vehiculoExistente.setTipo(dto.getTipo());
        vehiculoExistente.setProfundidad(dto.getProfundidad());
        vehiculoExistente.setAncho(dto.getAncho());

        Vehiculo actualizado = vehiculoRepository.save(vehiculoExistente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarVehiculo(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new RegistroNoEncontradoException("No existe vehículo con ID " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void eliminarPorMatricula(String matricula) {
        Vehiculo vehiculo = vehiculoRepository.findByMatricula(matricula.trim().toUpperCase())
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe vehículo con matrícula " + matricula));
        vehiculoRepository.delete(vehiculo);
    }

    private VehiculoResponseDTO mapearAResponseDTO(Vehiculo v) {
        VehiculoResponseDTO dto = new VehiculoResponseDTO();
        dto.setId(v.getId());
        dto.setMatricula(v.getMatricula());
        dto.setNombre(v.getNombre());
        dto.setTipo(v.getTipo());
        dto.setProfundidad(v.getProfundidad());
        dto.setAncho(v.getAncho());

        if (v.getSocio() != null) {
            dto.setSocioId(v.getSocio().getId());
            dto.setSocioNombre(v.getSocio().getNombre() + " " + v.getSocio().getApellido());
        }

        if (v.getEmpleadoResponsable() != null) {
            dto.setEmpleadoId(v.getEmpleadoResponsable().getId());
            dto.setEmpleadoNombre(v.getEmpleadoResponsable().getNombre() + " " + v.getEmpleadoResponsable().getApellido());
        }

        return dto;
    }
}

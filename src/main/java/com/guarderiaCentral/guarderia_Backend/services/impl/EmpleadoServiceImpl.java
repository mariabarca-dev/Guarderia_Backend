package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.EmpleadoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ErrorNegocio;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.models.Empleado;
import com.guarderiaCentral.guarderia_Backend.models.enums.Rol;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.EmpleadoRequestDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final AsignacionEmpleadoZonaRepository asignacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               AsignacionEmpleadoZonaRepository asignacionRepository,
                               VehiculoRepository vehiculoRepository,
                               PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.asignacionRepository = asignacionRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarTodos() {
        return empleadoRepository.findAll().stream()
                .filter(emp -> emp.getActivo() == null || emp.getActivo())
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO buscarPorId(Long id) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el empleado con ID: " + id));
        return mapearAResponseDTO(emp);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO registrarEmpleado(EmpleadoRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("El objeto empleado no puede ser nulo.");
        }

        // 1. Validar unicidad del código de empleado
        if (empleadoRepository.existsByCodigo(dto.getCodigo().trim())) {
            throw new CodigoEmpleadoDuplicadoException("Error: Ya existe un empleado registrado con el código: " + dto.getCodigo());
        }

        // 2. Validar unicidad de username
        if (empleadoRepository.existsByNombreUsuario(dto.getNombreUsuario().trim())) {
            throw new ErrorNegocio("El nombre de usuario '" + dto.getNombreUsuario() + "' ya existe.");
        }

        // 3. Crear y mapear entidad
        Empleado emp = new Empleado();
        emp.setNombre(dto.getNombre().trim());
        emp.setApellido(dto.getApellido().trim());
        emp.setDireccion(dto.getDireccion().trim());
        emp.setTelefono(dto.getTelefono().trim());
        emp.setNombreUsuario(dto.getNombreUsuario().trim());
        emp.setClave(passwordEncoder.encode(dto.getClave()));
        emp.setCodigo(dto.getCodigo().trim());
        emp.setEspecialidad(dto.getEspecialidad().trim());
        emp.setRol(Rol.EMPLEADO);
        emp.setActivo(true);

        Empleado guardado = empleadoRepository.save(emp);
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO dto) {
        Empleado empExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede actualizar: No se encontró un empleado con ID " + id));

        // Validar si intenta usar un código perteneciente a otro empleado
        if (!empExistente.getCodigo().equalsIgnoreCase(dto.getCodigo().trim()) &&
                empleadoRepository.existsByCodigo(dto.getCodigo().trim())) {
            throw new CodigoEmpleadoDuplicadoException("Error: Ya existe un empleado registrado con el código: " + dto.getCodigo());
        }

        empExistente.setNombre(dto.getNombre().trim());
        empExistente.setApellido(dto.getApellido().trim());
        empExistente.setDireccion(dto.getDireccion().trim());
        empExistente.setTelefono(dto.getTelefono().trim());
        empExistente.setNombreUsuario(dto.getNombreUsuario().trim());
        empExistente.setCodigo(dto.getCodigo().trim());
        empExistente.setEspecialidad(dto.getEspecialidad().trim());

        if (dto.getClave() != null && !dto.getClave().isBlank()) {
            empExistente.setClave(passwordEncoder.encode(dto.getClave()));
        }

        Empleado actualizado = empleadoRepository.save(empExistente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarEmpleado(Long id) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede eliminar: No se encontró un empleado con ID " + id));

        // Borrado lógico
        emp.setActivo(false);
        empleadoRepository.save(emp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponseDTO> listarZonasAsignadas(Long empleadoId, String usernameActual, boolean esAdmin) {
        Empleado emp = obtenerYValidarAcceso(empleadoId, usernameActual, esAdmin);

        // Consulta optimizada a nivel BD a través del repositorio de asignaciones
        return asignacionRepository.findZonasByEmpleadoId(emp.getId()).stream()
                .map(z -> {
                    ZonaResponseDTO dto = new ZonaResponseDTO();
                    dto.setId(z.getId());
                    dto.setLetra(z.getLetra());
                    dto.setCapacidadVehiculos(z.getCapacidadVehiculos());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculosBajoResponsabilidad(Long empleadoId, String usernameActual, boolean esAdmin) {
        Empleado emp = obtenerYValidarAcceso(empleadoId, usernameActual, esAdmin);

        // Consulta directa a la BD por ID o código de empleado
        return vehiculoRepository.findByResponsableId(emp.getId()).stream()
                .map(v -> {
                    VehiculoResponseDTO dto = new VehiculoResponseDTO();
                    dto.setId(v.getId());
                    dto.setMatricula(v.getMatricula());
                    dto.setTipo(v.getTipo());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Metodo auxiliar para verificación de existencia y control de privacidad (ownership)
    private Empleado obtenerYValidarAcceso(Long empleadoId, String usernameActual, boolean esAdmin) {
        Empleado emp = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el empleado con ID: " + empleadoId));

        if (!esAdmin && !emp.getNombreUsuario().equalsIgnoreCase(usernameActual)) {
            throw new AccessDeniedException("Acceso denegado: No tiene permisos para consultar información de otro empleado.");
        }

        return emp;
    }

    private EmpleadoResponseDTO mapearAResponseDTO(Empleado emp) {
        EmpleadoResponseDTO dto = new EmpleadoResponseDTO();
        dto.setId(emp.getId());
        dto.setNombre(emp.getNombre());
        dto.setApellido(emp.getApellido());
        dto.setDireccion(emp.getDireccion());
        dto.setTelefono(emp.getTelefono());
        dto.setNombreUsuario(emp.getNombreUsuario());
        dto.setCodigo(emp.getCodigo());
        dto.setEspecialidad(emp.getEspecialidad());
        dto.setRol(emp.getRol());
        return dto;
    }
}
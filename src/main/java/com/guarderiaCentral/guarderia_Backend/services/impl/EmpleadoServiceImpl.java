package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicio {@link EmpleadoService}.
 * Maneja la lógica de negocio de los empleados, validaciones de código y nombre de usuario,
 * guardado inteligente/reactivación, propagación en cascada de baja lógica sobre asignaciones y consultas asociadas.
 *
 * @author Cátedra Guardería Central
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionEmpleadoZonaRepository asignacionEmpleadoZonaRepository;
    private final GarageRepository garageRepository;
    private final AsignacionVehiculoGarageRepository asignacionVehiculoGarageRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        log.info("Iniciando creación de nuevo empleado con código: {} y nombreUsuario: {}", request.getCodigo(), request.getNombreUsuario());

        // 1. Validar unicidad global del nombreUsuario en toda la jerarquía de usuarios
        validarNombreUsuarioUnico(request.getNombreUsuario(), null);

        // 2. Verificar duplicados o presencia en inactivos por el código único de empleado
        Optional<Empleado> inactivoOpt = empleadoRepository.findAllIncludingInactive().stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().equalsIgnoreCase(request.getCodigo()))
                .findFirst();

        if (inactivoOpt.isPresent()) {
            Empleado existente = inactivoOpt.get();
            if (Boolean.TRUE.equals(existente.getActivo())) {
                throw new CodigoEmpleadoDuplicadoException("Ya existe un empleado activo con el código: " + request.getCodigo());
            }

            log.info("Reactivando empleado inactivo con ID: {}", existente.getId());

            EmpleadoUpdate update = new EmpleadoUpdate();
            update.setNombre(request.getNombre());
            update.setApellido(request.getApellido());
            update.setDireccion(request.getDireccion());
            update.setTelefono(request.getTelefono());
            update.setNombreUsuario(request.getNombreUsuario());
            if (request.getClave() != null && !request.getClave().isBlank()) {
                update.setClave(passwordEncoder.encode(request.getClave()));
            }
            update.setRol(request.getRol());
            update.setCodigo(request.getCodigo());
            update.setEspecialidad(request.getEspecialidad());

            empleadoRepository.updateEntity(existente, update);
            existente.setActivo(true);

            Empleado reactivado = empleadoRepository.save(existente);
            return empleadoRepository.fromEntity(reactivado);
        }

        // 3. Si no existe, se crea una nueva entidad
        Empleado empleado = empleadoRepository.toEntity(request);
        empleado.setClave(passwordEncoder.encode(request.getClave()));
        empleado.setActivo(true);

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado registrado exitosamente con ID: {}", guardado.getId());

        return empleadoRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse buscarPorId(Integer id) {
        log.info("Buscando empleado activo por ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);
        return empleadoRepository.fromEntity(empleado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodos() {
        log.info("Obteniendo listado de todos los empleados activos.");
        return empleadoRepository.findAllByActivoTrue().stream()
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodosIncluyendoInactivas() {
        log.info("Obteniendo listado completo de empleados (activos e inactivos).");
        return empleadoRepository.findAllIncludingInactive().stream()
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoUpdate update) {
        log.info("Actualizando información del empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        if (update.getCodigo() != null && !update.getCodigo().equalsIgnoreCase(empleado.getCodigo())) {
            validarCodigoUnico(update.getCodigo(), id);
        }

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equalsIgnoreCase(empleado.getNombreUsuario())) {
            validarNombreUsuarioUnico(update.getNombreUsuario(), id);
        }

        empleadoRepository.updateEntity(empleado, update);

        if (update.getClave() != null && !update.getClave().isBlank()) {
            empleado.setClave(passwordEncoder.encode(update.getClave()));
        }

        Empleado actualizado = empleadoRepository.save(empleado);
        log.info("Empleado con ID: {} actualizado correctamente.", actualizado.getId());

        return empleadoRepository.fromEntity(actualizado);
    }

    /**
     * {@inheritDoc}
     * Propaga la desactivación en cascada a sus asignaciones de zona (AsignacionEmpleadoZona).
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Ejecutando borrado lógico para empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        // Desactivación lógica del empleado
        empleado.setActivo(false);
        empleadoRepository.save(empleado);

        // Propagación en cascada: Desactivar asignaciones asociadas al empleado
        List<AsignacionEmpleadoZona> asignaciones = asignacionEmpleadoZonaRepository.findAll().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId().equals(id)
                        && Boolean.TRUE.equals(asig.getActivo()))
                .collect(Collectors.toList());

        for (AsignacionEmpleadoZona asig : asignaciones) {
            asig.setActivo(false);
            asignacionEmpleadoZonaRepository.save(asig);
            log.info("Propagación de borrado lógico a AsignacionEmpleadoZona ID: {}", asig.getId());
        }

        log.info("Borrado lógico completado para empleado ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarZonasAsignadas(int empleadoId) {
        log.info("Obteniendo zonas asignadas para el empleado ID: {}", empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);

        return asignacionEmpleadoZonaRepository.findAllByActivoTrue().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId().equals(empleadoId))
                .map(asignacionEmpleadoZonaRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosBajoResponsabilidad(int empleadoId) {
        log.info("Obteniendo vehículos bajo responsabilidad del empleado ID: {}", empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);

        // 1. Zonas asociadas a asignaciones de empleado activas
        List<Integer> zonasIds = asignacionEmpleadoZonaRepository.findAllByActivoTrue().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId().equals(empleadoId)
                        && asig.getZona() != null
                        && Boolean.TRUE.equals(asig.getZona().getActivo()))
                .map(asig -> asig.getZona().getId())
                .collect(Collectors.toList());

        if (zonasIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Garages activos de las zonas asignadas
        List<Garage> garagesEnZonas = garageRepository.findAll().stream()
                .filter(g -> g.getZona() != null
                        && zonasIds.contains(g.getZona().getId())
                        && Boolean.TRUE.equals(g.getActivo()))
                .collect(Collectors.toList());

        List<Integer> garageIds = garagesEnZonas.stream().map(Garage::getId).collect(Collectors.toList());

        if (garageIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Asignaciones Vehiculo-Garage activas asociadas a esos garages
        return asignacionVehiculoGarageRepository.findAllByActivoTrue().stream()
                .filter(asigVG -> asigVG.getGarage() != null
                        && garageIds.contains(asigVG.getGarage().getId())
                        && asigVG.getVehiculo() != null
                        && Boolean.TRUE.equals(asigVG.getVehiculo().getActivo()))
                .map(asigVG -> vehiculoRepository.fromEntity(asigVG.getVehiculo()))
                .collect(Collectors.toList());
    }

    // --- Métodos Privados Auxiliares ---

    private Empleado obtenerEmpleadoActivoPorId(Integer id) {
        return empleadoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un empleado activo con el ID: " + id));
    }

    private void validarCodigoUnico(String codigo, Integer idExcluir) {
        boolean existe = empleadoRepository.findAllIncludingInactive().stream()
                .anyMatch(e -> e.getCodigo() != null
                        && e.getCodigo().equalsIgnoreCase(codigo)
                        && Boolean.TRUE.equals(e.getActivo())
                        && (idExcluir == null || !e.getId().equals(idExcluir)));
        if (existe) {
            throw new CodigoEmpleadoDuplicadoException("Ya existe un empleado registrado con el código: " + codigo);
        }
    }

    private void validarNombreUsuarioUnico(String nombreUsuario, Integer idExcluir) {
        boolean existe = usuarioRepository.findAllIncludingInactive().stream()
                .anyMatch(u -> u.getNombreUsuario() != null
                        && u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)
                        && (idExcluir == null || !u.getId().equals(idExcluir)));
        if (existe) {
            throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + nombreUsuario + "' ya se encuentra registrado.");
        }
    }
}
package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicio {@link EmpleadoService}.
 * Maneja la lógica de negocio de los empleados, validaciones de código/DNI,
 * propagación en cascada de baja lógica sobre asignaciones y consultas asociadas.
 */
@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    private final EmpleadoRepository empleadoRepository;
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
    public EmpleadoResponse registrarEmpleado(EmpleadoRequest request) {
        log.info("Iniciando registro de nuevo empleado con código: {} y DNI: {}", request.getCodigo(), request.getDni());

        validarCodigoUnico(request.getCodigo(), null);
        validarDniUnico(request.getDni(), null);

        Empleado empleado = empleadoRepository.toEntity(request);
        empleado.setPassword(passwordEncoder.encode(request.getPassword()));
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
    public List<EmpleadoResponse> listarTodos() {
        log.info("Obteniendo listado de todos los empleados activos.");
        return empleadoRepository.findAll().stream()
                .filter(emp -> Boolean.TRUE.equals(emp.getActivo()))
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodosIncluyendoInactivos() {
        log.info("Obteniendo listado completo de empleados (activos e inactivos).");
        return empleadoRepository.findAllIncludingInactive().stream()
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse buscarEmpleadoPorId(Integer id) {
        log.info("Buscando empleado activo por ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);
        return empleadoRepository.fromEntity(empleado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmpleadoResponse actualizarEmpleado(Integer id, EmpleadoUpdate update) {
        log.info("Actualizando información del empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        if (update.getCodigo() != null && !update.getCodigo().equals(empleado.getCodigo())) {
            validarCodigoUnico(update.getCodigo(), id);
        }

        if (update.getDni() != null && !update.getDni().equals(empleado.getDni())) {
            validarDniUnico(update.getDni(), id);
        }

        empleadoRepository.updateEntity(empleado, update);

        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            empleado.setPassword(passwordEncoder.encode(update.getPassword()));
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
    public void eliminarEmpleado(Integer id) {
        log.info("Ejecutando borrado lógico para empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        // Desactivación lógica del empleado
        empleado.setActivo(false);
        empleadoRepository.save(empleado);

        // Propagación en cascada: Desactivar asignaciones asociadas al empleado
        List<AsignacionEmpleadoZona> asignaciones = asignacionEmpleadoZonaRepository.findAll().stream()
                .filter(asig -> asig.getEmpleado() != null && asig.getEmpleado().getId() == id && Boolean.TRUE.equals(asig.getActivo()))
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

        return asignacionEmpleadoZonaRepository.findAll().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId() == empleadoId
                        && Boolean.TRUE.equals(asig.getActivo()))
                .map(asignacionEmpleadoZonaRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * De acuerdo con la regla de negocio del dominio: Un empleado asignado a una zona tiene
     * a su cargo la cantidad indicada en {@code cantVehiculosACargo}. Se retornan los vehículos
     * estacionados en los garages pertenecientes a las zonas donde el empleado tiene asignación activa.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosBajoResponsabilidad(int empleadoId) {
        log.info("Obteniendo vehículos bajo responsabilidad del empleado ID: {}", empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);

        // 1. Zonas asociadas a asignaciones de empleado activas
        List<Integer> zonasIds = asignacionEmpleadoZonaRepository.findAll().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId() == empleadoId
                        && Boolean.TRUE.equals(asig.getActivo())
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
        return asignacionVehiculoGarageRepository.findAll().stream()
                .filter(asigVG -> asigVG.getGarage() != null
                        && garageIds.contains(asigVG.getGarage().getId())
                        && Boolean.TRUE.equals(asigVG.getActivo())
                        && asigVG.getVehiculo() != null
                        && Boolean.TRUE.equals(asigVG.getVehiculo().getActivo()))
                .map(asigVG -> vehiculoRepository.fromEntity(asigVG.getVehiculo()))
                .collect(Collectors.toList());
    }

    // --- Métodos Privados Auxiliares ---

    private Empleado obtenerEmpleadoActivoPorId(Integer id) {
        return empleadoRepository.findById(id)
                .filter(emp -> Boolean.TRUE.equals(emp.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un empleado activo con el ID: " + id));
    }

    private void validarCodigoUnico(String codigo, Integer idExcluir) {
        boolean existe = empleadoRepository.findAllIncludingInactive().stream()
                .anyMatch(e -> e.getCodigo().equalsIgnoreCase(codigo)
                        && (idExcluir == null || e.getId() != idExcluir));
        if (existe) {
            throw new CodigoEmpleadoDuplicadoException("Ya existe un empleado registrado con el código: " + codigo);
        }
    }

    private void validarDniUnico(String dni, Integer idExcluir) {
        boolean existe = empleadoRepository.findAllIncludingInactive().stream()
                .anyMatch(e -> e.getDni().equalsIgnoreCase(dni)
                        && (idExcluir == null || e.getId() != idExcluir));
        if (existe) {
            throw new DniDuplicadoException("Ya existe un usuario registrado con el DNI: " + dni);
        }
    }
}
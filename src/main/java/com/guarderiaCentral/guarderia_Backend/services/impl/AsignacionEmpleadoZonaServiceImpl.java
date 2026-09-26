package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionEmpleadoZonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de Asignación de Empleados a Zonas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionEmpleadoZonaServiceImpl implements AsignacionEmpleadoZonaService {

    private final AsignacionEmpleadoZonaRepository asignacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ZonaRepository zonaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponse crearAsignacion(AsignacionEmpleadoZonaRequest request) {
        log.info("Iniciando creación de asignación para empleado ID: {} en zona ID: {}",
                request.getIdEmpleado(), request.getIdZona());

        Empleado empleado = empleadoRepository.findById(request.getIdEmpleado())
                .filter(Empleado::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El empleado especificado no existe o está inactivo."));

        Zona zona = zonaRepository.findById(request.getIdZona())
                .filter(Zona::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("La zona especificada no existe o está inactiva."));

        if (request.getCantVehiculosACargo() < 0) {
            throw new IllegalArgumentException("La cantidad de vehículos a cargo no puede ser negativa.");
        }

        // Validar si ya existe una asignación activa para este empleado en esta zona
        boolean yaAsignado = asignacionRepository.findAll().stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getEmpleado() != null && a.getEmpleado().getId().equals(empleado.getId())
                        && a.getZona() != null && a.getZona().getId().equals(zona.getId()));

        if (yaAsignado) {
            throw new IllegalArgumentException("El empleado ya se encuentra asignado a la zona seleccionada.");
        }

        // Calcular vehículos actuales gestionados en la zona por las asignaciones activas
        int vehiculosActuales = asignacionRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getZona() != null
                        && a.getZona().getId().equals(zona.getId()))
                .mapToInt(AsignacionEmpleadoZona::getCantVehiculosACargo)
                .sum();

        if ((vehiculosActuales + request.getCantVehiculosACargo()) > zona.getCapacidadVehiculos()) {
            log.warn("Capacidad excedida para la zona ID: {}. Capacidad máxima: {}, Ocupados/Asignados: {}, Intentando agregar: {}",
                    zona.getId(), zona.getCapacidadVehiculos(), vehiculosActuales, request.getCantVehiculosACargo());
            throw new ZonaSinCapacidadException("La zona " + zona.getLetra() +
                    " no tiene capacidad suficiente para gestionar " + request.getCantVehiculosACargo() + " vehículos más.");
        }

        AsignacionEmpleadoZona asignacion = request.toEntity(empleado, zona);
        AsignacionEmpleadoZona guardada = asignacionRepository.save(asignacion);

        log.info("Asignación creada exitosamente con ID: {}", guardada.getId());
        return AsignacionEmpleadoZonaResponse.fromEntity(guardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarTodas() {
        log.info("Listando todas las asignaciones de empleado a zona activas");
        return asignacionRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .map(AsignacionEmpleadoZonaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarTodasIncluyendoInactivas() {
        log.info("Listando todas las asignaciones (incluyendo inactivas)");
        return asignacionRepository.findAll().stream()
                .map(AsignacionEmpleadoZonaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AsignacionEmpleadoZonaResponse buscarPorId(Integer id) {
        log.info("Buscando asignación por ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));
        return AsignacionEmpleadoZonaResponse.fromEntity(asignacion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponse actualizarAsignacion(Integer id, AsignacionEmpleadoZonaUpdate update) {
        log.info("Actualizando asignación con ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));

        if (update.getCantVehiculosACargo() != null) {
            if (update.getCantVehiculosACargo() < 0) {
                throw new IllegalArgumentException("La cantidad de vehículos a cargo no puede ser negativa.");
            }

            // Validar capacidad considerando el cambio
            Zona zona = asignacion.getZona();
            int vehiculosActualesOtros = asignacionRepository.findAll().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getActivo())
                            && a.getZona() != null
                            && a.getZona().getId().equals(zona.getId())
                            && !a.getId().equals(asignacion.getId()))
                    .mapToInt(AsignacionEmpleadoZona::getCantVehiculosACargo)
                    .sum();

            if ((vehiculosActualesOtros + update.getCantVehiculosACargo()) > zona.getCapacidadVehiculos()) {
                throw new ZonaSinCapacidadException("La zona " + zona.getLetra() +
                        " no tiene capacidad suficiente para actualizar a " + update.getCantVehiculosACargo() + " vehículos.");
            }
        }

        update.updateEntity(asignacion);
        AsignacionEmpleadoZona actualizada = asignacionRepository.save(asignacion);

        log.info("Asignación con ID: {} actualizada exitosamente", actualizada.getId());
        return AsignacionEmpleadoZonaResponse.fromEntity(actualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarAsignacion(Integer id) {
        log.info("Ejecutando borrado lógico para asignación con ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));

        asignacion.setActivo(false);
        asignacionRepository.save(asignacion);
        log.info("Borrado lógico aplicado exitosamente a la asignación con ID: {}", id);
    }
}
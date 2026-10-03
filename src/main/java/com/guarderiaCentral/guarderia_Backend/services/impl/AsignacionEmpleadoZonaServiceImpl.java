package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionEmpleadoZonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la entidad {@link AsignacionEmpleadoZona}.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionEmpleadoZonaServiceImpl implements AsignacionEmpleadoZonaService {

    private final AsignacionEmpleadoZonaRepository asignacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ZonaRepository zonaRepository;

    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponse crearAsignacion(AsignacionEmpleadoZonaRequest request) {
        log.info("Iniciando creación de asignación para empleado ID: {} en zona ID: {}",
                request.getEmpleadoId(), request.getZonaId());

        Empleado empleado = empleadoRepository.findByIdAndActivoTrue(request.getEmpleadoId())
                .orElseThrow(() -> new RegistroNoEncontradoException("El empleado con ID " + request.getEmpleadoId() + " no existe o está inactivo."));

        Zona zona = zonaRepository.findByIdAndActivoTrue(request.getZonaId())
                .orElseThrow(() -> new RegistroNoEncontradoException("La zona con ID " + request.getZonaId() + " no existe o está inactiva."));

        if (request.getCantVehiculosACargo() < 0) {
            throw new IllegalArgumentException("La cantidad de vehículos a cargo no puede ser negativa.");
        }

        // Validar si ya existe una asignación activa para este empleado en esta zona
        boolean yaAsignado = asignacionRepository.findAllByActivoTrue().stream()
                .anyMatch(a -> a.getEmpleado() != null && a.getEmpleado().getId().equals(empleado.getId())
                        && a.getZona() != null && a.getZona().getId().equals(zona.getId()));

        if (yaAsignado) {
            throw new IllegalArgumentException("El empleado ya se encuentra asignado a la zona seleccionada.");
        }

        // Validar capacidad de la zona considerando las asignaciones activas actuales
        int vehiculosActuales = asignacionRepository.findAllByZonaIdAndActivoTrue(zona.getId()).stream()
                .mapToInt(AsignacionEmpleadoZona::getCantVehiculosACargo)
                .sum();

        if ((vehiculosActuales + request.getCantVehiculosACargo()) > zona.getCapacidadVehiculos()) {
            log.warn("Capacidad excedida para la zona ID: {}. Capacidad máxima: {}, Ocupados: {}, Intentando agregar: {}",
                    zona.getId(), zona.getCapacidadVehiculos(), vehiculosActuales, request.getCantVehiculosACargo());
            throw new ZonaSinCapacidadException("La zona " + zona.getLetra() +
                    " no tiene capacidad suficiente para gestionar " + request.getCantVehiculosACargo() + " vehículos más.");
        }

        AsignacionEmpleadoZona asignacion = asignacionRepository.toEntity(request, empleado, zona);
        AsignacionEmpleadoZona guardada = asignacionRepository.save(asignacion);

        log.info("Asignación creada exitosamente con ID: {}", guardada.getId());
        return asignacionRepository.fromEntity(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarTodas() {
        log.debug("Listando todas las asignaciones de empleado a zona activas");
        return asignacionRepository.findAllByActivoTrue().stream()
                .map(asignacionRepository::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarTodasIncluyendoInactivas() {
        log.info("Listando todas las asignaciones (incluyendo inactivas) por solicitud administrativa.");
        return asignacionRepository.findAllIncludingInactive().stream()
                .map(asignacionRepository::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionEmpleadoZonaResponse buscarPorId(Integer id) {
        log.debug("Buscando asignación por ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));
        return asignacionRepository.fromEntity(asignacion);
    }

    @Override
    @Transactional
    public AsignacionEmpleadoZonaResponse actualizarAsignacion(Integer id, AsignacionEmpleadoZonaUpdate update) {
        log.info("Iniciando actualización de asignación con ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));

        if (update.getCantVehiculosACargo() != null) {
            if (update.getCantVehiculosACargo() < 0) {
                throw new IllegalArgumentException("La cantidad de vehículos a cargo no puede ser negativa.");
            }

            Zona zona = asignacion.getZona();
            int vehiculosActualesOtros = asignacionRepository.findAllByZonaIdAndActivoTrue(zona.getId()).stream()
                    .filter(a -> !a.getId().equals(asignacion.getId()))
                    .mapToInt(AsignacionEmpleadoZona::getCantVehiculosACargo)
                    .sum();

            if ((vehiculosActualesOtros + update.getCantVehiculosACargo()) > zona.getCapacidadVehiculos()) {
                throw new ZonaSinCapacidadException("La zona " + zona.getLetra() +
                        " no tiene capacidad suficiente para actualizar a " + update.getCantVehiculosACargo() + " vehículos.");
            }
        }

        Empleado empleadoNuevo = (update.getEmpleadoId() != null)
                ? empleadoRepository.findByIdAndActivoTrue(update.getEmpleadoId())
                .orElseThrow(() -> new RegistroNoEncontradoException("Empleado nuevo no encontrado o inactivo."))
                : null;

        Zona zonaNueva = (update.getZonaId() != null)
                ? zonaRepository.findByIdAndActivoTrue(update.getZonaId())
                .orElseThrow(() -> new RegistroNoEncontradoException("Zona nueva no encontrada o inactiva."))
                : null;

        asignacionRepository.updateEntity(asignacion, update, empleadoNuevo, zonaNueva);
        AsignacionEmpleadoZona actualizada = asignacionRepository.save(asignacion);

        log.info("Asignación con ID: {} actualizada exitosamente", actualizada.getId());
        return asignacionRepository.fromEntity(actualizada);
    }

    @Override
    @Transactional
    public void eliminarAsignacion(Integer id) {
        log.info("Ejecutando borrado lógico para asignación con ID: {}", id);
        AsignacionEmpleadoZona asignacion = asignacionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("La asignación con ID " + id + " no fue encontrada."));

        asignacion.setActivo(false);
        asignacionRepository.save(asignacion);
        log.info("Borrado lógico aplicado exitosamente a la asignación con ID: {}", id);
    }
}
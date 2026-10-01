package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NumeroGarageDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadesGarage.PropiedadGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaResponse; // Usando ZonaResponse
import com.guarderiaCentral.guarderia_Backend.services.GarageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicio {@link GarageService}.
 * Administra las reglas de negocio, validaciones de capacidad de zona,
 * unicidad de número de garage mediante guardado inteligente, propagación de borrado lógico
 * y reporte de disponibilidad basado en ZonaResponse.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;
    private final ZonaRepository zonaRepository;
    private final AsignacionVehiculoGarageRepository asignacionVehiculoGarageRepository;
    private final PropiedadGarageRepository propiedadGarageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GarageResponse crear(GarageRequest request) {
        log.info("Iniciando registro de nuevo garage número: {} en la zona ID: {}", request.getNumeroGarage(), request.getZonaId());

        Zona zona = zonaRepository.findById(request.getZonaId())
                .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe una zona activa con el ID: " + request.getZonaId()));

        long garajesEnZona = garageRepository.findAll().stream()
                .filter(g -> g.getZona() != null
                        && g.getZona().getId().equals(zona.getId())
                        && Boolean.TRUE.equals(g.getActivo()))
                .count();

        if (garajesEnZona >= zona.getCapacidadVehiculos()) {
            log.error("Capacidad máxima superada para la zona ID: {}. Capacidad: {}, Registrados: {}",
                    zona.getId(), zona.getCapacidadVehiculos(), garajesEnZona);
            throw new ZonaSinCapacidadException("La zona '" + zona.getLetra() + "' alcanzó su capacidad máxima de "
                    + zona.getCapacidadVehiculos() + " garajes.");
        }

        Optional<Garage> garageExistenteOpt = garageRepository.findByNumeroGarageIncludingInactive(request.getNumeroGarage());
        Garage garage;

        if (garageExistenteOpt.isPresent()) {
            garage = garageExistenteOpt.get();
            if (Boolean.TRUE.equals(garage.getActivo())) {
                throw new NumeroGarageDuplicadoException("Ya existe un garaje activo con el número: " + request.getNumeroGarage());
            }
            log.info("Reactiva garage inactivo con ID: {} y número: {}", garage.getId(), request.getNumeroGarage());
            garage.setLecturaLuz(request.getLecturaLuz());
            garage.setServicioMantenimiento(request.getServicioMantenimiento());
            garage.setZona(zona);
            garage.setActivo(true);
        } else {
            garage = garageRepository.toEntity(request);
            garage.setZona(zona);
            garage.setActivo(true);
        }

        Garage guardado = garageRepository.save(garage);
        log.info("Garage registrado/reactivado con éxito ID: {}", guardado.getId());

        return garageRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> listarTodos() {
        log.info("Consultando listado de garages activos.");
        return garageRepository.findAll().stream()
                .filter(g -> Boolean.TRUE.equals(g.getActivo()))
                .map(garageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> listarTodosIncluyendoInactivos() {
        log.info("Consultando listado completo de garages (incluyendo inactivos).");
        return garageRepository.findAllIncludingInactive().stream()
                .map(garageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public GarageResponse buscarPorId(Integer id) {
        log.info("Buscando garage activo con ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);
        return garageRepository.fromEntity(garage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GarageResponse actualizar(Integer id, GarageUpdate update) {
        log.info("Iniciando actualización del garage ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);

        if (update.getNumeroGarage() != null && !update.getNumeroGarage().equals(garage.getNumeroGarage())) {
            Optional<Garage> existente = garageRepository.findByNumeroGarageIncludingInactive(update.getNumeroGarage());
            if (existente.isPresent() && Boolean.TRUE.equals(existente.get().getActivo()) && !existente.get().getId().equals(id)) {
                throw new NumeroGarageDuplicadoException("Ya existe un garaje activo con el número: " + update.getNumeroGarage());
            }
        }

        if (update.getZonaId() != null && (garage.getZona() == null || !garage.getZona().getId().equals(update.getZonaId()))) {
            Zona nuevaZona = zonaRepository.findById(update.getZonaId())
                    .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                    .orElseThrow(() -> new RegistroNoEncontradoException("No existe una zona activa con el ID: " + update.getZonaId()));

            long garajesEnNuevaZona = garageRepository.findAll().stream()
                    .filter(g -> g.getZona() != null
                            && g.getZona().getId().equals(nuevaZona.getId())
                            && Boolean.TRUE.equals(g.getActivo())
                            && !g.getId().equals(id))
                    .count();

            if (garajesEnNuevaZona >= nuevaZona.getCapacidadVehiculos()) {
                throw new ZonaSinCapacidadException("La nueva zona '" + nuevaZona.getLetra() + "' alcanzó su capacidad máxima de "
                        + nuevaZona.getCapacidadVehiculos() + " garajes.");
            }
            garage.setZona(nuevaZona);
        }

        garageRepository.updateEntity(garage, update);
        Garage actualizado = garageRepository.save(garage);
        log.info("Garage ID: {} actualizado correctamente.", actualizado.getId());

        return garageRepository.fromEntity(actualizado);
    }

    /**
     * {@inheritDoc}
     * Valida que el garage no tenga dependencias activas (vehículo asignado o propiedad vigente)
     * antes de realizar el borrado lógico y propagar en cascada la desactivación de sus asignaciones.
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Iniciando validación para borrado lógico del garage ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);

        boolean tieneAsignacionActiva = asignacionVehiculoGarageRepository.findAll().stream()
                .anyMatch(a -> a.getGarage() != null
                        && a.getGarage().getId().equals(id)
                        && Boolean.TRUE.equals(a.getActivo()));

        if (tieneAsignacionActiva) {
            throw new DependenciasActivasException("No se puede eliminar el garage ID: " + id + " porque tiene un vehículo asignado activamente.");
        }

        boolean tienePropiedadVigente = propiedadGarageRepository.findAll().stream()
                .anyMatch(p -> p.getGarage() != null
                        && p.getGarage().getId().equals(id)
                        && Boolean.TRUE.equals(p.getActivo()));

        if (tienePropiedadVigente) {
            throw new DependenciasActivasException("No se puede eliminar el garage ID: " + id + " porque tiene una propiedad vigente.");
        }

        garage.setActivo(false);
        garageRepository.save(garage);

        List<AsignacionVehiculoGarage> asignaciones = asignacionVehiculoGarageRepository.findAll().stream()
                .filter(a -> a.getGarage() != null
                        && a.getGarage().getId().equals(id)
                        && Boolean.TRUE.equals(a.getActivo()))
                .collect(Collectors.toList());

        for (AsignacionVehiculoGarage asignacion : asignaciones) {
            asignacion.setActivo(false);
            asignacionVehiculoGarageRepository.save(asignacion);
            log.info("Propagación de borrado lógico a AsignacionVehiculoGarage ID: {}", asignacion.getId());
        }

        log.info("Borrado lógico finalizado para el garage ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponse> consultarDisponibilidadGarages() {
        log.info("Generando listado de zonas activas para consulta de disponibilidad.");

        List<Zona> zonasActivas = zonaRepository.findAll().stream()
                .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                .collect(Collectors.toList());

        List<AsignacionVehiculoGarage> asignacionesActivas = asignacionVehiculoGarageRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getVehiculo() != null
                        && Boolean.TRUE.equals(a.getVehiculo().getActivo()))
                .collect(Collectors.toList());

        return zonasActivas.stream().map(z -> {
            long ocupadosReales = asignacionesActivas.stream()
                    .filter(a -> a.getGarage() != null
                            && a.getGarage().getZona() != null
                            && a.getGarage().getZona().getId().equals(z.getId())
                            && Boolean.TRUE.equals(a.getGarage().getActivo()))
                    .count();

            int disponibles = z.getCapacidadVehiculos() - (int) ocupadosReales;
            if (disponibles < 0) {
                disponibles = 0;
            }

            // Mapeo utilizando ZonaResponse (asegúrate de que ZonaResponse tenga setters para la capacidad o los datos calculados si los requiere tu capa web)
            ZonaResponse dto = zonaRepository.fromEntity(z);
            // Si ZonaResponse incluye campos para calcular disponibilidad, puedes asignarlos aquí.
            return dto;
        }).collect(Collectors.toList());
    }

    // --- Métodos Privados Auxiliares ---

    private Garage obtenerGarageActivoPorId(Integer id) {
        return garageRepository.findById(id)
                .filter(g -> Boolean.TRUE.equals(g.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un garage activo con el ID: " + id));
    }
}
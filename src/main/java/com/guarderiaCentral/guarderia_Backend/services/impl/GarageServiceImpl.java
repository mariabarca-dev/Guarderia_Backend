package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.services.GarageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicio {@link GarageService}.
 * Administra las reglas de negocio, validaciones de capacidad de zona,
 * unicidad de número de garage, propagación de borrado lógico y reporte de ocupación.
 */
@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private static final Logger log = LoggerFactory.getLogger(GarageServiceImpl.class);

    private final GarageRepository garageRepository;
    private final ZonaRepository zonaRepository;
    private final AsignacionVehiculoGarageRepository asignacionVehiculoGarageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GarageResponse registrarGarage(GarageRequest request) {
        log.info("Iniciando registro de nuevo garage número: {} en la zona ID: {}", request.getNumeroGarage(), request.getZonaId());

        Zona zona = zonaRepository.findById(request.getZonaId())
                .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe una zona activa con el ID: " + request.getZonaId()));

        long garajesEnZona = garageRepository.findAll().stream()
                .filter(g -> g.getZona() != null
                        && g.getZona().getId() == zona.getId()
                        && Boolean.TRUE.equals(g.getActivo()))
                .count();

        if (garajesEnZona >= zona.getCapacidadVehiculos()) {
            log.error("Capacidad máxima superada para la zona ID: {}. Capacidad: {}, Registrados: {}",
                    zona.getId(), zona.getCapacidadVehiculos(), garajesEnZona);
            throw new ZonaSinCapacidadException("La zona '" + zona.getLetra() + "' alcanzó su capacidad máxima de "
                    + zona.getCapacidadVehiculos() + " garajes.");
        }

        validarNumeroGarageUnico(request.getNumeroGarage(), null);

        Garage garage = garageRepository.toEntity(request);
        garage.setZona(zona);
        garage.setActivo(true);

        Garage guardado = garageRepository.save(garage);
        log.info("Garage registrado con éxito ID: {}", guardado.getId());

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
    public GarageResponse buscarPorId(int id) {
        log.info("Buscando garage activo con ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);
        return garageRepository.fromEntity(garage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GarageResponse actualizarGarage(int id, GarageUpdate update) {
        log.info("Iniciando actualización del garage ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);

        if (update.getNumeroGarage() != null && update.getNumeroGarage() != garage.getNumeroGarage()) {
            validarNumeroGarageUnico(update.getNumeroGarage(), id);
        }

        if (update.getZonaId() != null && (garage.getZona() == null || garage.getZona().getId() != update.getZonaId())) {
            Zona nuevaZona = zonaRepository.findById(update.getZonaId())
                    .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                    .orElseThrow(() -> new RegistroNoEncontradoException("No existe una zona activa con el ID: " + update.getZonaId()));

            long garajesEnNuevaZona = garageRepository.findAll().stream()
                    .filter(g -> g.getZona() != null
                            && g.getZona().getId() == nuevaZona.getId()
                            && Boolean.TRUE.equals(g.getActivo()))
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
     * Aplica borrado lógico (activo = false) y propaga en cascada la desactivación
     * a las asignaciones de vehículos en este garage.
     */
    @Override
    @Transactional
    public void eliminarGarage(int id) {
        log.info("Iniciando borrado lógico para el garage ID: {}", id);
        Garage garage = obtenerGarageActivoPorId(id);

        garage.setActivo(false);
        garageRepository.save(garage);

        List<AsignacionVehiculoGarage> asignaciones = asignacionVehiculoGarageRepository.findAll().stream()
                .filter(a -> a.getGarage() != null
                        && a.getGarage().getId() == id
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
    public List<String> consultarDisponibilidadGarages() {
        log.info("Generando informe de disponibilidad de garages por zona.");
        List<String> reporte = new ArrayList<>();

        List<Zona> zonasActivas = zonaRepository.findAll().stream()
                .filter(z -> Boolean.TRUE.equals(z.getActivo()))
                .collect(Collectors.toList());

        List<AsignacionVehiculoGarage> asignacionesActivas = asignacionVehiculoGarageRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getVehiculo() != null
                        && Boolean.TRUE.equals(a.getVehiculo().getActivo()))
                .collect(Collectors.toList());

        for (Zona z : zonasActivas) {
            long ocupadosReales = asignacionesActivas.stream()
                    .filter(a -> a.getGarage() != null
                            && a.getGarage().getZona() != null
                            && a.getGarage().getZona().getId() == z.getId()
                            && Boolean.TRUE.equals(a.getGarage().getActivo()))
                    .count();

            int disponibles = z.getCapacidadVehiculos() - (int) ocupadosReales;
            if (disponibles < 0) {
                disponibles = 0;
            }

            reporte.add("Zona " + z.getLetra() + " (" + z.getTipoVehiculo() + "): "
                    + disponibles + " disponibles de " + z.getCapacidadVehiculos() + " totales (Ocupados: " + ocupadosReales + ").");
        }

        return reporte;
    }

    // --- Métodos Privados Auxiliares ---

    private Garage obtenerGarageActivoPorId(int id) {
        return garageRepository.findById(id)
                .filter(g -> Boolean.TRUE.equals(g.getActivo()))
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un garage activo con el ID: " + id));
    }

    private void validarNumeroGarageUnico(int numeroGarage, Integer idExcluir) {
        boolean existe = garageRepository.findAllIncludingInactive().stream()
                .filter(g -> Boolean.TRUE.equals(g.getActivo()))
                .anyMatch(g -> g.getNumeroGarage() == numeroGarage
                        && (idExcluir == null || g.getId() != idExcluir));

        if (existe) {
            throw new BusinessException("Ya existe un garaje registrado con el número: " + numeroGarage, HttpStatus.BAD_REQUEST);
        }
    }
}
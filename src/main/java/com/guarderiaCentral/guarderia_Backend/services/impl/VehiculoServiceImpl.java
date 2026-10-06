package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoUpdate;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la entidad {@link Vehiculo}.
 * Maneja el guardado inteligente por matrícula (alta o reactivación), la validación de socios,
 * la baja lógica con cascada sobre la asignación de garage y las consultas filtradas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final SocioRepository socioRepository;
    private final AsignacionVehiculoGarageRepository asignacionVehiculoGarageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        log.info("Iniciando alta de vehículo con matrícula: {}", request.getMatricula());

        Optional<Vehiculo> existenteOpt = vehiculoRepository.findByMatriculaIncludingInactive(request.getMatricula());
        if (existenteOpt.isPresent() && Boolean.TRUE.equals(existenteOpt.get().getActivo())) {
            log.error("Error al registrar vehículo: matrícula {} duplicada.", request.getMatricula());
            throw new MatriculaDuplicadaException("Ya existe un vehículo registrado con la matrícula: " + request.getMatricula());
        }

        Socio socio = obtenerSocioActivo(request.getSocioId());

        Vehiculo vehiculo;
        if (existenteOpt.isPresent()) {
            vehiculo = existenteOpt.get();
            log.info("Reactivando vehículo inactivo ID {} con matrícula {}", vehiculo.getId(), request.getMatricula());
            vehiculo.setNombre(request.getNombre());
            vehiculo.setTipo(request.getTipo());
            vehiculo.setProfundidad(request.getProfundidad());
            vehiculo.setAncho(request.getAncho());
            vehiculo.setSocio(socio);
            vehiculo.setActivo(true);
        } else {
            vehiculo = vehiculoRepository.toEntity(request);
            vehiculo.setSocio(socio);
        }

        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo guardado exitosamente con ID: {}", guardado.getId());
        return vehiculoRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse buscarPorId(Integer id) {
        log.debug("Buscando vehículo por ID: {}", id);
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se encontró vehículo activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró vehículo con ID: " + id);
                });
        return vehiculoRepository.fromEntity(vehiculo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse buscarPorMatricula(String matricula) {
        log.debug("Buscando vehículo por matrícula: {}", matricula);
        Vehiculo vehiculo = vehiculoRepository.findByMatriculaAndActivoTrue(matricula)
                .orElseThrow(() -> {
                    log.error("No se encontró vehículo activo con matrícula: {}", matricula);
                    return new RegistroNoEncontradoException("No se encontró vehículo con matrícula: " + matricula);
                });
        return vehiculoRepository.fromEntity(vehiculo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarTodos() {
        log.debug("Listando todos los vehículos activos");
        return vehiculoRepository.findAllByActivoTrue().stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarTodosIncluyendoInactivos() {
        log.info("Listando todos los vehículos (incluyendo inactivos) por solicitud administrativa.");
        return vehiculoRepository.findAllIncludingInactive().stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarPorSocio(Integer socioId) {
        log.debug("Listando vehículos para socio ID: {}", socioId);
        obtenerSocioActivo(socioId);

        return vehiculoRepository.findAllBySocioIdAndActivoTrue(socioId).stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> buscarPorTipo(TipoVehiculo tipo) {
        log.debug("Filtrando vehículos por tipo: {}", tipo);
        return vehiculoRepository.findAllByTipoAndActivoTrue(tipo).stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public VehiculoResponse actualizar(Integer id, VehiculoUpdate update) {
        log.info("Iniciando actualización de vehículo ID: {}", id);

        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Vehículo ID {} no encontrado para actualizar.", id);
                    return new RegistroNoEncontradoException("No existe vehículo con ID " + id);
                });

        if (update.getMatricula() != null && !update.getMatricula().equals(vehiculo.getMatricula())) {
            vehiculoRepository.findByMatriculaIncludingInactive(update.getMatricula())
                    .ifPresent(otro -> {
                        log.error("La matrícula {} ya pertenece a otro vehículo (ID: {})", update.getMatricula(), otro.getId());
                        throw new MatriculaDuplicadaException("Ya existe otro vehículo con la matrícula: " + update.getMatricula());
                    });
        }

        if (update.getSocioId() != null) {
            obtenerSocioActivo(update.getSocioId());
        }

        vehiculoRepository.updateEntity(vehiculo, update);
        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo ID {} actualizado correctamente.", id);
        return vehiculoRepository.fromEntity(actualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Iniciando baja lógica de vehículo con ID: {}", id);

        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: vehículo con ID {} no encontrado.", id);
                    return new RegistroNoEncontradoException("No existe vehículo con ID " + id);
                });

        List<AsignacionVehiculoGarage> asignaciones = asignacionVehiculoGarageRepository.findAllByVehiculoIdAndActivoTrue(id);
        asignaciones.forEach(asignacion -> asignacion.setActivo(false));
        asignacionVehiculoGarageRepository.saveAll(asignaciones);
        log.info("Se liberó el garage: {} asignación(es) del vehículo ID {} dadas de baja.", asignaciones.size(), id);

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
        log.info("Baja lógica del vehículo con ID {} realizada con éxito.", id);
    }

    /**
     * Obtiene un socio activo o lanza la excepción correspondiente.
     *
     * @param socioId Identificador del socio.
     * @return El {@link Socio} activo.
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    private Socio obtenerSocioActivo(Integer socioId) {
        return socioRepository.findByIdAndActivoTrue(socioId)
                .orElseThrow(() -> {
                    log.error("Socio ID {} no existe o se encuentra inactivo.", socioId);
                    return new RegistroNoEncontradoException("No se encontró el socio activo con ID: " + socioId);
                });
    }
}
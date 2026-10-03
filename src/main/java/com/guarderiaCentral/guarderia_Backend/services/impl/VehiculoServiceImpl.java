package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRepository;
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
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la entidad {@link Vehiculo}.
 * Maneja persistencia, validación de matrículas, relaciones con socios, borrado lógico
 * y filtros según responsabilidades de zonas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final SocioRepository socioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionEmpleadoZonaRepository asignacionEmpleadoZonaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public VehiculoResponse registrarVehiculo(VehiculoRequest request) {
        log.info("Iniciando registro de vehículo con matrícula: {}", request.getMatricula());

        if (vehiculoRepository.existsByMatriculaAndActivoTrue(request.getMatricula())) {
            log.error("Error al registrar vehículo: Matrícula {} duplicada.", request.getMatricula());
            throw new MatriculaDuplicadaException("Ya existe un vehículo registrado con la matrícula: " + request.getMatricula());
        }

        Socio socio = socioRepository.findByIdAndActivoTrue(request.getSocioId())
                .orElseThrow(() -> {
                    log.error("Socio con ID {} no encontrado al registrar vehículo.", request.getSocioId());
                    return new RegistroNoEncontradoException("No se encontró el socio activo con ID: " + request.getSocioId());
                });

        Vehiculo vehiculo = vehiculoRepository.toEntity(request);
        vehiculo.setSocio(socio);
        vehiculo.setActivo(true);

        Vehiculo vehiculoGuardado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo guardado exitosamente con ID: {}", vehiculoGuardado.getId());

        return vehiculoRepository.fromEntity(vehiculoGuardado);
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
        return vehiculoRepository.findAll().stream()
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
        validarSocioExistente(socioId);

        return vehiculoRepository.findAllBySocioIdAndActivoTrue(socioId).stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> buscarVehiculosPorSocio(Integer socioId) {
        return listarPorSocio(socioId);
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
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosPorResponsable(int empleadoId) {
        log.debug("Obteniendo vehículos pertenecientes a las zonas del empleado responsable ID: {}", empleadoId);

        if (!empleadoRepository.existsByIdAndActivoTrue(empleadoId)) {
            log.error("Empleado con ID {} no existe o está inactivo.", empleadoId);
            throw new RegistroNoEncontradoException("No se encontró el empleado activo con ID: " + empleadoId);
        }

        List<Integer> zonaIds = asignacionEmpleadoZonaRepository.findAllByEmpleadoIdAndActivoTrue(empleadoId).stream()
                .map(a -> a.getZona().getId())
                .collect(Collectors.toList());

        return vehiculoRepository.findAllByZonaIdInAndActivoTrue(zonaIds).stream()
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public VehiculoResponse actualizarVehiculo(Integer id, VehiculoUpdate update) {
        log.info("Iniciando actualización de vehículo ID: {}", id);

        Vehiculo vehiculoExistente = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Vehículo ID {} no encontrado para actualizar.", id);
                    return new RegistroNoEncontradoException("No existe vehículo con ID " + id);
                });

        if (update.getMatricula() != null) {
            vehiculoRepository.findByMatriculaAndActivoTrue(update.getMatricula())
                    .ifPresent(v -> {
                        if (!v.getId().equals(id)) {
                            log.error("La matrícula {} ya pertenece a otro vehículo activo (ID: {})", update.getMatricula(), v.getId());
                            throw new MatriculaDuplicadaException("Ya existe otro vehículo con la matrícula: " + update.getMatricula());
                        }
                    });
        }

        if (update.getSocioId() != null) {
            Socio nuevoSocio = socioRepository.findByIdAndActivoTrue(update.getSocioId())
                    .orElseThrow(() -> {
                        log.error("Socio ID {} no encontrado para vincular al vehículo.", update.getSocioId());
                        return new RegistroNoEncontradoException("No existe socio activo con ID " + update.getSocioId());
                    });
            vehiculoExistente.setSocio(nuevoSocio);
        }

        vehiculoRepository.updateEntity(vehiculoExistente, update);
        Vehiculo vehiculoActualizado = vehiculoRepository.save(vehiculoExistente);
        log.info("Vehículo ID {} actualizado correctamente.", id);

        return vehiculoRepository.fromEntity(vehiculoActualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarVehiculo(String matricula) {
        log.info("Iniciando baja lógica de vehículo con matrícula: {}", matricula);

        Vehiculo vehiculo = vehiculoRepository.findByMatriculaAndActivoTrue(matricula)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: Vehículo con matrícula {} no encontrado.", matricula);
                    return new RegistroNoEncontradoException("No existe vehículo con matrícula " + matricula);
                });

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
        log.info("Baja lógica del vehículo con matrícula {} realizada con éxito.", matricula);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarVehiculoPorId(Integer id) {
        log.info("Iniciando baja lógica de vehículo con ID: {}", id);

        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: Vehículo con ID {} no encontrado.", id);
                    return new RegistroNoEncontradoException("No existe vehículo con ID " + id);
                });

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
        log.info("Baja lógica del vehículo con ID {} realizada con éxito.", id);
    }

    /**
     * Método auxiliar privado para validar que el socio exista y esté activo en el sistema.
     *
     * @param socioId Identificador del socio a validar.
     * @throws RegistroNoEncontradoException Si el socio no se encuentra activo.
     */
    private void validarSocioExistente(Integer socioId) {
        if (!socioRepository.existsByIdAndActivoTrue(socioId)) {
            log.error("Socio ID {} no existe o se encuentra inactivo.", socioId);
            throw new RegistroNoEncontradoException("Socio no encontrado con ID: " + socioId);
        }
    }
}
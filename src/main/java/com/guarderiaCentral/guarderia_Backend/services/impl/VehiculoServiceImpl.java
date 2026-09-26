package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.MatriculaDuplicadaException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoUpdate;
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
     * Registra un nuevo vehículo realizando validaciones de unicidad de matrícula
     * y existencia del socio propietario.
     *
     * @param request Datos del vehículo a registrar.
     * @return {@link VehiculoResponse} DTO con el vehículo guardado.
     * @throws MatriculaDuplicadaException Si la matrícula ya pertenece a un vehículo activo.
     * @throws RegistroNoEncontradoException Si el socio especificado no existe o está inactivo.
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
     * Busca un vehículo activo por su ID.
     *
     * @param id Identificador único del vehículo.
     * @return DTO {@link VehiculoResponse}.
     * @throws RegistroNoEncontradoException Si no existe registro activo.
     */
    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse buscarPorId(int id) {
        log.debug("Buscando vehículo por ID: {}", id);
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se encontró vehículo activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró vehículo con ID: " + id);
                });
        return vehiculoRepository.fromEntity(vehiculo);
    }

    /**
     * Busca un vehículo activo por su matrícula.
     *
     * @param matricula Matrícula del vehículo.
     * @return DTO {@link VehiculoResponse}.
     * @throws RegistroNoEncontradoException Si no existe registro activo.
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
     * Obtiene la lista completa de vehículos activos.
     *
     * @return Lista de DTOs {@link VehiculoResponse}.
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
     * Obtiene los vehículos asociados a un socio activo determinado en formato DTO simple.
     *
     * @param socioId ID del socio.
     * @return Lista de {@link VehiculoDTO}.
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> listarPorSocio(int socioId) {
        log.debug("Listando vehículos en formato DTO para socio ID: {}", socioId);
        validarSocioExistente(socioId);

        return vehiculoRepository.findAllBySocioIdAndActivoTrue(socioId).stream()
                .map(v -> new VehiculoDTO(
                        v.getId(),
                        v.getMatricula(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getTipo() != null ? v.getTipo().name() : null,
                        v.getSocio() != null ? v.getSocio().getId() : 0,
                        v.getActivo()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Método alternativo para buscar vehículos por socio ID.
     *
     * @param socioId ID del socio.
     * @return Lista de {@link VehiculoDTO}.
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> buscarVehiculosPorSocio(int socioId) {
        return listarPorSocio(socioId);
    }

    /**
     * Busca vehículos activos filtrados por tipo de vehículo.
     *
     * @param tipo Tipo de vehículo (e.g. LANCHA, MOTO_AQUATICA).
     * @return Lista de DTOs {@link VehiculoResponse}.
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
     * Obtiene todos los vehículos pertenecientes a las zonas donde un empleado específico
     * se encuentra asignado como responsable. Cumple con la regla de negocio donde la asignación
     * de empleados se realiza por zona a través de AsignacionEmpleadoZona.
     *
     * @param empleadoId Identificador único del empleado.
     * @return Lista de DTOs {@link VehiculoDTO} de las zonas a cargo del empleado.
     * @throws RegistroNoEncontradoException Si el empleado no existe o no está activo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> listarVehiculosPorResponsable(int empleadoId) {
        log.debug("Obteniendo vehículos pertenecientes a las zonas del empleado responsable ID: {}", empleadoId);

        if (!empleadoRepository.existsByIdAndActivoTrue(empleadoId)) {
            log.error("Empleado con ID {} no existe o está inactivo.", empleadoId);
            throw new RegistroNoEncontradoException("No se encontró el empleado activo con ID: " + empleadoId);
        }

        List<Integer> zonaIds = asignacionEmpleadoZonaRepository.findAllByEmpleadoIdAndActivoTrue(empleadoId).stream()
                .map(a -> a.getZona().getId())
                .collect(Collectors.toList());

        return vehiculoRepository.findAllByZonaIdInAndActivoTrue(zonaIds).stream()
                .map(v -> new VehiculoDTO(
                        v.getId(),
                        v.getMatricula(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getTipo() != null ? v.getTipo().name() : null,
                        v.getSocio() != null ? v.getSocio().getId() : 0,
                        v.getActivo()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un vehículo activo.
     *
     * @param id Identificador del vehículo a modificar.
     * @param update DTO con los datos a actualizar.
     * @return DTO {@link VehiculoResponse} con los datos actualizados.
     * @throws RegistroNoEncontradoException Si el vehículo o el nuevo socio no existen o están inactivos.
     * @throws MatriculaDuplicadaException Si la nueva matrícula está asignada a otro vehículo activo.
     */
    @Override
    @Transactional
    public VehiculoResponse actualizarVehiculo(int id, VehiculoUpdate update) {
        log.info("Iniciando actualización de vehículo ID: {}", id);

        Vehiculo vehiculoExistente = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Vehículo ID {} no encontrado para actualizar.", id);
                    return new RegistroNoEncontradoException("No existe vehículo con ID " + id);
                });

        vehiculoRepository.findByMatriculaAndActivoTrue(update.getMatricula())
                .ifPresent(v -> {
                    if (v.getId() != id) {
                        log.error("La matrícula {} ya pertenece a otro vehículo activo (ID: {})", update.getMatricula(), v.getId());
                        throw new MatriculaDuplicadaException("Ya existe otro vehículo con la matrícula: " + update.getMatricula());
                    }
                });

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
     * Realiza el borrado lógico de un vehículo buscando por su matrícula.
     *
     * @param matricula Matrícula del vehículo a dar de baja.
     * @throws RegistroNoEncontradoException Si no existe vehículo activo con la matrícula especificada.
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
     * Realiza el borrado lógico de un vehículo buscando por su ID.
     *
     * @param id Identificador único del vehículo a dar de baja.
     * @throws RegistroNoEncontradoException Si no existe vehículo activo con el ID especificado.
     */
    @Override
    @Transactional
    public void eliminarVehiculoPorId(int id) {
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
    private void validarSocioExistente(int socioId) {
        if (!socioRepository.existsByIdAndActivoTrue(socioId)) {
            log.error("Socio ID {} no existe o se encuentra inactivo.", socioId);
            throw new RegistroNoEncontradoException("Socio no encontrado con ID: " + socioId);
        }
    }
}
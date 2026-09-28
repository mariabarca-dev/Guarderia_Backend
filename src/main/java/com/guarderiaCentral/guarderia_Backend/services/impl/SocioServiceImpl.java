package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de la entidad {@link Socio}.
 * Administra las validaciones de negocio, borrado lógico y mapeo con repositorios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocioServiceImpl implements SocioService {

    private final SocioRepository socioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoService vehiculoService;
    private final PropiedadGarageService propiedadGarageService;
    private final PasswordEncoder passwordEncoder;

    private static final LocalDate FECHA_FUNDACION = LocalDate.of(2000, 1, 1);

    /**
     * Registra un nuevo socio aplicando las validaciones requeridas.
     *
     * @param request Datos del nuevo socio.
     * @return {@link SocioResponse} con los datos guardados.
     * @throws DniDuplicadoException Si el DNI ya está en uso.
     * @throws BusinessException Si las validaciones de negocio fallan (fecha de ingreso, nombre de usuario o rol).
     */
    @Override
    @Transactional
    public SocioResponse registrarSocio(SocioRequest request) {
        log.info("Iniciando registro de nuevo socio con DNI: {}", request.getDni());

        if (usuarioRepository.existsByDniAndActivoTrue(request.getDni())) {
            log.error("Error al registrar socio: DNI {} ya registrado.", request.getDni());
            throw new DniDuplicadoException("Ya existe un socio o usuario registrado con el DNI: " + request.getDni());
        }

        if (usuarioRepository.existsByNombreUsuarioAndActivoTrue(request.getNombreUsuario())) {
            log.error("Error al registrar socio: Nombre de usuario '{}' ya existe.", request.getNombreUsuario());
            throw new BusinessException("El nombre de usuario '" + request.getNombreUsuario() + "' ya se encuentra en uso.", HttpStatus.BAD_REQUEST);
        }

        if (request.getFechaIngreso() != null && request.getFechaIngreso().isBefore(FECHA_FUNDACION)) {
            log.error("Error al registrar socio: Fecha de ingreso {} anterior a la fundación.", request.getFechaIngreso());
            throw new BusinessException("La fecha de ingreso no puede ser anterior a la fecha de fundación del sistema (" + FECHA_FUNDACION + ").", HttpStatus.BAD_REQUEST);
        }

        if (request.getRol() != Rol.SOCIO && request.getRol() != Rol.ADMINISTRADOR) {
            log.error("Error al registrar socio: Rol asignado no válido ({})", request.getRol());
            throw new BusinessException("El rol asignado no cuenta con los permisos permitidos para este tipo de registro.", HttpStatus.BAD_REQUEST);
        }

        Socio socio = socioRepository.toEntity(request);
        socio.setPassword(passwordEncoder.encode(request.getPassword()));
        socio.setActivo(true);

        Socio socioGuardado = socioRepository.save(socio);
        log.info("Socio registrado exitosamente con ID: {}", socioGuardado.getId());

        return socioRepository.fromEntity(socioGuardado);
    }

    /**
     * Busca un socio activo por su identificador.
     *
     * @param id Identificador único del socio.
     * @return DTO {@link SocioResponse}.
     * @throws RegistroNoEncontradoException Si no se encuentra el registro activo.
     */
    @Override
    @Transactional(readOnly = true)
    public SocioResponse buscarPorId(Integer id) {
        log.debug("Buscando socio por ID: {}", id);
        Socio socio = socioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se encontró socio activo con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró el socio con ID: " + id);
                });
        return socioRepository.fromEntity(socio);
    }

    /**
     * Busca un socio activo por su DNI.
     *
     * @param dni Documento nacional de identidad.
     * @return DTO {@link SocioResponse}.
     * @throws RegistroNoEncontradoException Si no existe un socio activo con ese DNI.
     */
    @Override
    @Transactional(readOnly = true)
    public SocioResponse buscarPorDni(String dni) {
        log.debug("Buscando socio por DNI: {}", dni);
        Socio socio = socioRepository.findByDniAndActivoTrue(dni)
                .orElseThrow(() -> {
                    log.error("No se encontró socio activo con DNI: {}", dni);
                    return new RegistroNoEncontradoException("No se encontró el socio con DNI: " + dni);
                });
        return socioRepository.fromEntity(socio);
    }

    /**
     * Lista todos los socios que se encuentran activos.
     *
     * @return Lista de DTOs {@link SocioResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SocioResponse> listarTodos() {
        log.debug("Obteniendo listado de todos los socios activos");
        return socioRepository.findAllByActivoTrue().stream()
                .map(socioRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un socio activo existente.
     *
     * @param id Identificador del socio a modificar.
     * @param update DTO con los datos modificados.
     * @return DTO {@link SocioResponse} actualizado.
     * @throws RegistroNoEncontradoException Si el socio no se encuentra activo.
     * @throws BusinessException Si el DNI ya pertenece a otro usuario activo.
     */
    @Override
    @Transactional
    public SocioResponse actualizarSocio(Integer id, SocioUpdate update) {
        log.info("Iniciando actualización para el socio con ID: {}", id);

        Socio socioExistente = socioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Socio con ID {} no encontrado para actualizar.", id);
                    return new RegistroNoEncontradoException("No se puede actualizar: Socio no encontrado con ID " + id);
                });

        usuarioRepository.findByDniAndActivoTrue(update.getDni())
                .ifPresent(u -> {
                    if (u.getId() != id) {
                        log.error("El DNI {} ya se encuentra en uso por otro usuario (ID: {})", update.getDni(), u.getId());
                        throw new BusinessException("El DNI " + update.getDni() + " ya está asignado a otro socio.", HttpStatus.BAD_REQUEST);
                    }
                });

        socioRepository.updateEntity(socioExistente, update);
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            socioExistente.setPassword(passwordEncoder.encode(update.getPassword()));
        }

        Socio socioActualizado = socioRepository.save(socioExistente);
        log.info("Socio con ID {} actualizado exitosamente.", id);

        return socioRepository.fromEntity(socioActualizado);
    }

    /**
     * Realiza la baja lógica del socio estableciendo su campo 'activo' en false.
     * Regla de cascada: Se inhabilita el acceso del socio en el sistema. Las relaciones
     * asociativas (como vehículos o propiedades) permanecen registradas históricamente
     * pero no se podrán asociar a nuevas operaciones operativas mientras el socio esté inactivo.
     *
     * @param id Identificador del socio a dar de baja.
     * @throws RegistroNoEncontradoException Si el socio no existe o ya se encuentra inactivo.
     */
    @Override
    @Transactional
    public void eliminarSocio(Integer id) {
        log.info("Iniciando baja lógica del socio con ID: {}", id);

        Socio socio = socioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: Socio no encontrado con ID {}", id);
                    return new RegistroNoEncontradoException("No se puede eliminar: Socio no encontrado con ID " + id);
                });

        socio.setActivo(false);
        socioRepository.save(socio);
        log.info("Baja lógica del socio con ID {} completada correctamente.", id);
    }

    /**
     * Obtiene los vehículos asignados al socio especificado.
     *
     * @param socioId Identificador del socio.
     * @return Lista de DTOs {@link VehiculoResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosPorSocio(Integer socioId) {
        log.debug("Listando vehículos para el socio ID: {}", socioId);
        validarSocioExistente(socioId);
        return vehiculoService.listarPorSocio(socioId);
    }

    /**
     * Obtiene los garages pertenecientes al socio especificado.
     *
     * @param socioId Identificador del socio.
     * @return Lista de DTOs {@link GarageResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> listarGarajesPorSocio(Integer socioId) {
        log.debug("Listando garages en propiedad para el socio ID: {}", socioId);
        validarSocioExistente(socioId);
        return propiedadGarageService.listarPorSocio(socioId);
    }

    /**
     * Consulta el estado del garage asignado/adquirido por el socio.
     *
     * @param socioId Identificador del socio.
     * @return Descripción del estado actual del garage del socio.
     */
    @Override
    @Transactional(readOnly = true)
    public String obtenerEstadoGarageSocio(Integer socioId) {
        log.debug("Obteniendo estado del garage para el socio ID: {}", socioId);
        validarSocioExistente(socioId);
        return propiedadGarageService.obtenerEstadoGarageSocio(socioId);
    }

    /**
     * Método auxilar para verificar si un socio existe y está activo.
     *
     * @param socioId ID del socio a verificar.
     * @throws RegistroNoEncontradoException Si no existe un socio activo con el ID proporcionado.
     */
    private void validarSocioExistente(Integer socioId) {
        if (!socioRepository.existsByIdAndActivoTrue(socioId)) {
            log.error("Operación cancelada: El socio con ID {} no existe o está inactivo.", socioId);
            throw new RegistroNoEncontradoException("Socio no encontrado con ID: " + socioId);
        }
    }
}
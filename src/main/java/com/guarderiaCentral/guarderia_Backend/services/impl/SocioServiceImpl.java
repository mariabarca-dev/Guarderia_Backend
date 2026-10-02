package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de la entidad {@link Socio}.
 * Administra validaciones, guardado inteligente con reactivación, baja lógica con bloqueo y mapeos.
 *
 * @author Guardería Central
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
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SocioResponse crear(SocioRequest request) {
        log.info("Iniciando registro de nuevo socio con DNI: {}", request.getDni());

        // 1. Validar fecha de ingreso
        if (request.getFechaIngreso() != null && request.getFechaIngreso().isBefore(FECHA_FUNDACION)) {
            log.error("Error al registrar socio: Fecha de ingreso {} anterior a la fundación.", request.getFechaIngreso());
            throw new BusinessException("La fecha de ingreso no puede ser anterior a la fecha de fundación del sistema (" + FECHA_FUNDACION + ").");
        }

        // 2. Validar Rol asignado
        if (request.getRol() != Rol.SOCIO && request.getRol() != Rol.ADMINISTRADOR) {
            log.error("Error al registrar socio: Rol asignado no válido ({})", request.getRol());
            throw new BusinessException("El rol asignado no cuenta con los permisos permitidos para este tipo de registro.");
        }

        // 3. Verificar si el nombreUsuario pertenece a otro usuario en toda la jerarquía (activos e inactivos)
        boolean existeNombreUsuario = usuarioRepository.findAllIncludingInactive().stream()
                .anyMatch(u -> u.getNombreUsuario() != null && u.getNombreUsuario().equalsIgnoreCase(request.getNombreUsuario()));

        // 4. Buscar socio por DNI incluyendo inactivos para guardado inteligente / reactivación
        Optional<Socio> socioInactivoOpt = socioRepository.findByDniIncludingInactive(request.getDni());

        if (socioInactivoOpt.isPresent()) {
            Socio existente = socioInactivoOpt.get();

            if (Boolean.TRUE.equals(existente.getActivo())) {
                log.error("Error al registrar socio: DNI {} ya registrado y activo.", request.getDni());
                throw new DniDuplicadoException("Ya existe un socio o usuario registrado con el DNI: " + request.getDni());
            }

            // Validar que el nombreUsuario ingresado no pertenezca a otra cuenta diferente
            if (existeNombreUsuario && !existente.getNombreUsuario().equalsIgnoreCase(request.getNombreUsuario())) {
                throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + request.getNombreUsuario() + "' ya se encuentra registrado.");
            }

            log.info("Reactivando socio inactivo con ID: {}", existente.getId());

            SocioUpdate update = new SocioUpdate();
            update.setNombre(request.getNombre());
            update.setApellido(request.getApellido());
            update.setDireccion(request.getDireccion());
            update.setTelefono(request.getTelefono());
            update.setNombreUsuario(request.getNombreUsuario());
            if (request.getClave() != null && !request.getClave().isBlank()) {
                update.setClave(passwordEncoder.encode(request.getClave()));
            }
            update.setRol(request.getRol());
            update.setDni(request.getDni());
            update.setFechaIngreso(request.getFechaIngreso());

            socioRepository.updateEntity(existente, update);
            existente.setActivo(true);

            Socio reactivado = socioRepository.save(existente);
            return socioRepository.fromEntity(reactivado);
        }

        if (existeNombreUsuario) {
            log.error("Error al registrar socio: Nombre de usuario '{}' ya existe.", request.getNombreUsuario());
            throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + request.getNombreUsuario() + "' ya se encuentra en uso.");
        }

        Socio socio = socioRepository.toEntity(request);
        socio.setClave(passwordEncoder.encode(request.getClave()));
        socio.setActivo(true);

        Socio socioGuardado = socioRepository.save(socio);
        log.info("Socio registrado exitosamente con ID: {}", socioGuardado.getId());

        return socioRepository.fromEntity(socioGuardado);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SocioResponse> listarTodosIncluyendoInactivos() {
        log.debug("Obteniendo listado de todos los socios (incluyendo inactivos)");
        return socioRepository.findAllIncludingInactive().stream()
                .map(socioRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SocioResponse actualizar(Integer id, SocioUpdate update) {
        log.info("Iniciando actualización para el socio con ID: {}", id);

        Socio socioExistente = socioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Socio con ID {} no encontrado para actualizar.", id);
                    return new RegistroNoEncontradoException("No se puede actualizar: Socio no encontrado con ID " + id);
                });

        if (update.getDni() != null && !update.getDni().equalsIgnoreCase(socioExistente.getDni())) {
            socioRepository.findByDniAndActivoTrue(update.getDni())
                    .ifPresent(u -> {
                        if (!u.getId().equals(id)) {
                            log.error("El DNI {} ya se encuentra en uso por otro socio (ID: {})", update.getDni(), u.getId());
                            throw new DniDuplicadoException("El DNI " + update.getDni() + " ya está asignado a otro socio.");
                        }
                    });
        }

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equalsIgnoreCase(socioExistente.getNombreUsuario())) {
            boolean existeNombreUsuario = usuarioRepository.findAllIncludingInactive().stream()
                    .anyMatch(u -> u.getNombreUsuario() != null
                            && u.getNombreUsuario().equalsIgnoreCase(update.getNombreUsuario())
                            && !u.getId().equals(id));
            if (existeNombreUsuario) {
                throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + update.getNombreUsuario() + "' ya se encuentra registrado.");
            }
        }

        socioRepository.updateEntity(socioExistente, update);
        if (update.getClave() != null && !update.getClave().isBlank()) {
            socioExistente.setClave(passwordEncoder.encode(update.getClave()));
        }

        Socio socioActualizado = socioRepository.save(socioExistente);
        log.info("Socio con ID {} actualizado exitosamente.", id);

        return socioRepository.fromEntity(socioActualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Iniciando baja lógica del socio con ID: {}", id);

        Socio socio = socioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: Socio no encontrado con ID {}", id);
                    return new RegistroNoEncontradoException("No se puede eliminar: Socio no encontrado con ID " + id);
                });

        // REGLA DE NEGOCIO - Bloqueo por dependencias activas:
        // 1. Vehículos activos
        List<VehiculoResponse> vehiculosActivos = vehiculoService.listarPorSocio(id);
        if (!vehiculosActivos.isEmpty()) {
            log.warn("Bloqueo de baja lógica: El socio ID {} posee vehículos activos registrados.", id);
            throw new DependenciasActivasException("No se puede eliminar el socio porque tiene vehículos activos registrados.");
        }

        // 2. Propiedades de garage vigentes
        List<PropiedadGarageResponse> propiedadesActivas = propiedadGarageService.listarPorSocio(id);
        if (!propiedadesActivas.isEmpty()) {
            log.warn("Bloqueo de baja lógica: El socio ID {} posee garajes a su nombre.", id);
            throw new DependenciasActivasException("No se puede eliminar el socio porque posee garajes a su nombre.");
        }

        socio.setActivo(false);
        socioRepository.save(socio);
        log.info("Baja lógica del socio con ID {} completada correctamente.", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosPorSocio(Integer socioId) {
        log.debug("Listando vehículos para el socio ID: {}", socioId);
        validarSocioExistente(socioId);
        return vehiculoService.listarPorSocio(socioId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarGarajesPorSocio(Integer socioId) {
        log.debug("Listando garajes en propiedad para el socio ID: {}", socioId);
        validarSocioExistente(socioId);
        return propiedadGarageService.listarPorSocio(socioId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PropiedadGarageResponse obtenerEstadoGarageSocio(Integer socioId) {
        log.debug("Obteniendo estado del garaje para el socio ID: {}", socioId);
        validarSocioExistente(socioId);

        List<PropiedadGarageResponse> propiedades = propiedadGarageService.listarPorSocio(socioId);
        if (propiedades.isEmpty()) {
            throw new RegistroNoEncontradoException("El socio con ID " + socioId + " no posee garajes asignados.");
        }

        return propiedades.get(0);
    }

    /**
     * Método auxiliar para verificar si un socio existe y está activo.
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
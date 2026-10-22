package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.AccesoDenegadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DependenciasActivasException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.GarageOcupadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de la entidad {@link Socio}.
 * Administra validaciones, guardado inteligente con reactivación, baja lógica con bloqueo y mapeos.
 * El rol de toda cuenta de socio es siempre {@code Rol.SOCIO}: lo fija el repository y no se
 * puede elegir ni modificar desde el Request o el Update.
 * <p>
 * El DNI del socio es obligatorio y único entre todas las cuentas del sistema (no solo entre socios).
 * </p>
 * <p>
 * La fecha de ingreso no puede ser anterior a la fundación de la guardería (al crear y al modificar) ni
 * posterior a la compra más antigua de los garages del socio (al modificar), para que nunca quede un garage
 * comprado antes de que el socio ingresara.
 * </p>
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
    private final AsignacionVehiculoGarageService asignacionVehiculoGarageService;
    private final PasswordEncoder passwordEncoder;

    private static final LocalDate FECHA_FUNDACION = LocalDate.of(2000, 1, 1);

    /**
     * {@inheritDoc}
     * Si el DNI pertenece a un socio inactivo, se reactiva ese socio; si pertenece a cualquier otra cuenta
     * (activa o inactiva), se rechaza el alta.
     */
    @Override
    @Transactional
    public SocioResponse crear(SocioRequest request) {
        log.info("Iniciando registro de nuevo socio con DNI: {}", request.getDni());

        // 1. Validar fecha de ingreso
        validarFechaIngresoDesdeFundacion(request.getFechaIngreso());

        // 2. Verificar si el nombreUsuario pertenece a otro usuario en toda la jerarquía (activos e inactivos)
        Optional<Integer> duenoNombreUsuarioOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(request.getNombreUsuario());

        // 3. Verificar a quién pertenece el DNI en toda la jerarquía y buscar el socio por DNI (incluyendo inactivos)
        Optional<Integer> duenoDniOpt = usuarioRepository.buscarIdPorDniIncluyendoInactivos(request.getDni());
        Optional<Socio> socioInactivoOpt = socioRepository.findByDniIncludingInactive(request.getDni());

        if (socioInactivoOpt.isPresent()) {
            Socio existente = socioInactivoOpt.get();

            if (Boolean.TRUE.equals(existente.getActivo())) {
                log.error("Error al registrar socio: DNI {} ya registrado y activo.", request.getDni());
                throw new DniDuplicadoException("Ya existe un socio o usuario registrado con el DNI: " + request.getDni());
            }

            // Validar que el nombreUsuario ingresado no pertenezca a otra cuenta diferente
            if (duenoNombreUsuarioOpt.isPresent() && !duenoNombreUsuarioOpt.get().equals(existente.getId())) {
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
            update.setDni(request.getDni());
            update.setFechaIngreso(request.getFechaIngreso());

            socioRepository.updateEntity(existente, update);
            existente.setActivo(true);

            Socio reactivado = socioRepository.save(existente);
            return socioRepository.fromEntity(reactivado);
        }

        // El DNI pertenece a una cuenta que no es de un socio (empleado o administrador, activa o inactiva)
        if (duenoDniOpt.isPresent()) {
            log.error("Error al registrar socio: el DNI {} ya pertenece a otra cuenta.", request.getDni());
            throw new DniDuplicadoException("El DNI " + request.getDni() + " ya se encuentra registrado en otra cuenta.");
        }

        if (duenoNombreUsuarioOpt.isPresent()) {
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
     * Si se modifica el DNI, no puede pertenecer a ninguna otra cuenta del sistema. Si se modifica la fecha
     * de ingreso, no puede ser anterior a la fundación de la guardería ni posterior a la compra más antigua
     * de los garages activos del socio.
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
            validarDniUnico(update.getDni(), id);
        }

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equalsIgnoreCase(socioExistente.getNombreUsuario())) {
            Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(update.getNombreUsuario());
            if (duenoOpt.isPresent() && !duenoOpt.get().equals(id)) {
                throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + update.getNombreUsuario() + "' ya se encuentra registrado.");
            }
        }

        if (update.getFechaIngreso() != null) {
            validarFechaIngresoDesdeFundacion(update.getFechaIngreso());
            validarFechaIngresoAnteriorALasCompras(id, update.getFechaIngreso());
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
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SocioResponse buscarMiPerfil(String nombreUsuario) {
        log.debug("Consultando el perfil del socio autenticado: {}", nombreUsuario);
        return socioRepository.fromEntity(obtenerSocioAutenticado(nombreUsuario));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarMisVehiculos(String nombreUsuario) {
        Socio socio = obtenerSocioAutenticado(nombreUsuario);
        log.debug("Listando vehículos del socio autenticado ID: {}", socio.getId());
        return vehiculoService.listarPorSocio(socio.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse buscarMiVehiculo(Integer vehiculoId, String nombreUsuario) {
        Socio socio = obtenerSocioAutenticado(nombreUsuario);
        VehiculoResponse vehiculo = vehiculoService.buscarPorId(vehiculoId);

        if (!socio.getId().equals(vehiculo.getSocioId())) {
            log.warn("Acceso denegado: el socio ID {} intentó consultar el vehículo ID {} de otro socio.",
                    socio.getId(), vehiculoId);
            throw new AccesoDenegadoException("El vehículo solicitado no pertenece al socio autenticado.");
        }
        return vehiculo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarMisPropiedades(String nombreUsuario) {
        Socio socio = obtenerSocioAutenticado(nombreUsuario);
        log.debug("Listando propiedades de garage del socio autenticado ID: {}", socio.getId());
        return propiedadGarageService.listarPorSocio(socio.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<GarageOcupadoResponse> listarMisGaragesOcupados(String nombreUsuario) {
        Socio socio = obtenerSocioAutenticado(nombreUsuario);
        log.debug("Listando garages ocupados por los vehículos del socio autenticado ID: {}", socio.getId());
        return asignacionVehiculoGarageService.listarGaragesOcupadosPorSocio(socio.getId());
    }

    /**
     * Valida que el DNI no pertenezca a ninguna otra cuenta del sistema (activa o inactiva, de cualquier tipo).
     *
     * @param dni       DNI a validar.
     * @param idExcluir ID de la cuenta que se está modificando (no cuenta como "otra").
     * @throws DniDuplicadoException Si el DNI ya pertenece a otra cuenta.
     */
    private void validarDniUnico(String dni, Integer idExcluir) {
        Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorDniIncluyendoInactivos(dni);
        if (duenoOpt.isPresent() && !duenoOpt.get().equals(idExcluir)) {
            log.error("El DNI {} ya se encuentra en uso por otra cuenta (ID: {})", dni, duenoOpt.get());
            throw new DniDuplicadoException("El DNI " + dni + " ya está asignado a otra cuenta.");
        }
    }

    /**
     * Valida que la fecha de ingreso no sea anterior a la fecha de fundación de la guardería.
     * Se usa tanto al crear como al modificar un socio.
     *
     * @param fechaIngreso Fecha de ingreso a validar (puede ser null, en cuyo caso no se valida).
     * @throws BusinessException Si la fecha es anterior a la fundación.
     */
    private void validarFechaIngresoDesdeFundacion(LocalDate fechaIngreso) {
        if (fechaIngreso != null && fechaIngreso.isBefore(FECHA_FUNDACION)) {
            log.error("Fecha de ingreso {} anterior a la fundación.", fechaIngreso);
            throw new BusinessException("La fecha de ingreso no puede ser anterior a la fecha de fundación del sistema (" + FECHA_FUNDACION + ").");
        }
    }

    /**
     * Valida que la nueva fecha de ingreso no sea posterior a la compra más antigua de los garages activos del
     * socio, para que nunca quede un garage comprado antes de que el socio ingresara.
     *
     * @param socioId          ID del socio que se está modificando.
     * @param nuevaFechaIngreso Nueva fecha de ingreso solicitada.
     * @throws BusinessException Si el socio ya compró un garage antes de la nueva fecha de ingreso (409).
     */
    private void validarFechaIngresoAnteriorALasCompras(Integer socioId, LocalDate nuevaFechaIngreso) {
        Optional<LocalDate> primeraCompra = propiedadGarageService.listarPorSocio(socioId).stream()
                .map(PropiedadGarageResponse::getFechaCompraGarage)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);

        if (primeraCompra.isPresent() && nuevaFechaIngreso.isAfter(primeraCompra.get())) {
            log.error("No se puede cambiar el ingreso del socio ID {} a {}: ya compró un garage el {}.",
                    socioId, nuevaFechaIngreso, primeraCompra.get());
            throw new BusinessException("Error: No se puede cambiar la fecha de ingreso del socio a " + nuevaFechaIngreso
                    + " porque ya compró un garage el " + primeraCompra.get()
                    + ". Elegí una fecha de ingreso igual o anterior a esa compra.", HttpStatus.CONFLICT);
        }
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

    /**
     * Resuelve el socio autenticado a partir del nombre de usuario que viene del token.
     *
     * @param nombreUsuario Nombre de usuario del {@code Authentication}.
     * @return El {@link Socio} activo correspondiente.
     * @throws RegistroNoEncontradoException Si no existe un socio activo con ese nombre de usuario.
     */
    private Socio obtenerSocioAutenticado(String nombreUsuario) {
        return socioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio autenticado."));
    }
}
package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.CodigoEmpleadoDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionEmpleadoZona;
import com.guarderiaCentral.guarderia_Backend.modelos.Empleado;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas.AsignacionEmpleadoZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.empleados.EmpleadoUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoResponse;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicio {@link EmpleadoService}.
 * Maneja la lógica de negocio de los empleados, validaciones de código, nombre de usuario y DNI (opcional,
 * pero único entre todas las cuentas cuando se carga), guardado inteligente/reactivación, propagación en
 * cascada de baja lógica sobre asignaciones, la relación empleado-vehículo (vehículos que cada empleado
 * atiende) y las consultas del empleado autenticado.
 *
 * @author Cátedra Guardería Central
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionEmpleadoZonaRepository asignacionEmpleadoZonaRepository;
    private final AsignacionVehiculoGarageRepository asignacionVehiculoGarageRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        log.info("Iniciando creación de nuevo empleado con código: {} y nombreUsuario: {}", request.getCodigo(), request.getNombreUsuario());

        // 1. Validar unicidad global del nombreUsuario en toda la jerarquía de usuarios (activos e inactivos)
        validarNombreUsuarioUnico(request.getNombreUsuario(), null);

        // 2. Verificar duplicados o presencia en inactivos por el código único de empleado
        Optional<Empleado> inactivoOpt = empleadoRepository.findAllIncludingInactive().stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().equalsIgnoreCase(request.getCodigo()))
                .findFirst();

        if (inactivoOpt.isPresent()) {
            Empleado existente = inactivoOpt.get();
            if (Boolean.TRUE.equals(existente.getActivo())) {
                throw new CodigoEmpleadoDuplicadoException("Ya existe un empleado activo con el código: " + request.getCodigo());
            }

            // El DNI (si se envió) no puede pertenecer a otra cuenta distinta del empleado que se reactiva
            validarDniUnico(request.getDni(), existente.getId());

            log.info("Reactivando empleado inactivo con ID: {}", existente.getId());

            EmpleadoUpdate update = new EmpleadoUpdate();
            update.setNombre(request.getNombre());
            update.setApellido(request.getApellido());
            update.setDni(request.getDni());
            update.setDireccion(request.getDireccion());
            update.setTelefono(request.getTelefono());
            update.setNombreUsuario(request.getNombreUsuario());
            if (request.getClave() != null && !request.getClave().isBlank()) {
                update.setClave(passwordEncoder.encode(request.getClave()));
            }
            update.setCodigo(request.getCodigo());
            update.setEspecialidad(request.getEspecialidad());

            empleadoRepository.updateEntity(existente, update);
            existente.setActivo(true);

            Empleado reactivado = empleadoRepository.save(existente);
            return empleadoRepository.fromEntity(reactivado);
        }

        // 3. Validar que el DNI (si se envió) no pertenezca a ninguna otra cuenta del sistema
        validarDniUnico(request.getDni(), null);

        // 4. Si no existe, se crea una nueva entidad
        Empleado empleado = empleadoRepository.toEntity(request);
        empleado.setClave(passwordEncoder.encode(request.getClave()));
        empleado.setActivo(true);

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado registrado exitosamente con ID: {}", guardado.getId());

        return empleadoRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse buscarPorId(Integer id) {
        log.info("Buscando empleado activo por ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);
        return empleadoRepository.fromEntity(empleado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodos() {
        log.info("Obteniendo listado de todos los empleados activos.");
        return empleadoRepository.findAllByActivoTrue().stream()
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodosIncluyendoInactivos() {
        log.info("Obteniendo listado completo de empleados (activos e inactivos).");
        return empleadoRepository.findAllIncludingInactive().stream()
                .map(empleadoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoUpdate update) {
        log.info("Actualizando información del empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        if (update.getCodigo() != null && !update.getCodigo().equalsIgnoreCase(empleado.getCodigo())) {
            validarCodigoUnico(update.getCodigo(), id);
        }

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equalsIgnoreCase(empleado.getNombreUsuario())) {
            validarNombreUsuarioUnico(update.getNombreUsuario(), id);
        }

        if (update.getDni() != null && !update.getDni().equalsIgnoreCase(empleado.getDni())) {
            validarDniUnico(update.getDni(), id);
        }

        empleadoRepository.updateEntity(empleado, update);

        if (update.getClave() != null && !update.getClave().isBlank()) {
            empleado.setClave(passwordEncoder.encode(update.getClave()));
        }

        Empleado actualizado = empleadoRepository.save(empleado);
        log.info("Empleado con ID: {} actualizado correctamente.", actualizado.getId());

        return empleadoRepository.fromEntity(actualizado);
    }

    /**
     * {@inheritDoc}
     * Propaga la desactivación en cascada a sus asignaciones de zona (AsignacionEmpleadoZona)
     * y libera los vehículos que tenía a cargo.
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Ejecutando borrado lógico para empleado con ID: {}", id);
        Empleado empleado = obtenerEmpleadoActivoPorId(id);

        // Liberar los vehículos que tenía a cargo, para que no queden apuntando a un empleado inactivo
        List<Vehiculo> vehiculosACargo = vehiculoRepository.findAllByActivoTrue().stream()
                .filter(v -> v.getEmpleado() != null && v.getEmpleado().getId().equals(id))
                .collect(Collectors.toList());
        vehiculosACargo.forEach(v -> v.setEmpleado(null));
        vehiculoRepository.saveAll(vehiculosACargo);

        // Desactivación lógica del empleado
        empleado.setActivo(false);
        empleadoRepository.save(empleado);

        // Propagación en cascada: Desactivar asignaciones asociadas al empleado
        List<AsignacionEmpleadoZona> asignaciones = asignacionEmpleadoZonaRepository.findAll().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId().equals(id)
                        && Boolean.TRUE.equals(asig.getActivo()))
                .collect(Collectors.toList());

        for (AsignacionEmpleadoZona asig : asignaciones) {
            asig.setActivo(false);
            asignacionEmpleadoZonaRepository.save(asig);
            log.info("Propagación de borrado lógico a AsignacionEmpleadoZona ID: {}", asig.getId());
        }

        log.info("Borrado lógico completado para empleado ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarZonasAsignadas(Integer empleadoId) {
        log.info("Obteniendo zonas asignadas para el empleado ID: {}", empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);

        return asignacionEmpleadoZonaRepository.findAllByActivoTrue().stream()
                .filter(asig -> asig.getEmpleado() != null
                        && asig.getEmpleado().getId().equals(empleadoId))
                .map(asignacionEmpleadoZonaRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarVehiculosBajoResponsabilidad(Integer empleadoId) {
        log.info("Obteniendo vehículos a cargo del empleado ID: {}", empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);

        return vehiculoRepository.findAllByActivoTrue().stream()
                .filter(v -> v.getEmpleado() != null && v.getEmpleado().getId().equals(empleadoId))
                .map(vehiculoRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public VehiculoResponse asignarVehiculo(Integer empleadoId, Integer vehiculoId) {
        log.info("Asignando el vehículo ID {} al empleado ID {}", vehiculoId, empleadoId);
        Empleado empleado = obtenerEmpleadoActivoPorId(empleadoId);
        Vehiculo vehiculo = obtenerVehiculoActivoPorId(vehiculoId);

        // 1. El vehículo pertenece a la zona del garage que ocupa
        Integer zonaId = zonaPorVehiculo().get(vehiculoId);
        if (zonaId == null) {
            throw new BusinessException("El vehículo no está asignado a ningún garage, por lo que no pertenece a ninguna zona.", HttpStatus.CONFLICT);
        }

        // 2. El empleado debe estar asignado a esa zona
        AsignacionEmpleadoZona asignacionZona = asignacionEmpleadoZonaRepository.findAllByEmpleadoIdAndActivoTrue(empleadoId).stream()
                .filter(a -> a.getZona() != null && a.getZona().getId().equals(zonaId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("El empleado no está asignado a la zona donde se encuentra el vehículo.", HttpStatus.CONFLICT));

        // 3. Si ya estaba a su cargo no hay nada que cambiar
        if (vehiculo.getEmpleado() != null && vehiculo.getEmpleado().getId().equals(empleadoId)) {
            return vehiculoRepository.fromEntity(vehiculo);
        }

        // 4. No puede superar la cantidad de vehículos a cargo definida para la zona
        int asignados = contarVehiculosAsignadosEnZona(empleadoId, zonaId);
        if (asignados >= asignacionZona.getCantVehiculosACargo()) {
            throw new BusinessException("El empleado ya tiene asignados los " + asignacionZona.getCantVehiculosACargo()
                    + " vehículos que tiene a cargo en la zona " + asignacionZona.getZona().getLetra() + ".", HttpStatus.CONFLICT);
        }

        vehiculo.setEmpleado(empleado);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        log.info("Vehículo ID {} asignado al empleado ID {}", vehiculoId, empleadoId);

        return vehiculoRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void desasignarVehiculo(Integer empleadoId, Integer vehiculoId) {
        log.info("Quitando el vehículo ID {} del cargo del empleado ID {}", vehiculoId, empleadoId);
        obtenerEmpleadoActivoPorId(empleadoId);
        Vehiculo vehiculo = obtenerVehiculoActivoPorId(vehiculoId);

        if (vehiculo.getEmpleado() == null || !vehiculo.getEmpleado().getId().equals(empleadoId)) {
            throw new RegistroNoEncontradoException("El vehículo con ID " + vehiculoId + " no está a cargo del empleado con ID " + empleadoId + ".");
        }

        vehiculo.setEmpleado(null);
        vehiculoRepository.save(vehiculo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public int contarVehiculosAsignadosEnZona(Integer empleadoId, Integer zonaId) {
        return vehiculosDelEmpleadoEnZona(empleadoId, zonaId).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void liberarVehiculosEnZona(Integer empleadoId, Integer zonaId) {
        List<Vehiculo> vehiculos = vehiculosDelEmpleadoEnZona(empleadoId, zonaId);
        vehiculos.forEach(v -> v.setEmpleado(null));
        vehiculoRepository.saveAll(vehiculos);
        log.info("Se liberaron {} vehículo(s) del empleado ID {} en la zona ID {}", vehiculos.size(), empleadoId, zonaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse buscarMiPerfil(String nombreUsuario) {
        log.info("Consultando el perfil del empleado autenticado: {}", nombreUsuario);
        return empleadoRepository.fromEntity(obtenerEmpleadoAutenticado(nombreUsuario));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEmpleadoZonaResponse> listarMisZonas(String nombreUsuario) {
        return listarZonasAsignadas(obtenerEmpleadoAutenticado(nombreUsuario).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarMisVehiculos(String nombreUsuario) {
        return listarVehiculosBajoResponsabilidad(obtenerEmpleadoAutenticado(nombreUsuario).getId());
    }

    // --- Métodos Privados Auxiliares ---

    private Empleado obtenerEmpleadoActivoPorId(Integer id) {
        return empleadoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un empleado activo con el ID: " + id));
    }

    private Empleado obtenerEmpleadoAutenticado(String nombreUsuario) {
        return empleadoRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el empleado autenticado."));
    }

    private Vehiculo obtenerVehiculoActivoPorId(Integer id) {
        return vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró un vehículo activo con el ID: " + id));
    }

    /**
     * Arma un mapa vehículoId -> zonaId a partir de las asignaciones vehículo-garage activas.
     */
    private Map<Integer, Integer> zonaPorVehiculo() {
        Map<Integer, Integer> zonaPorVehiculo = new HashMap<>();
        asignacionVehiculoGarageRepository.findAllByActivoTrue().forEach(a -> {
            if (a.getVehiculo() != null && a.getGarage() != null && a.getGarage().getZona() != null) {
                zonaPorVehiculo.put(a.getVehiculo().getId(), a.getGarage().getZona().getId());
            }
        });
        return zonaPorVehiculo;
    }

    private List<Vehiculo> vehiculosDelEmpleadoEnZona(Integer empleadoId, Integer zonaId) {
        Map<Integer, Integer> zonaPorVehiculo = zonaPorVehiculo();
        return vehiculoRepository.findAllByActivoTrue().stream()
                .filter(v -> v.getEmpleado() != null
                        && v.getEmpleado().getId().equals(empleadoId)
                        && zonaId.equals(zonaPorVehiculo.get(v.getId())))
                .collect(Collectors.toList());
    }

    private void validarCodigoUnico(String codigo, Integer idExcluir) {
        boolean existe = empleadoRepository.findAllIncludingInactive().stream()
                .anyMatch(e -> e.getCodigo() != null
                        && e.getCodigo().equalsIgnoreCase(codigo)
                        && Boolean.TRUE.equals(e.getActivo())
                        && (idExcluir == null || !e.getId().equals(idExcluir)));
        if (existe) {
            throw new CodigoEmpleadoDuplicadoException("Ya existe un empleado registrado con el código: " + codigo);
        }
    }

    private void validarNombreUsuarioUnico(String nombreUsuario, Integer idExcluir) {
        Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(nombreUsuario);
        if (duenoOpt.isPresent() && (idExcluir == null || !duenoOpt.get().equals(idExcluir))) {
            throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + nombreUsuario + "' ya se encuentra registrado.");
        }
    }

    /**
     * Valida que el DNI no pertenezca a ninguna otra cuenta del sistema (activa o inactiva, de cualquier tipo).
     * El DNI es opcional en el empleado: si es nulo o está en blanco, no se valida.
     *
     * @param dni       DNI a validar (puede ser nulo).
     * @param idExcluir ID de la cuenta que se está modificando o reactivando; null en un alta nueva.
     * @throws DniDuplicadoException Si el DNI ya pertenece a otra cuenta.
     */
    private void validarDniUnico(String dni, Integer idExcluir) {
        if (dni == null || dni.isBlank()) {
            return;
        }
        Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorDniIncluyendoInactivos(dni);
        if (duenoOpt.isPresent() && (idExcluir == null || !duenoOpt.get().equals(idExcluir))) {
            throw new DniDuplicadoException("El DNI " + dni + " ya se encuentra registrado en otra cuenta.");
        }
    }
}
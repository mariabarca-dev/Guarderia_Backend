package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaOcupadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.GarageOcupadoResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de asignaciones entre Vehículos y Garajes.
 * Garantiza la integridad referencial, el borrado lógico y el cumplimiento de las reglas de dominio:
 * un garage aloja un solo vehículo, un vehículo ocupa un solo garage, el tipo del vehículo debe coincidir
 * con el de la zona, el vehículo debe entrar en las dimensiones de los garages de la zona y la zona no
 * puede superar su capacidad. Las mismas reglas se aplican al crear y al modificar una asignación.
 * Cuando un vehículo deja el garage o cambia de zona, deja de estar a cargo del empleado que lo atendía.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionVehiculoGarageServiceImpl implements AsignacionVehiculoGarageService {

    private final AsignacionVehiculoGarageRepository asignacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final GarageRepository garageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AsignacionVehiculoGarageResponse crearAsignacion(AsignacionVehiculoGarageRequest request) {
        log.info("Iniciando solicitud de asignación -> Vehículo ID: {}, Garaje ID: {}",
                request.getVehiculoId(), request.getGarageId());

        if (request.getVehiculoId() == null || request.getGarageId() == null) {
            log.error("Error al crear asignación: Identificadores de vehículo y garaje obligatorios");
            throw new BusinessException("Error: El vehículo y el garaje son obligatorios.", HttpStatus.BAD_REQUEST);
        }

        // 1. Recuperar y verificar que las entidades existen y están activas
        Vehiculo vehiculo = buscarVehiculoActivo(request.getVehiculoId());
        Garage garage = buscarGarageActivo(request.getGarageId());

        // 2. REGLAS DE NEGOCIO: garage libre y vehículo sin otra asignación
        validarGarageLibre(garage);
        validarVehiculoSinAsignacion(vehiculo);

        // 3. REGLAS DE NEGOCIO: el vehículo debe encajar en la zona del garaje (tipo y dimensiones)
        validarEncajeEnZona(vehiculo, garage);

        // 4. REGLA DE NEGOCIO: capacidad máxima de la zona
        validarCapacidadDeZona(garage.getZona());

        // 5. Mapeo y persistencia
        AsignacionVehiculoGarage nuevaAsignacion = asignacionRepository.toEntity(request);
        nuevaAsignacion.setVehiculo(vehiculo);
        nuevaAsignacion.setGarage(garage);
        nuevaAsignacion.setActivo(true);

        AsignacionVehiculoGarage guardada = asignacionRepository.save(nuevaAsignacion);
        log.info("Asignación de vehículo a garaje creada con éxito. ID: {}", guardada.getId());

        return asignacionRepository.fromEntity(guardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionVehiculoGarageResponse> listarTodas() {
        log.info("Consultando todas las asignaciones activas de vehículo a garaje");
        return asignacionRepository.findAll().stream()
                .filter(AsignacionVehiculoGarage::getActivo)
                .map(asignacionRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsignacionVehiculoGarageResponse> listarTodasIncluyendoInactivas() {
        log.info("Consultando todas las asignaciones incluyendo las inactivas para auditoría");
        return asignacionRepository.findAllIncludingInactive().stream()
                .map(asignacionRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AsignacionVehiculoGarageResponse buscarPorId(Integer id) {
        log.info("Buscando asignación activa ID: {}", id);
        AsignacionVehiculoGarage asignacion = asignacionRepository.findById(id)
                .filter(AsignacionVehiculoGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se ha encontrado la asignación activa con ID: " + id));

        return asignacionRepository.fromEntity(asignacion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AsignacionVehiculoGarageResponse buscarPorGarage(Integer idGarage) {
        log.info("Buscando asignación activa para el garaje ID: {}", idGarage);
        AsignacionVehiculoGarage asignacion = asignacionRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getGarage() != null
                        && a.getGarage().getId().equals(idGarage))
                .findFirst()
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe ninguna asignación activa para el garaje ID: " + idGarage));

        return asignacionRepository.fromEntity(asignacion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AsignacionVehiculoGarageResponse buscarPorVehiculo(Integer idVehiculo) {
        log.info("Buscando asignación activa para el vehículo ID: {}", idVehiculo);
        AsignacionVehiculoGarage asignacion = asignacionRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getVehiculo() != null
                        && a.getVehiculo().getId().equals(idVehiculo))
                .findFirst()
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe ninguna asignación activa para el vehículo ID: " + idVehiculo));

        return asignacionRepository.fromEntity(asignacion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<GarageOcupadoResponse> listarGaragesOcupadosPorSocio(Integer socioId) {
        log.info("Consultando los garages ocupados por los vehículos del socio ID: {}", socioId);
        return asignacionRepository.findAllByActivoTrue().stream()
                .filter(a -> a.getVehiculo() != null
                        && a.getVehiculo().getSocio() != null
                        && a.getVehiculo().getSocio().getId().equals(socioId)
                        && a.getGarage() != null)
                .map(a -> new GarageOcupadoResponse(
                        a.getId(),
                        a.getVehiculo().getId(),
                        garageRepository.fromEntity(a.getGarage()),
                        a.getFechaAsignacionGarage()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Solo se validan los cambios reales: si cambia el vehículo, no debe tener otra asignación; si cambia el
     * garage, debe estar libre y, si está en otra zona, esa zona debe tener capacidad. Si cambia cualquiera de
     * los dos, el vehículo debe encajar en la zona del garage (tipo y dimensiones). Si el vehículo cambia o el
     * garage nuevo está en otra zona, el vehículo anterior deja de estar a cargo del empleado que lo atendía.
     */
    @Override
    @Transactional
    public AsignacionVehiculoGarageResponse actualizarAsignacion(Integer id, AsignacionVehiculoGarageUpdate update) {
        log.info("Actualizando asignación ID: {}", id);

        AsignacionVehiculoGarage asignacionExistente = asignacionRepository.findById(id)
                .filter(AsignacionVehiculoGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe la asignación activa a actualizar con ID: " + id));

        // Se guarda lo anterior antes de aplicar los cambios
        Vehiculo vehiculoAnterior = asignacionExistente.getVehiculo();
        Garage garageAnterior = asignacionExistente.getGarage();
        Integer vehiculoAnteriorId = vehiculoAnterior != null ? vehiculoAnterior.getId() : null;
        Integer garageAnteriorId = garageAnterior != null ? garageAnterior.getId() : null;
        Integer zonaAnteriorId = obtenerZonaId(garageAnterior);

        boolean cambiaVehiculo = update.getVehiculoId() != null && !update.getVehiculoId().equals(vehiculoAnteriorId);
        boolean cambiaGarage = update.getGarageId() != null && !update.getGarageId().equals(garageAnteriorId);

        // Vehículo y garage que quedarían tras el cambio (los actuales si no se modifican)
        Vehiculo vehiculoDestino = cambiaVehiculo ? buscarVehiculoActivo(update.getVehiculoId()) : vehiculoAnterior;
        Garage garageDestino = cambiaGarage ? buscarGarageActivo(update.getGarageId()) : garageAnterior;
        Integer zonaNuevaId = obtenerZonaId(garageDestino);

        if (cambiaVehiculo) {
            validarVehiculoSinAsignacion(vehiculoDestino);
        }
        if (cambiaGarage) {
            validarGarageLibre(garageDestino);
        }
        if (cambiaVehiculo || cambiaGarage) {
            validarEncajeEnZona(vehiculoDestino, garageDestino);
        }
        if (cambiaGarage && !Objects.equals(zonaAnteriorId, zonaNuevaId)) {
            validarCapacidadDeZona(garageDestino.getZona());
        }

        asignacionRepository.updateEntity(asignacionExistente, update);
        AsignacionVehiculoGarage actualizada = asignacionRepository.save(asignacionExistente);

        if (cambiaVehiculo || !Objects.equals(zonaAnteriorId, zonaNuevaId)) {
            liberarEmpleadoDelVehiculo(vehiculoAnterior);
        }

        log.info("Asignación ID: {} actualizada con éxito", id);
        return asignacionRepository.fromEntity(actualizada);
    }

    /**
     * {@inheritDoc}
     * El vehículo queda sin garage, por lo que también deja de estar a cargo de un empleado.
     */
    @Override
    @Transactional
    public void eliminarAsignacion(Integer id) {
        log.info("Ejecutando el borrado lógico de la asignación ID: {}", id);

        AsignacionVehiculoGarage asignacion = asignacionRepository.findById(id)
                .filter(AsignacionVehiculoGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se ha encontrado la asignación activa a eliminar con ID: " + id));

        asignacion.setActivo(false);
        asignacionRepository.save(asignacion);

        liberarEmpleadoDelVehiculo(asignacion.getVehiculo());

        log.info("Borrado lógico finalizado con éxito para la asignación ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void validarVehiculoAsignadoSigueEncajando(Integer vehiculoId) {
        asignacionRepository.findAllByVehiculoIdAndActivoTrue(vehiculoId)
                .forEach(a -> validarEncajeEnZona(a.getVehiculo(), a.getGarage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void validarVehiculosAsignadosEnZonaSiguenEncajando(Integer zonaId) {
        asignacionRepository.findAllByActivoTrue().stream()
                .filter(a -> a.getGarage() != null
                        && a.getGarage().getZona() != null
                        && a.getGarage().getZona().getId().equals(zonaId))
                .forEach(a -> validarEncaje(a.getVehiculo(), a.getGarage().getZona()));
    }

    /**
     * Recupera un vehículo activo o lanza la excepción de registro no encontrado.
     */
    private Vehiculo buscarVehiculoActivo(Integer vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .filter(Vehiculo::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El vehículo especificado no existe o se encuentra inactivo."));
    }

    /**
     * Recupera un garage activo o lanza la excepción de registro no encontrado.
     */
    private Garage buscarGarageActivo(Integer garageId) {
        return garageRepository.findById(garageId)
                .filter(Garage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El garaje especificado no existe o se encuentra inactivo."));
    }

    /**
     * Valida que el garage no tenga ya una asignación activa.
     *
     * @throws GarageYaOcupadoException Si el garage está ocupado por otro vehículo.
     */
    private void validarGarageLibre(Garage garage) {
        boolean garageOcupado = asignacionRepository.findAllByActivoTrue().stream()
                .anyMatch(a -> a.getGarage() != null && a.getGarage().getId().equals(garage.getId()));

        if (garageOcupado) {
            log.warn("Intento de asignar garaje ocupado ID: {}", garage.getId());
            throw new GarageYaOcupadoException("Error: El garaje N° " + garage.getNumeroGarage() + " ya se encuentra ocupado por otro vehículo.");
        }
    }

    /**
     * Valida que el vehículo no tenga ya otra asignación activa.
     *
     * @throws BusinessException Si el vehículo ya está asignado a un garage (409).
     */
    private void validarVehiculoSinAsignacion(Vehiculo vehiculo) {
        boolean vehiculoYaAsignado = !asignacionRepository.findAllByVehiculoIdAndActivoTrue(vehiculo.getId()).isEmpty();

        if (vehiculoYaAsignado) {
            log.warn("El vehículo ID: {} ya dispone de una asignación activa en el sistema", vehiculo.getId());
            throw new BusinessException("Error de negocio: El vehículo con matrícula " + vehiculo.getMatricula()
                    + " ya está asignado a un garaje en el sistema.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Valida que el vehículo encaje en la zona del garage. Exige que el garage tenga zona y delega el
     * control de tipo y dimensiones en {@link #validarEncaje(Vehiculo, Zona)}.
     *
     * @throws BusinessException Si el garage no tiene zona, el tipo no coincide o el vehículo no entra.
     */
    private void validarEncajeEnZona(Vehiculo vehiculo, Garage garage) {
        Zona zona = garage.getZona();
        if (zona == null) {
            log.error("El garaje ID {} no tiene ninguna zona asociada", garage.getId());
            throw new BusinessException("Error de negocio: El garaje especificado no está asociado a ninguna zona.", HttpStatus.BAD_REQUEST);
        }
        validarEncaje(vehiculo, zona);
    }

    /**
     * Valida que el tipo del vehículo coincida con el de la zona y que su ancho y profundidad no superen los
     * de los garages de la zona (si miden lo mismo, entra).
     *
     * @throws BusinessException Si el tipo no coincide o el vehículo no entra en los garages de la zona.
     */
    private void validarEncaje(Vehiculo vehiculo, Zona zona) {
        if (vehiculo.getTipo() != zona.getTipoVehiculo()) {
            log.warn("Incompatibilidad de tipo -> Vehículo: {}, Zona: {}", vehiculo.getTipo(), zona.getTipoVehiculo());
            throw new BusinessException("Error de negocio: El vehículo de tipo " + vehiculo.getTipo()
                    + " no es compatible con la zona asignada de " + zona.getTipoVehiculo() + ".", HttpStatus.BAD_REQUEST);
        }

        if (vehiculo.getAncho() > zona.getAnchoGarage() || vehiculo.getProfundidad() > zona.getLargoGarage()) {
            log.warn("El vehículo ID {} no entra en los garages de la zona ID {}", vehiculo.getId(), zona.getId());
            throw new BusinessException("Error de negocio: El vehículo con matrícula " + vehiculo.getMatricula()
                    + " (ancho " + vehiculo.getAncho() + ", profundidad " + vehiculo.getProfundidad()
                    + ") no entra en los garages de la zona '" + zona.getLetra()
                    + "' (ancho " + zona.getAnchoGarage() + ", profundidad " + zona.getLargoGarage() + ").",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Valida que la zona tenga lugar para un vehículo más.
     *
     * @throws ZonaSinCapacidadException Si la zona alcanzó su capacidad máxima.
     */
    private void validarCapacidadDeZona(Zona zona) {
        int capacidadMaxima = zona.getCapacidadVehiculos();
        int vehiculosActuales = contarVehiculosActivosEnZona(zona);

        if (vehiculosActuales >= capacidadMaxima) {
            log.error("Capacidad de zona superada en la zona ID {}. Límite: {}, Actuales: {}",
                    zona.getId(), capacidadMaxima, vehiculosActuales);
            throw new ZonaSinCapacidadException("Error: La zona '" + zona.getLetra()
                    + "' ha alcanzado su capacidad máxima permitida de " + capacidadMaxima + " vehículos.");
        }
    }

    /**
     * Cuenta la cantidad de vehículos actualmente asignados y activos en la zona suministrada.
     */
    private int contarVehiculosActivosEnZona(Zona zona) {
        return (int) asignacionRepository.findAllByActivoTrue().stream()
                .filter(a -> a.getGarage() != null
                        && a.getGarage().getZona() != null
                        && a.getGarage().getZona().getId().equals(zona.getId()))
                .count();
    }

    /**
     * Devuelve el ID de la zona de un garage, o null si el garage o su zona no existen.
     */
    private Integer obtenerZonaId(Garage garage) {
        return (garage != null && garage.getZona() != null) ? garage.getZona().getId() : null;
    }

    /**
     * Deja al vehículo sin empleado a cargo, si lo tenía.
     */
    private void liberarEmpleadoDelVehiculo(Vehiculo vehiculo) {
        if (vehiculo != null && vehiculo.getEmpleado() != null) {
            vehiculo.setEmpleado(null);
            vehiculoRepository.save(vehiculo);
            log.info("El vehículo ID {} dejó de estar a cargo de su empleado.", vehiculo.getId());
        }
    }
}
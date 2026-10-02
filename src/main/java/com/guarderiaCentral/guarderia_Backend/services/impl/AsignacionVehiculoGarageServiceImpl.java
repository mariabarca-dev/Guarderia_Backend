package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaOcupadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.ZonaSinCapacidadException;
import com.guarderiaCentral.guarderia_Backend.modelos.AsignacionVehiculoGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.PropiedadGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Vehiculo;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages.AsignacionVehiculoGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.vehiculos.VehiculoRepository;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de asignaciones entre Vehículos y Garajes.
 * Garantiza la integridad referencial, el borrado lógico y el cumplimiento de las reglas de dominio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionVehiculoGarageServiceImpl implements AsignacionVehiculoGarageService {

    private final AsignacionVehiculoGarageRepository asignacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final GarageRepository garageRepository;
    private final PropiedadGarageRepository propiedadGarageRepository;

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
        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .filter(Vehiculo::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El vehículo especificado no existe o se encuentra inactivo."));

        Garage garage = garageRepository.findById(request.getGarageId())
                .filter(Garage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El garaje especificado no existe o se encuentra inactivo."));

        // 2. REGLA DE NEGOCIO: Excepción específica si el garaje ya está ocupado
        boolean garageOcupado = asignacionRepository.findAll().stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getGarage() != null
                        && a.getGarage().getId().equals(garage.getId()));

        if (garageOcupado) {
            log.warn("Intento de asignar garaje ocupado ID: {}", garage.getId());
            throw new GarageYaOcupadoException("Error: El garaje N° " + garage.getId() + " ya se encuentra ocupado por otro vehículo.");
        }

        // 3. REGLA DE NEGOCIO: El vehículo no puede tener otra asignación activa
        boolean vehiculoYaAsignado = asignacionRepository.findAll().stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getVehiculo() != null
                        && a.getVehiculo().getId().equals(vehiculo.getId()));

        if (vehiculoYaAsignado) {
            log.warn("El vehículo ID: {} ya dispone de una asignación activa en el sistema", vehiculo.getId());
            throw new BusinessException("Error de negocio: El vehículo con matrícula " + vehiculo.getMatricula()
                    + " ya está asignado a un garaje en el sistema.", HttpStatus.CONFLICT);
        }

        // 4. REGLA DE NEGOCIO: El vehículo debe pertenecer al socio propietario del garaje (vía PropiedadGarage)
        Optional<PropiedadGarage> propiedadOpt = propiedadGarageRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo())
                        && p.getGarage() != null
                        && p.getGarage().getId().equals(garage.getId()))
                .findFirst();

        if (propiedadOpt.isPresent()) {
            PropiedadGarage propiedad = propiedadOpt.get();
            if (vehiculo.getSocio() == null || !vehiculo.getSocio().getId().equals(propiedad.getSocio().getId())) {
                log.warn("Conflicto de titularidad: Vehículo ID {} no pertenece al propietario del garaje ID {}",
                        vehiculo.getId(), garage.getId());
                throw new BusinessException("Error de negocio: El vehículo no pertenece al socio propietario de este garaje.", HttpStatus.FORBIDDEN);
            }
        }

        // 5. REGLA DE NEGOCIO: Compatibilidad de tipo de vehículo con el tipo permitido en la Zona
        Zona zona = garage.getZona();
        if (zona == null) {
            log.error("El garaje ID {} no tiene ninguna zona asociada", garage.getId());
            throw new BusinessException("Error de negocio: El garaje especificado no está asociado a ninguna zona.", HttpStatus.BAD_REQUEST);
        }

        if (vehiculo.getTipo() != zona.getTipoVehiculo()) {
            log.warn("Incompatibilidad de tipo -> Vehículo: {}, Zona: {}", vehiculo.getTipo(), zona.getTipoVehiculo());
            throw new BusinessException("Error de negocio: El vehículo de tipo " + vehiculo.getTipo()
                    + " no es compatible con la zona asignada de " + zona.getTipoVehiculo() + ".", HttpStatus.BAD_REQUEST);
        }

        // 6. REGLA DE NEGOCIO: Excepción específica de capacidad máxima en la Zona
        int capacidadMaxima = zona.getCapacidadVehiculos();
        int vehiculosActuales = contarVehiculosActivosEnZona(zona);

        if (vehiculosActuales >= capacidadMaxima) {
            log.error("Capacidad de zona superada en la zona ID {}. Límite: {}, Actuales: {}",
                    zona.getId(), capacidadMaxima, vehiculosActuales);
            throw new ZonaSinCapacidadException("Error: La zona '" + zona.getLetra()
                    + "' ha alcanzado su capacidad máxima permitida de " + capacidadMaxima + " vehículos.");
        }

        // 7. Mapeo y persistencia
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
    @Transactional
    public AsignacionVehiculoGarageResponse actualizarAsignacion(Integer id, AsignacionVehiculoGarageUpdate update) {
        log.info("Actualizando asignación ID: {}", id);

        AsignacionVehiculoGarage asignacionExistente = asignacionRepository.findById(id)
                .filter(AsignacionVehiculoGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe la asignación activa a actualizar con ID: " + id));

        asignacionRepository.updateEntity(asignacionExistente, update);
        AsignacionVehiculoGarage actualizada = asignacionRepository.save(asignacionExistente);

        log.info("Asignación ID: {} actualizada con éxito", id);
        return asignacionRepository.fromEntity(actualizada);
    }

    /**
     * {@inheritDoc}
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

        log.info("Borrado lógico finalizado con éxito para la asignación ID: {}", id);
    }

    /**
     * Cuenta la cantidad de vehículos actualmente asignados y activos en la zona suministrada.
     */
    private int contarVehiculosActivosEnZona(Zona zona) {
        return (int) asignacionRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo())
                        && a.getGarage() != null
                        && a.getGarage().getZona() != null
                        && a.getGarage().getZona().getId().equals(zona.getId()))
                .count();
    }
}
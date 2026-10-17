package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaVendidoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.PropiedadGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.socios.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de propiedades de garage.
 * Un garage pertenece a un solo socio y su fecha de compra no puede ser anterior al ingreso del socio:
 * ambas reglas se aplican tanto al registrar como al modificar una propiedad.
 *
 * @author Cátedra
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropiedadGarageServiceImpl implements PropiedadGarageService {

    private final PropiedadGarageRepository propiedadGarageRepository;
    private final SocioRepository socioRepository;
    private final GarageRepository garageRepository;

    @Override
    @Transactional
    public PropiedadGarageResponse registrarPropiedad(PropiedadGarageRequest request) {
        log.info("Iniciando registro de propiedad para Socio ID: {} y Garage ID: {}", request.getSocioId(), request.getGarageId());

        Socio socio = socioRepository.findById(request.getSocioId())
                .filter(Socio::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El socio especificado no existe o está inactivo en el sistema."));

        Garage garage = garageRepository.findById(request.getGarageId())
                .filter(Garage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El garage especificado no existe o está inactivo en el sistema."));

        boolean yaVendido = propiedadGarageRepository.findAll().stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getActivo())
                        && p.getGarage() != null
                        && p.getGarage().getId().equals(garage.getId()));

        if (yaVendido) {
            log.warn("Intento fallido de compra: El garage N° {} ya tiene un socio propietario asignado.", garage.getNumeroGarage());
            throw new GarageYaVendidoException("Error: El garaje N° " + garage.getNumeroGarage() + " ya tiene un socio propietario asignado.");
        }

        validarFechaCompraPosteriorAlIngreso(request.getFechaCompraGarage(), socio);

        PropiedadGarage nuevaPropiedad = propiedadGarageRepository.toEntity(request);
        nuevaPropiedad.setSocio(socio);
        nuevaPropiedad.setGarage(garage);
        nuevaPropiedad.setActivo(true);

        PropiedadGarage guardada = propiedadGarageRepository.save(nuevaPropiedad);
        log.info("Propiedad registrada exitosamente con ID: {}", guardada.getId());

        return propiedadGarageRepository.fromEntity(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public PropiedadGarageResponse obtenerPorId(Integer id) {
        log.debug("Buscando propiedad de garage con ID: {}", id);
        PropiedadGarage propiedad = propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("Propiedad de garage no encontrada con ID: " + id));
        return propiedadGarageRepository.fromEntity(propiedad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarTodas() {
        log.debug("Listando todas las propiedades de garage activas");
        return propiedadGarageRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(propiedadGarageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarTodasIncluyendoInactivas() {
        log.debug("Listando todas las propiedades de garage (incluyendo inactivas)");
        return propiedadGarageRepository.findAllIncludingInactive().stream()
                .map(propiedadGarageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarPorSocio(Integer socioId) {
        log.debug("Listando propiedades para el Socio ID: {}", socioId);

        if (!socioRepository.existsById(socioId)) {
            throw new RegistroNoEncontradoException("El socio especificado con ID " + socioId + " no existe.");
        }

        return propiedadGarageRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo())
                        && p.getSocio() != null
                        && p.getSocio().getId().equals(socioId))
                .map(propiedadGarageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Si el garage indicado ya pertenece a otra propiedad activa, la modificación se rechaza para que un garage
     * nunca tenga dos propietarios. La propiedad que se está modificando no cuenta como "otra". La fecha de compra
     * que se valida contra el ingreso del socio es la nueva o, si no se envía, la que ya tenía la propiedad.
     */
    @Override
    @Transactional
    public PropiedadGarageResponse actualizar(Integer id, PropiedadGarageUpdate update) {
        log.info("Actualizando propiedad de garage con ID: {}", id);

        PropiedadGarage propiedadExistente = propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró la propiedad de garage especificada."));

        Socio socio = socioRepository.findById(update.getSocioId())
                .filter(Socio::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El socio especificado no existe o está inactivo."));

        Garage garage = garageRepository.findById(update.getGarageId())
                .filter(Garage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El garage especificado no existe o está inactivo."));

        boolean garageConOtroPropietario = propiedadGarageRepository.findAll().stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getActivo())
                        && !p.getId().equals(id)
                        && p.getGarage() != null
                        && p.getGarage().getId().equals(garage.getId()));

        if (garageConOtroPropietario) {
            log.warn("Intento fallido de modificar la propiedad ID {}: el garage N° {} ya tiene otro propietario.", id, garage.getNumeroGarage());
            throw new GarageYaVendidoException("Error: El garaje N° " + garage.getNumeroGarage() + " ya tiene un socio propietario asignado.");
        }

        LocalDate fechaCompraResultante = update.getFechaCompraGarage() != null
                ? update.getFechaCompraGarage()
                : propiedadExistente.getFechaCompraGarage();
        validarFechaCompraPosteriorAlIngreso(fechaCompraResultante, socio);

        propiedadGarageRepository.updateEntity(propiedadExistente, update);
        propiedadExistente.setSocio(socio);
        propiedadExistente.setGarage(garage);

        PropiedadGarage actualizada = propiedadGarageRepository.save(propiedadExistente);
        log.info("Propiedad de garage actualizada exitosamente con ID: {}", actualizada.getId());

        return propiedadGarageRepository.fromEntity(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Realizando borrado lógico de la propiedad de garage con ID: {}", id);

        PropiedadGarage propiedad = propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("Propiedad de garage no encontrada con ID: " + id));

        propiedad.setActivo(false);
        propiedadGarageRepository.save(propiedad);
        log.info("Borrado lógico completado para propiedad con ID: {}", id);
    }

    /**
     * Valida que la fecha de compra no sea anterior a la fecha de ingreso del socio. Se usa tanto al registrar
     * como al modificar una propiedad.
     *
     * @param fechaCompra Fecha de compra del garage (puede ser null, en cuyo caso no se valida).
     * @param socio       Socio propietario.
     * @throws BusinessException Si la fecha de compra es anterior al ingreso del socio (400).
     */
    private void validarFechaCompraPosteriorAlIngreso(LocalDate fechaCompra, Socio socio) {
        if (fechaCompra != null && socio.getFechaIngreso() != null && fechaCompra.isBefore(socio.getFechaIngreso())) {
            throw new BusinessException("Error de negocio: La fecha de compra (" + fechaCompra
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").", HttpStatus.BAD_REQUEST);
        }
    }
}
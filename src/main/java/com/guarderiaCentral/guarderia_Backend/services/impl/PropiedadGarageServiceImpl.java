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

        LocalDate fechaCompra = request.getFechaCompraGarage();
        if (socio.getFechaIngreso() != null && fechaCompra.isBefore(socio.getFechaIngreso())) {
            throw new BusinessException("Error de negocio: La fecha de compra (" + fechaCompra
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").", HttpStatus.BAD_REQUEST);
        }

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

        if (update.getFechaCompraGarage() != null && socio.getFechaIngreso() != null && update.getFechaCompraGarage().isBefore(socio.getFechaIngreso())) {
            throw new BusinessException("Error de negocio: La fecha de compra (" + update.getFechaCompraGarage()
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").", HttpStatus.BAD_REQUEST);
        }

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
}
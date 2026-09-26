package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.GarageYaVendidoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Garage;
import com.guarderiaCentral.guarderia_Backend.modelos.PropiedadGarage;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.PropiedadGarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.PropiedadGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.PropiedadGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
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
 * Implementación del servicio de gestión de propiedades de garage ({@link PropiedadGarageService}).
 * Maneja las transacciones y aplica de manera estricta las reglas de negocio del dominio:
 * - Un garage no puede venderse más de una vez mientras mantenga una propiedad activa.
 * - La fecha de compra del garage no puede ser anterior a la fecha de alta/ingreso del socio.
 * - Soporte nativo para borrado lógico y mapeo de datos.
 *
 * @author Cátedra
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropiedadGarageServiceImpl implements PropiedadGarageService {

    private final PropiedadGarageRepository propiedadGarageRepository;
    private final SocioRepository socioRepository;
    private final GarageRepository garageRepository;

    /**
     * Registra la compra de un garage por un socio comprobando la existencia de las entidades,
     * la disponibilidad previa del garage y la coherencia cronológica de la transacción.
     *
     * @param request DTO con la información requerida para registrar la propiedad.
     * @return {@link PropiedadGarageResponse} formateado de la entidad creada.
     * @throws RegistroNoEncontradoException Si el socio o garage no existen o están inactivos.
     * @throws GarageYaVendidoException Si el garage ya fue vendido a otro o al mismo socio.
     * @throws BusinessException Si la fecha de compra es anterior a la fecha de ingreso del socio.
     */
    @Override
    public PropiedadGarageResponse registrarPropiedad(PropiedadGarageRequest request) {
        log.info("Iniciando registro de propiedad para Socio ID: {} y Garage ID: {}", request.getSocioId(), request.getGarageId());

        Socio socio = socioRepository.findById(request.getSocioId())
                .filter(Socio::getActivo)
                .orElseThrow(() -> {
                    log.error("Socio no encontrado con ID: {}", request.getSocioId());
                    return new RegistroNoEncontradoException("El socio especificado no existe o está inactivo en el sistema.");
                });

        Garage garage = garageRepository.findById(request.getGarageId())
                .filter(Garage::getActivo)
                .orElseThrow(() -> {
                    log.error("Garage no encontrado con ID: {}", request.getGarageId());
                    return new RegistroNoEncontradoException("El garage especificado no existe o está inactivo en el sistema.");
                });

        // REGLA DE NEGOCIO: Validar que el garage no tenga una propiedad activa
        boolean yaVendido = propiedadGarageRepository.findByGarageIdAndActivoTrue(garage.getId()).isPresent();
        if (yaVendido) {
            log.warn("Intento fallido de compra: El garage N° {} ya tiene un socio propietario asignado.", garage.getNumeroGarage());
            throw new GarageYaVendidoException("Error: El garaje N° " + garage.getNumeroGarage() + " ya tiene un socio propietario asignado.");
        }

        // REGLA DE NEGOCIO: La fecha de compra no puede ser anterior a la fecha de ingreso del socio
        LocalDate fechaCompra = request.getFechaCompra();
        if (socio.getFechaIngreso() != null && fechaCompra.isBefore(socio.getFechaIngreso())) {
            log.warn("Fecha de compra invalida: {} es anterior a la fecha de ingreso del socio: {}", fechaCompra, socio.getFechaIngreso());
            throw new BusinessException("Error de negocio: La fecha de compra (" + fechaCompra
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").", HttpStatus.BAD_REQUEST);
        }

        PropiedadGarage nuevaPropiedad = propiedadGarageRepository.toEntity(request, socio, garage);
        nuevaPropiedad.setActivo(true);

        PropiedadGarage guardada = propiedadGarageRepository.save(nuevaPropiedad);
        log.info("Propiedad registrada exitosamente con ID: {}", guardada.getId());

        return propiedadGarageRepository.fromEntity(guardada);
    }

    /**
     * Busca y retorna una propiedad por su ID.
     *
     * @param id Identificador de la propiedad.
     * @return DTO de respuesta con la propiedad hallada.
     * @throws RegistroNoEncontradoException Si no existe la propiedad.
     */
    @Override
    @Transactional(readOnly = true)
    public PropiedadGarageResponse obtenerPorId(int id) {
        log.debug("Buscando propiedad de garage con ID: {}", id);
        return propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .map(propiedadGarageRepository::fromEntity)
                .orElseThrow(() -> {
                    log.error("Propiedad de garage no encontrada con ID: {}", id);
                    return new RegistroNoEncontradoException("Propiedad de garage no encontrada con ID: " + id);
                });
    }

    /**
     * Devuelve el listado completo de las propiedades de garage activas.
     *
     * @return Lista de DTOs de respuesta.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarTodas() {
        log.debug("Listando todas las propiedades de garage activas");
        return propiedadGarageRepository.findAll().stream()
                .filter(PropiedadGarage::getActivo)
                .map(propiedadGarageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el listado de garages que pertenecen a un socio en particular.
     *
     * @param socioId Identificador del socio.
     * @return Lista de propiedades pertenecientes al socio.
     * @throws RegistroNoEncontradoException Si el socio no existe o está inactivo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PropiedadGarageResponse> listarPorSocio(int socioId) {
        log.debug("Listando propiedades para el Socio ID: {}", socioId);

        if (!socioRepository.existsById(socioId)) {
            log.error("Socio no encontrado con ID: {}", socioId);
            throw new RegistroNoEncontradoException("El socio especificado con ID " + socioId + " no existe.");
        }

        return propiedadGarageRepository.findBySocioIdAndActivoTrue(socioId).stream()
                .map(propiedadGarageRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los valores de la propiedad especificada.
     *
     * @param id Identificador de la propiedad a actualizar.
     * @param update DTO con las modificaciones solicitadas.
     * @return DTO de respuesta actualizado.
     * @throws RegistroNoEncontradoException Si la propiedad o las entidades relacionadas no existen.
     */
    @Override
    public PropiedadGarageResponse actualizar(int id, PropiedadGarageUpdate update) {
        log.info("Actualizando propiedad de garage con ID: {}", id);

        PropiedadGarage propiedadExistente = propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .orElseThrow(() -> {
                    log.error("Propiedad de garage no encontrada para actualizar con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró la propiedad de garage especificada.");
                });

        Socio socio = socioRepository.findById(update.getSocioId())
                .filter(Socio::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El socio especificado no existe o está inactivo."));

        Garage garage = garageRepository.findById(update.getGarageId())
                .filter(Garage::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("El garage especificado no existe o está inactivo."));

        if (socio.getFechaIngreso() != null && update.getFechaCompra().isBefore(socio.getFechaIngreso())) {
            throw new BusinessException("Error de negocio: La fecha de compra (" + update.getFechaCompra()
                    + ") no puede ser anterior a la fecha de ingreso del socio (" + socio.getFechaIngreso() + ").", HttpStatus.BAD_REQUEST);
        }

        propiedadGarageRepository.updateEntity(propiedadExistente, update, socio, garage);
        PropiedadGarage actualizada = propiedadGarageRepository.save(propiedadExistente);
        log.info("Propiedad de garage actualizada exitosamente con ID: {}", actualizada.getId());

        return propiedadGarageRepository.fromEntity(actualizada);
    }

    /**
     * Ejecuta el borrado lógico de la propiedad de garage marcando activo=false.
     *
     * @param id Identificador de la propiedad a dar de baja.
     * @throws RegistroNoEncontradoException Si el registro no se encuentra.
     */
    @Override
    public void eliminar(int id) {
        log.info("Realizando borrado lógico de la propiedad de garage con ID: {}", id);

        PropiedadGarage propiedad = propiedadGarageRepository.findById(id)
                .filter(PropiedadGarage::getActivo)
                .orElseThrow(() -> {
                    log.error("Propiedad de garage no encontrada con ID: {}", id);
                    return new RegistroNoEncontradoException("Propiedad de garage no encontrada con ID: " + id);
                });

        propiedad.setActivo(false);
        propiedadGarageRepository.save(propiedad);
        log.info("Borrado lógico completado para propiedad con ID: {}", id);
    }
}
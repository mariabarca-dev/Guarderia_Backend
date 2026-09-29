package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Zona;
import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.zonas.ZonaUpdate;
import com.guarderiaCentral.guarderia_Backend.services.ZonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de negocio para la entidad {@link Zona}.
 * Maneja las validaciones de unicidad, integridad referencial y borrado lógico.
 *
 * @author Cátedra Guardería Central
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;
    private final GarageRepository garageRepository;

    /**
     * Registra una nueva zona en la base de datos previa normalización y verificación de unicidad.
     *
     * @param request DTO con los datos de entrada para el registro.
     * @return {@link ZonaResponse} Objeto de respuesta mapeado.
     * @throws BusinessException Si ya existe una zona activa con la misma letra.
     */
    @Override
    @Transactional
    public ZonaResponse registrarZona(ZonaRequest request) {
        log.info("Iniciando proceso de registro de zona con letra: {}", request.getLetra());

        String letraNormalizada = request.getLetra().trim().toUpperCase();
        request.setLetra(letraNormalizada);

        if (zonaRepository.existsByLetraAndActivoTrue(letraNormalizada)) {
            log.error("Error al registrar zona. La letra '{}' ya está registrada y activa.", letraNormalizada);
            throw new BusinessException("Ya existe una zona registrada con la letra: " + letraNormalizada, HttpStatus.BAD_REQUEST);
        }

        Zona zona = zonaRepository.toEntity(request);
        zona.setActivo(true);

        Zona zonaGuardada = zonaRepository.save(zona);
        log.info("Zona registrada exitosamente con ID: {}", zonaGuardada.getId());

        return zonaRepository.fromEntity(zonaGuardada);
    }

    /**
     * Busca una zona activa por su ID.
     *
     * @param id Identificador de la zona.
     * @return {@link ZonaResponse} Datos de la zona encontrada.
     * @throws RegistroNoEncontradoException Si la zona no existe o está desactivada.
     */
    @Override
    @Transactional(readOnly = true)
    public ZonaResponse buscarPorId(Integer id) {
        log.info("Buscando zona por ID: {}", id);
        Zona zona = zonaRepository.findById(id)
                .filter(Zona::getActivo)
                .orElseThrow(() -> {
                    log.error("No se encontró la zona activa con ID: {}", id);
                    return new RegistroNoEncontradoException("No se encontró la zona activa con ID: " + id);
                });
        return zonaRepository.fromEntity(zona);
    }

    /**
     * Busca una zona activa por su letra identificadora.
     *
     * @param letra Letra de la zona.
     * @return {@link ZonaResponse} Datos de la zona encontrada.
     * @throws RegistroNoEncontradoException Si la zona no existe con esa letra.
     */
    @Override
    @Transactional(readOnly = true)
    public ZonaResponse buscarPorLetra(String letra) {
        String letraNorm = (letra != null) ? letra.trim().toUpperCase() : "";
        log.info("Buscando zona activa por letra: {}", letraNorm);

        Zona zona = zonaRepository.findByLetraAndActivoTrue(letraNorm)
                .orElseThrow(() -> {
                    log.error("No se encontró la zona activa con letra: {}", letraNorm);
                    return new RegistroNoEncontradoException("No se encontró la zona con letra: " + letraNorm);
                });

        return zonaRepository.fromEntity(zona);
    }

    /**
     * Retorna todas las zonas con borrado lógico en {@code true}.
     *
     * @return Lista de {@link ZonaResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponse> listarTodas() {
        log.info("Listando todas las zonas activas");
        return zonaRepository.findAll().stream()
                .filter(Zona::getActivo)
                .map(zonaRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas las zonas sin aplicar filtro de borrado lógico (Exclusivo para uso administrativo).
     *
     * @return Lista completa de {@link ZonaResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponse> listarTodasIncluyendoInactivas() {
        log.info("Listando todas las zonas (incluyendo inactivas)");
        return zonaRepository.findAllIncludingInactive().stream()
                .map(zonaRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la información de una zona. Valida que no se modifique el tipo de vehículo
     * si la zona ya cuenta con garages asociados.
     *
     * @param id Identificador de la zona.
     * @param update DTO con los datos a actualizar.
     * @return {@link ZonaResponse} Datos de la zona actualizada.
     * @throws RegistroNoEncontradoException Si la zona no existe.
     * @throws BusinessException Si se intenta modificar el tipo de vehículo teniendo garages asociados.
     */
    @Override
    @Transactional
    public ZonaResponse actualizarZona(Integer id, ZonaUpdate update) {
        log.info("Iniciando actualización de la zona con ID: {}", id);

        Zona zonaExistente = zonaRepository.findById(id)
                .filter(Zona::getActivo)
                .orElseThrow(() -> {
                    log.error("No se puede actualizar: La zona con ID {} no existe o está inactiva.", id);
                    return new RegistroNoEncontradoException("No se puede actualizar: La zona con ID " + id + " no existe.");
                });

        String nuevaLetra = update.getLetra().trim().toUpperCase();
        update.setLetra(nuevaLetra);

        // Si cambia la letra, validar que la nueva no esté ocupada por otra zona
        if (!zonaExistente.getLetra().equalsIgnoreCase(nuevaLetra) &&
                zonaRepository.existsByLetraAndActivoTrue(nuevaLetra)) {
            log.error("No se puede actualizar la zona. La letra '{}' ya está asignada a otra zona.", nuevaLetra);
            throw new BusinessException("Ya existe una zona registrada con la letra: " + nuevaLetra, HttpStatus.BAD_REQUEST);
        }

        // Regla de Negocio: Validar si cambia el tipo de vehículo y si existen garages asociados
        if (!zonaExistente.getTipoVehiculo().equals(update.getTipoVehiculo())) {
            boolean tieneGarajes = garageRepository.existsByZonaIdAndActivoTrue(id);
            if (tieneGarajes) {
                log.error("Bloqueo de modificación de tipo de vehículo en zona ID {}: posee garajes activos asociados.", id);
                throw new BusinessException("Error: No se puede cambiar el tipo de vehículo de la zona '"
                        + zonaExistente.getLetra() + "' porque ya posee garajes asociados.", HttpStatus.BAD_REQUEST);
            }
        }

        zonaRepository.updateEntity(zonaExistente, update);
        Zona zonaActualizada = zonaRepository.save(zonaExistente);
        log.info("Zona con ID: {} actualizada exitosamente.", id);

        return zonaRepository.fromEntity(zonaActualizada);
    }

    /**
     * Desactiva lógicamente una zona del sistema. Bloquea la operación si la zona
     * aún tiene garajes asociados activos para resguardar la integridad referencial.
     *
     * @param id Identificador único de la zona a desactivar.
     * @throws RegistroNoEncontradoException Si no se encuentra la zona activa.
     * @throws BusinessException Si existen garajes asociados a la zona.
     */
    @Override
    @Transactional
    public void eliminarZona(Integer id) {
        log.info("Iniciando borrado lógico de la zona con ID: {}", id);

        Zona zona = zonaRepository.findById(id)
                .filter(Zona::getActivo)
                .orElseThrow(() -> {
                    log.error("No se puede eliminar: La zona con ID {} no existe o ya está inactiva.", id);
                    return new RegistroNoEncontradoException("No se puede eliminar: La zona con ID " + id + " no existe.");
                });

        boolean tieneGarajes = garageRepository.existsByZonaIdAndActivoTrue(id);
        if (tieneGarajes) {
            log.error("Bloqueo de eliminación para la zona ID {}: existen garajes asociados.", id);
            throw new BusinessException("Error: No se puede eliminar la zona '" + zona.getLetra()
                    + "' porque existen garajes asociados a ella.", HttpStatus.BAD_REQUEST);
        }

        zona.setActivo(false);
        zonaRepository.save(zona);
        log.info("Borrado lógico realizado con éxito para la zona ID: {}", id);
    }
}
package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageUpdate;
import com.guarderiaCentral.guarderia_Backend.services.GarageService;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * Controlador RESTful para la gestión y administración de las relaciones de propiedad entre Socios y Garajes.
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/api/propiedades-garage")
@RequiredArgsConstructor
public class PropiedadGarageRestController {

    private static final Logger logger = LoggerFactory.getLogger(PropiedadGarageRestController.class);

    private final PropiedadGarageService propiedadGarageService;
    private final GarageService garageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<PropiedadGarageResponse>> obtenerTodas() {
        logger.info("REST Request para listar todas las propiedades de garajes activas.");
        List<PropiedadGarageResponse> lista = propiedadGarageService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/inactivas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PropiedadGarageResponse>> listarTodasIncluyendoInactivas() {
        logger.info("REST Request para listar todas las propiedades de garajes (incluyendo inactivas).");
        List<PropiedadGarageResponse> lista = propiedadGarageService.listarTodasIncluyendoInactivas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<PropiedadGarageResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para obtener la propiedad de garaje con ID: {}", id);
        PropiedadGarageResponse response = propiedadGarageService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PropiedadGarageResponse> registrarPropiedad(@Valid @RequestBody PropiedadGarageRequest request) {
        logger.info("REST Request para registrar la propiedad del garaje ID {} al socio ID {}",
                request.getGarageId(), request.getSocioId());
        PropiedadGarageResponse nuevaPropiedad = propiedadGarageService.registrarPropiedad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPropiedad);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PropiedadGarageResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PropiedadGarageUpdate update) {
        logger.info("REST Request para actualizar la propiedad de garaje con ID: {}", id);
        PropiedadGarageResponse propiedadActualizada = propiedadGarageService.actualizar(id, update);
        return ResponseEntity.ok(propiedadActualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para dar de baja la propiedad de garaje con ID: {}", id);
        propiedadGarageService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponse>> listarPorSocio(@PathVariable Integer socioId) {
        logger.info("REST Request para listar los garajes pertenecientes al socio ID: {}", socioId);
        List<GarageResponse> garages = propiedadGarageService.listarPorSocio(socioId).stream()
                .map(prop -> {
                    if (prop.getGarageId() != null) {
                        try {
                            return garageService.buscarPorId(prop.getGarageId());
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        return ResponseEntity.ok(garages);
    }
}
package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages.PropiedadGarageUpdate;
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

/**
 * Controlador RESTful para la gestión y administración de las relaciones de propiedad entre Socios y Garajes.
 * Administra el registro de compra, consulta y mantenimiento de las titularidades sobre los garajes.
 *
 * Cumple con la matriz de control de acceso basada en roles:
 * - SOCIO y EMPLEADO: Permiso de lectura/consulta sobre sus propios registros de asignación o entidades asociadas.
 * - ADMINISTRADOR: Control total CRUD (GET/POST/PUT/DELETE) sobre las propiedades de garajes.
 * - SYSADMIN: Sin acceso a la entidad de negocio PropiedadGarage.
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/api/propiedades-garage")
@RequiredArgsConstructor
public class PropiedadGarageRestController {

    private static final Logger logger = LoggerFactory.getLogger(PropiedadGarageRestController.class);

    private final PropiedadGarageService propiedadGarageService;

    /**
     * Obtiene el listado global de todas las relaciones de propiedad de garaje activas registradas en el sistema.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO.
     *
     * @return ResponseEntity conteniendo la lista de {@link PropiedadGarageResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<PropiedadGarageResponse>> obtenerTodas() {
        logger.info("REST Request para listar todas las propiedades de garajes activas.");
        List<PropiedadGarageResponse> lista = propiedadGarageService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene el listado completo de todas las relaciones de propiedad de garaje, incluyendo inactivas (uso administrativo).
     * Permitido para rol: ADMINISTRADOR.
     *
     * @return ResponseEntity conteniendo la lista completa de {@link PropiedadGarageResponse} y código HTTP 200 OK.
     */
    @GetMapping("/admin/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PropiedadGarageResponse>> listarTodasIncluyendoInactivas() {
        logger.info("REST Request para listar todas las propiedades de garajes (incluyendo inactivas).");
        List<PropiedadGarageResponse> lista = propiedadGarageService.listarTodasIncluyendoInactivas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna el detalle de una propiedad de garaje por su identificador único.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO, SOCIO.
     *
     * @param id Identificador único del registro de propiedad.
     * @return ResponseEntity con la información de {@link PropiedadGarageResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<PropiedadGarageResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para obtener la propiedad de garaje con ID: {}", id);
        PropiedadGarageResponse response = propiedadGarageService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra una nueva venta/titularidad de garaje asignándola a un socio.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param request Objeto con los datos de asignación de la propiedad ({@link PropiedadGarageRequest}).
     * @return ResponseEntity con el {@link PropiedadGarageResponse} creado y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PropiedadGarageResponse> registrarPropiedad(@Valid @RequestBody PropiedadGarageRequest request) {
        logger.info("REST Request para registrar la propiedad del garaje ID {} al socio ID {}",
                request.getGarageId(), request.getSocioId());
        PropiedadGarageResponse nuevaPropiedad = propiedadGarageService.registrarPropiedad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPropiedad);
    }

    /**
     * Actualiza la información registrada de una propiedad de garaje existente.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único del registro de propiedad a modificar.
     * @param update Objeto con la información actualizada de la propiedad ({@link PropiedadGarageUpdate}).
     * @return ResponseEntity con el {@link PropiedadGarageResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PropiedadGarageResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PropiedadGarageUpdate update) {
        logger.info("REST Request para actualizar la propiedad de garaje con ID: {}", id);
        PropiedadGarageResponse propiedadActualizada = propiedadGarageService.actualizar(id, update);
        return ResponseEntity.ok(propiedadActualizada);
    }

    /**
     * Cancela o desactiva (borrado lógico) la asignación de propiedad de un garaje.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único del registro de propiedad a dar de baja.
     * @return ResponseEntity con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para dar de baja la propiedad de garaje con ID: {}", id);
        propiedadGarageService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la lista de garajes pertenecientes a un socio determinado utilizando {@link GarageResponse}.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO, SOCIO.
     *
     * @param socioId Identificador único del socio a consultar.
     * @return ResponseEntity conteniendo la lista de {@link GarageResponse} pertenecientes al socio y código HTTP 200 OK.
     */
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponse>> listarPorSocio(@PathVariable Integer socioId) {
        logger.info("REST Request para listar los garajes pertenecientes al socio ID: {}", socioId);
        List<GarageResponse> garages = propiedadGarageService.listarPorSocio(socioId).stream()
                .map(prop -> {
                    if (prop.getGarage() != null) {
                        return prop.getGarage();
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return ResponseEntity.ok(garages);
    }
}
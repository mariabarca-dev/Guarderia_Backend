package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.GarageRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.GarageUpdate;
import com.guarderiaCentral.guarderia_Backend.services.GarageService;
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
 * Controlador RESTful para la gestión y administración de Garages en la guardería central.
 * Proporciona endpoints para consulta y mantenimiento de las instalaciones físicas (garages).
 *
 * Cumple con las restricciones de seguridad basadas en roles expuestas en la matriz del sistema:
 * - SOCIO y EMPLEADO: Acceso de lectura/consulta (GET) sobre la entidad Garage.
 * - ADMINISTRADOR: Control total (CRUD - GET/POST/PUT/DELETE) sobre la entidad Garage.
 * - SYSADMIN: Sin acceso a la entidad de negocio Garage.
 *
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/api/garages")
@RequiredArgsConstructor
public class GarageRestController {

    private static final Logger logger = LoggerFactory.getLogger(GarageRestController.class);

    private final GarageService garageService;

    /**
     * Obtiene el listado de todos los garages activos registrados en el sistema.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO, SOCIO.
     *
     * @return ResponseEntity conteniendo la lista de {@link GarageResponse} y código HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponse>> obtenerTodos() {
        logger.info("REST Request para listar todos los garages activos.");
        List<GarageResponse> lista = garageService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Busca y retorna un garage activo según su identificador único.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO, SOCIO.
     *
     * @param id Identificador único del garage.
     * @return ResponseEntity con la información de {@link GarageResponse} y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<GarageResponse> obtenerPorId(@PathVariable Integer id) {
        logger.info("REST Request para obtener el garage con ID: {}", id);
        GarageResponse response = garageService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca y retorna la información de un garage activo a partir de su número asignado.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO, SOCIO.
     *
     * @param numeroGarage Número identificador del garage dentro del establecimiento.
     * @return ResponseEntity con la información de {@link GarageResponse} y código HTTP 200 OK.
     */
    @GetMapping("/numero/{numeroGarage}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<GarageResponse> obtenerPorNumero(@PathVariable int numeroGarage) {
        logger.info("REST Request para consultar el garage por número: {}", numeroGarage);
        GarageResponse response = garageService.buscarPorNumeroGarage(numeroGarage);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo garage en el sistema.
     * La sintaxis y estructura son verificadas automáticamente mediante {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param request Objeto con la información requerida para el alta del garage ({@link GarageRequest}).
     * @return ResponseEntity con el {@link GarageResponse} creado y código HTTP 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GarageResponse> crear(@Valid @RequestBody GarageRequest request) {
        logger.info("REST Request para dar de alta un nuevo garage con número: {}", request.getNumeroGarage());
        GarageResponse nuevoGarage = garageService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoGarage);
    }

    /**
     * Actualiza la información de un garage existente en el sistema.
     * La estructura del cuerpo de la petición es verificada con {@code @Valid}.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único del garage a modificar.
     * @param update Objeto con los datos actualizados del garage ({@link GarageUpdate}).
     * @return ResponseEntity con el {@link GarageResponse} actualizado y código HTTP 200 OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GarageResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody GarageUpdate update) {
        logger.info("REST Request para actualizar el garage con ID: {}", id);
        GarageResponse garageActualizado = garageService.actualizar(id, update);
        return ResponseEntity.ok(garageActualizado);
    }

    /**
     * Desactiva (borrado lógico) un garage según su identificador único.
     * Permitido para rol: ADMINISTRADOR.
     *
     * @param id Identificador único del garage a dar de baja.
     * @return ResponseEntity con código HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logger.info("REST Request para dar de baja lógicamente el garage con ID: {}", id);
        garageService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene un reporte con la información de disponibilidad de los garages del sistema.
     * Exclusivo para personal operativo y administrativo.
     * Permitido para roles: ADMINISTRADOR, EMPLEADO.
     *
     * @return ResponseEntity con la lista de estados de disponibilidad y código HTTP 200 OK.
     */
    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<String>> consultarDisponibilidad() {
        logger.info("REST Request para obtener el reporte global de disponibilidad de garages.");
        List<String> reporte = garageService.consultarDisponibilidadGarages();
        return ResponseEntity.ok(reporte);
    }
}
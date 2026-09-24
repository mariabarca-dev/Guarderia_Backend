package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.*;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/socios")
public class SocioRestController {

    private final SocioService socioService;

    public SocioRestController(SocioService socioService) {
        this.socioService = socioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SocioResponseDTO> registrarSocio(@Valid @RequestBody SocioRequestDTO requestDTO) {
        SocioResponseDTO nuevoSocio = socioService.registrarSocio(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSocio);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<SocioResponseDTO>> listarTodosLosSocios() {
        return ResponseEntity.ok(socioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<SocioResponseDTO> buscarSocioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(socioService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<SocioResponseDTO> buscarSocioPorDni(@RequestParam String dni) {
        return ResponseEntity.ok(socioService.buscarPorDni(dni));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SocioResponseDTO> modificarSocio(@PathVariable Long id,
                                                           @Valid @RequestBody SocioRequestDTO requestDTO) {
        return ResponseEntity.ok(socioService.actualizarSocio(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarSocio(@PathVariable Long id) {
        socioService.eliminarSocio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{socioId}/vehiculos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculosPorSocio(@PathVariable Long socioId,
                                                                             Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");
        return ResponseEntity.ok(socioService.listarVehiculosPorSocio(socioId, username, esSocio));
    }

    @GetMapping("/{socioId}/garages")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponseDTO>> listarGarajesPorSocio(@PathVariable Long socioId,
                                                                         Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");
        return ResponseEntity.ok(socioService.listarGarajesPorSocio(socioId, username, esSocio));
    }

    @GetMapping("/{socioId}/garage-estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<EstadoGarageSocioResponseDTO> obtenerEstadoGarageSocio(@PathVariable Long socioId,
                                                                                 Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");
        return ResponseEntity.ok(socioService.obtenerEstadoGarageSocio(socioId, username, esSocio));
    }

    private boolean poseeRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(rol));
    }
}

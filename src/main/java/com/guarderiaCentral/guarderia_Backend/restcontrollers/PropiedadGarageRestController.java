package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.EstadoGarageSocioResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.PropiedadGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.PropiedadGarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/propiedades-garage")
public class PropiedadGarageRestController {

    private final PropiedadGarageService propiedadGarageService;

    public PropiedadGarageRestController(PropiedadGarageService propiedadGarageService) {
        this.propiedadGarageService = propiedadGarageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PropiedadGarageResponseDTO> registrarPropiedad(@Valid @RequestBody PropiedadGarageRequestDTO requestDTO) {
        PropiedadGarageResponseDTO nuevaPropiedad = propiedadGarageService.registrarPropiedad(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPropiedad);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<PropiedadGarageResponseDTO>> listarTodas() {
        return ResponseEntity.ok(propiedadGarageService.listarTodas());
    }

    @GetMapping("/socio/{socioId}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<EstadoGarageSocioResponseDTO> obtenerEstadoGarageSocio(@PathVariable Long socioId,
                                                                                 Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");

        EstadoGarageSocioResponseDTO estado = propiedadGarageService.obtenerEstadoGarageSocio(socioId, username, esSocio);
        return ResponseEntity.ok(estado);
    }

    @GetMapping("/socio/{socioId}/garages")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponseDTO>> listarPorSocio(@PathVariable Long socioId,
                                                                  Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");

        List<GarageResponseDTO> garages = propiedadGarageService.listarPorSocio(socioId, username, esSocio);
        return ResponseEntity.ok(garages);
    }

    private boolean poseeRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(rol));
    }
}

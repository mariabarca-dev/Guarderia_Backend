package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.GarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ReporteDisponibilidadZonaDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.GarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.GarageService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/garages")
public class GarageRestController {

    private final GarageService garageService;

    public GarageRestController(GarageService garageService) {
        this.garageService = garageService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<List<GarageResponseDTO>> listarGarajes(Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");
        // Asume que si es socio, el username coincide con el identificador del socio o se recupera del principal
        String dniSocio = esSocio ? username : null;

        return ResponseEntity.ok(garageService.listarGarages(username, esSocio, dniSocio));
    }

    @GetMapping("/numero/{numeroGarage}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<GarageResponseDTO> buscarPorNumero(@PathVariable Integer numeroGarage,
                                                             Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = poseeRol(authentication, "ROLE_SOCIO");
        String dniSocio = esSocio ? username : null;

        return ResponseEntity.ok(garageService.buscarPorNumero(numeroGarage, username, esSocio, dniSocio));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<GarageResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(garageService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GarageResponseDTO> registrarGarage(@Valid @RequestBody GarageRequestDTO requestDTO) {
        GarageResponseDTO nuevo = garageService.registrarGarage(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GarageResponseDTO> actualizarGarage(@PathVariable Long id,
                                                              @Valid @RequestBody GarageRequestDTO requestDTO) {
        GarageResponseDTO actualizado = garageService.actualizarGarage(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/numero/{numeroGarage}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarGarage(@PathVariable Integer numeroGarage) {
        garageService.eliminarGarage(numeroGarage);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reportes/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<ReporteDisponibilidadZonaDTO>> consultarDisponibilidad() {
        return ResponseEntity.ok(garageService.consultarDisponibilidadGarages());
    }

    private boolean poseeRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(rol));
    }
}

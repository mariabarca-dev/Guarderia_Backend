package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.AsignacionVehiculoGarageResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.AsignacionVehiculoGarageRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionVehiculoGarageService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asignaciones-vehiculo-garage")
public class AsignacionVehiculoGarageRestController {

    private final AsignacionVehiculoGarageService asignacionService;

    public AsignacionVehiculoGarageRestController(AsignacionVehiculoGarageService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionVehiculoGarageResponseDTO> crearAsignacion(@Valid @RequestBody AsignacionVehiculoGarageRequestDTO requestDTO) {
        AsignacionVehiculoGarageResponseDTO nuevaAsignacion = asignacionService.crearAsignacion(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<AsignacionVehiculoGarageResponseDTO>> listarTodas() {
        return ResponseEntity.ok(asignacionService.listarTodas());
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO', 'SOCIO')")
    public ResponseEntity<AsignacionVehiculoGarageResponseDTO> buscarPorVehiculo(@PathVariable Long vehiculoId,
                                                                                 Authentication authentication) {
        String username = authentication.getName();
        boolean esSocio = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SOCIO"));

        AsignacionVehiculoGarageResponseDTO asignacion = asignacionService.buscarPorVehiculoId(vehiculoId, username, esSocio);
        return ResponseEntity.ok(asignacion);
    }
}

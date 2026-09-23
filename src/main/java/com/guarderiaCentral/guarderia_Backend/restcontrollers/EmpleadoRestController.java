package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.EmpleadoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.EmpleadoRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.EmpleadoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empleados")
public class EmpleadoRestController {

    private final EmpleadoService empleadoService;

    public EmpleadoRestController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EmpleadoResponseDTO> registrarEmpleado(@Valid @RequestBody EmpleadoRequestDTO requestDTO) {
        EmpleadoResponseDTO nuevoEmpleado = empleadoService.registrarEmpleado(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EmpleadoResponseDTO> actualizarEmpleado(@PathVariable Long id,
                                                                  @Valid @RequestBody EmpleadoRequestDTO requestDTO) {
        EmpleadoResponseDTO empActualizado = empleadoService.actualizarEmpleado(id, requestDTO);
        return ResponseEntity.ok(empActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/zonas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<ZonaResponseDTO>> listarZonasAsignadas(@PathVariable Long id,
                                                                      Authentication authentication) {
        String username = authentication.getName();
        boolean esAdmin = esAdministrador(authentication);

        List<ZonaResponseDTO> zonas = empleadoService.listarZonasAsignadas(id, username, esAdmin);
        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/{id}/vehiculos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculosBajoResponsabilidad(@PathVariable Long id,
                                                                                        Authentication authentication) {
        String username = authentication.getName();
        boolean esAdmin = esAdministrador(authentication);

        List<VehiculoResponseDTO> vehiculos = empleadoService.listarVehiculosBajoResponsabilidad(id, username, esAdmin);
        return ResponseEntity.ok(vehiculos);
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMINISTRADOR"));
    }
}

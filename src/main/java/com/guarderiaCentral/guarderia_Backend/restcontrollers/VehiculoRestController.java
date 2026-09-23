package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.VehiculoResponseDTO;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoRestController {

    private final VehiculoService vehiculoService;

    public VehiculoRestController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponseDTO> registrarVehiculo(@Valid @RequestBody VehiculoRequestDTO requestDTO) {
        VehiculoResponseDTO nuevoVehiculo = vehiculoService.registrarVehiculo(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoVehiculo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarTodosLosVehiculos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<VehiculoResponseDTO> buscarVehiculoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.buscarPorId(id));
    }

    @GetMapping("/matricula/{matricula}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<VehiculoResponseDTO> buscarVehiculoPorMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(vehiculoService.buscarPorMatricula(matricula));
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<VehiculoResponseDTO>> buscarVehiculosPorTipo(@PathVariable TipoVehiculo tipo) {
        return ResponseEntity.ok(vehiculoService.buscarPorTipo(tipo));
    }

    @GetMapping("/zona/{zonaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculosPorZona(@PathVariable Long zonaId) {
        return ResponseEntity.ok(vehiculoService.listarVehiculosPorZona(zonaId));
    }

    @GetMapping("/responsable/{empleadoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'EMPLEADO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculosPorResponsable(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(vehiculoService.listarVehiculosPorResponsable(empleadoId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VehiculoResponseDTO> modificarVehiculo(@PathVariable Long id,
                                                                 @Valid @RequestBody VehiculoRequestDTO requestDTO) {
        return ResponseEntity.ok(vehiculoService.actualizarVehiculo(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Long id) {
        vehiculoService.eliminarVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}

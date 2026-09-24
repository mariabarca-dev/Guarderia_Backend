package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AsignacionEmpleadoZonaUpdateDTO;
import com.guarderiaCentral.guarderia_Backend.services.AsignacionEmpleadoZonaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asignaciones-empleado-zona")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AsignacionEmpleadoZonaRestController {

    private final AsignacionEmpleadoZonaService asignacionService;

    public AsignacionEmpleadoZonaRestController(AsignacionEmpleadoZonaService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    public ResponseEntity<List<AsignacionEmpleadoZonaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(asignacionService.listarTodas());
    }

    @GetMapping("/empleado/{codigo}")
    public ResponseEntity<List<AsignacionEmpleadoZonaResponseDTO>> buscarPorCodigoEmpleado(@PathVariable String codigo) {
        return ResponseEntity.ok(asignacionService.buscarPorCodigoEmpleado(codigo));
    }

    @GetMapping("/zona/{idZona}/empleados")
    public ResponseEntity<List<EmpleadoResponseDTO>> obtenerEmpleadosPorZona(@PathVariable Long idZona) {
        return ResponseEntity.ok(asignacionService.obtenerEmpleadosPorZona(idZona));
    }

    @PostMapping
    public ResponseEntity<AsignacionEmpleadoZonaResponseDTO> crearAsignacion(@Valid @RequestBody AsignacionEmpleadoZonaRequestDTO requestDTO) {
        AsignacionEmpleadoZonaResponseDTO nuevaAsignacion = asignacionService.crearAsignacion(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }
}
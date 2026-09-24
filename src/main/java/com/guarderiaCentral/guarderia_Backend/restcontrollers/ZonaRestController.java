package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.dtos.ZonaRequestDTO;
import com.guarderiaCentral.guarderia_Backend.dtos.ZonaResponseDTO;
import com.guarderiaCentral.guarderia_Backend.services.ZonaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zonas")
public class ZonaRestController {

    private final ZonaService zonaService;

    public ZonaRestController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ZonaResponseDTO> registrarZona(@Valid @RequestBody ZonaRequestDTO requestDTO) {
        ZonaResponseDTO nuevaZona = zonaService.registrarZona(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaZona);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ZonaResponseDTO>> listarZonas() {
        return ResponseEntity.ok(zonaService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZonaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(zonaService.buscarPorId(id));
    }

    @GetMapping("/letra/{letra}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZonaResponseDTO> buscarPorLetra(@PathVariable String letra) {
        return ResponseEntity.ok(zonaService.buscarPorLetra(letra));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ZonaResponseDTO> actualizarZona(@PathVariable Long id,
                                                          @Valid @RequestBody ZonaRequestDTO requestDTO) {
        return ResponseEntity.ok(zonaService.actualizarZona(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarZona(@PathVariable Long id) {
        zonaService.eliminarZona(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/letra/{letra}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarPorLetra(@PathVariable String letra) {
        zonaService.eliminarPorLetra(letra);
        return ResponseEntity.noContent().build();
    }
}

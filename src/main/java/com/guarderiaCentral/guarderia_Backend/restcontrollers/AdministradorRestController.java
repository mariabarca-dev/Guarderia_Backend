package com.guarderiaCentral.guarderia_Backend.restcontrollers;

import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorRequestDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorResponseDTO;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorUpdateDTO;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/administradores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdministradorRestController {

    private final AdministradorService administradorService;

    public AdministradorRestController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> listarTodos() {
        List<AdministradorResponseDTO> administradores = administradorService.listarTodos();
        return ResponseEntity.ok(administradores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> buscarPorId(@PathVariable Long id) {
        AdministradorResponseDTO admin = administradorService.buscarPorId(id);
        return ResponseEntity.ok(admin);
    }

    @PostMapping
    public ResponseEntity<AdministradorResponseDTO> registrarAdministrador(@Valid @RequestBody AdministradorRequestDTO requestDTO) {
        AdministradorResponseDTO nuevoAdmin = administradorService.registrarAdministrador(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAdmin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> actualizarAdministrador(@PathVariable Long id,
                                                                            @Valid @RequestBody AdministradorRequestDTO requestDTO) {
        AdministradorResponseDTO adminActualizado = administradorService.actualizarAdministrador(id, requestDTO);
        return ResponseEntity.ok(adminActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Long id) {
        administradorService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}

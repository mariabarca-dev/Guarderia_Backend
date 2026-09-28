package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.AdministradorDTO;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Administrador;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la entidad Administrador.
 * Maneja persistencia JPA, validaciones de unicidad y borrado lógico.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorDTO> listarActivos() {
        log.info("Listando todos los administradores activos.");
        return administradorRepository.findByActivoTrue().stream()
                .map(AdministradorResponse::fromEntity)
                .map(response -> {
                    AdministradorDTO dto = new AdministradorDTO();
                    dto.setId(response.getId());
                    dto.setNombreUsuario(response.getNombreUsuario());
                    dto.setDni(response.getDni());
                    dto.setNombre(response.getNombre());
                    dto.setApellido(response.getApellido());
                    dto.setEmail(response.getEmail());
                    dto.setTelefono(response.getTelefono());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponse> listarTodosAdmin() {
        log.info("Listando todos los administradores (incluyendo inactivos) por solicitud administrativa.");
        return administradorRepository.findAllIncludingInactive().stream()
                .map(AdministradorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdministradorDTO buscarPorId(Integer id) {
        log.info("Buscando administrador con ID: {}", id);
        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        AdministradorResponse response = AdministradorResponse.fromEntity(admin);
        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(response.getId());
        dto.setNombreUsuario(response.getNombreUsuario());
        dto.setDni(response.getDni());
        dto.setNombre(response.getNombre());
        dto.setApellido(response.getApellido());
        dto.setEmail(response.getEmail());
        dto.setTelefono(response.getTelefono());
        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdministradorResponse registrarAdministrador(AdministradorRequest request) {
        log.info("Registrando nuevo administrador con DNI: {}", request.getDni());

        if (administradorRepository.existsByDni(request.getDni())) {
            throw new DniDuplicadoException("Ya existe un administrador registrado con el DNI: " + request.getDni());
        }

        Administrador admin = request.toEntity();
        // Encriptar password usando el PasswordEncoder inyectado por seguridad
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setActivo(true);

        Administrador savedAdmin = administradorRepository.save(admin);
        log.info("Administrador registrado exitosamente con ID: {}", savedAdmin.getId());

        return AdministradorResponse.fromEntity(savedAdmin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdministradorResponse actualizarAdministrador(Integer id, AdministradorUpdate update) {
        log.info("Actualizando administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        update.updateEntity(admin);
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(update.getPassword()));
        }

        Administrador updatedAdmin = administradorRepository.save(admin);
        log.info("Administrador con ID: {} actualizado exitosamente.", id);

        return AdministradorResponse.fromEntity(updatedAdmin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarAdministrador(Integer id) {
        log.info("Ejecutando borrado lógico para el administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        admin.setActivo(false);
        administradorRepository.save(admin);
        log.info("Administrador con ID: {} desactivado correctamente.", id);
    }
}
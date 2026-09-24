package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.AdministradorResponseDTO;
import com.guarderiaCentral.guarderia_Backend.modelos.Administrador;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.repositories.AdministradorRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.dtos.AdministradorRequestDTO;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorServiceImpl(AdministradorRepository administradorRepository,
                                    PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponseDTO> listarTodos() {
        return administradorRepository.findAll().stream()
                .filter(admin -> admin.getActivo() == null || admin.getActivo()) // Considera borrado lógico si la entidad lo tiene
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdministradorResponseDTO buscarPorId(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador con ID: " + id));

        return mapearAResponseDTO(admin);
    }

    @Override
    @Transactional
    public AdministradorResponseDTO registrarAdministrador(AdministradorRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("Los datos del administrador no pueden ser nulos.");
        }

        // 1. Validar regla de negocio de unicidad de username
        if (administradorRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ErrorNegocio("El nombre de usuario '" + dto.getNombreUsuario() + "' ya existe.");
        }

        // 2. Mapeo manual hacia el modelo
        Administrador admin = new Administrador();
        admin.setNombre(dto.getNombre().trim());
        admin.setApellido(dto.getApellido().trim());
        admin.setDireccion(dto.getDireccion().trim());
        admin.setTelefono(dto.getTelefono().trim());
        admin.setNombreUsuario(dto.getNombreUsuario().trim());

        // Cifrado de contraseña obligatorio antes de guardar
        admin.setClave(passwordEncoder.encode(dto.getClave()));

        // Asignación de rol por defecto
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setActivo(true);

        // 3. Persistencia (el ID lo genera la BD de forma autoincremental/sequence)
        Administrador guardado = administradorRepository.save(admin);

        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional
    public AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorRequestDTO dto) {
        if (dto == null) {
            throw new ErrorNegocio("Los datos a actualizar no pueden ser nulos.");
        }

        Administrador adminExistente = administradorRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede actualizar: El administrador con ID " + id + " no existe."));

        // Validar si intenta cambiar el nombre de usuario a uno que ya pertenece a otro registro
        if (!adminExistente.getNombreUsuario().equalsIgnoreCase(dto.getNombreUsuario()) &&
                administradorRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ErrorNegocio("El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso.");
        }

        adminExistente.setNombre(dto.getNombre().trim());
        adminExistente.setApellido(dto.getApellido().trim());
        adminExistente.setDireccion(dto.getDireccion().trim());
        adminExistente.setTelefono(dto.getTelefono().trim());
        adminExistente.setNombreUsuario(dto.getNombreUsuario().trim());

        // Actualizar clave únicamente si se envía una nueva
        if (dto.getClave() != null && !dto.getClave().isBlank()) {
            adminExistente.setClave(passwordEncoder.encode(dto.getClave()));
        }

        Administrador actualizado = administradorRepository.save(adminExistente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede eliminar: El administrador con ID " + id + " no existe."));

        // Borrado lógico (recomendado en entornos enterprise con Spring Data JPA)
        admin.setActivo(false);
        administradorRepository.save(admin);

        // Si fuera borrado físico:
        // administradorRepository.delete(admin);
    }

    // Auxiliar de mapeo interno a DTO de salida
    private AdministradorResponseDTO mapearAResponseDTO(Administrador admin) {
        AdministradorResponseDTO response = new AdministradorResponseDTO();
        response.setId(admin.getId());
        response.setNombre(admin.getNombre());
        response.setApellido(admin.getApellido());
        response.setDireccion(admin.getDireccion());
        response.setTelefono(admin.getTelefono());
        response.setNombreUsuario(admin.getNombreUsuario());
        response.setRol(admin.getRol());
        return response;
    }
}
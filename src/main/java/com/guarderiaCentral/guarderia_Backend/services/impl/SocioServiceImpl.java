package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.dtos.*;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.modelos.Socio;
import com.guarderiaCentral.guarderia_Backend.repositories.SocioRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.services.PropiedadGarageService;
import com.guarderiaCentral.guarderia_Backend.services.SocioService;
import com.guarderiaCentral.guarderia_Backend.services.VehiculoService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocioServiceImpl implements SocioService {

    private final SocioRepository socioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoService vehiculoService;
    private final PropiedadGarageService propiedadGarageService;
    private final PasswordEncoder passwordEncoder;

    public SocioServiceImpl(SocioRepository socioRepository,
                            UsuarioRepository usuarioRepository,
                            VehiculoService vehiculoService,
                            PropiedadGarageService propiedadGarageService,
                            PasswordEncoder passwordEncoder) {
        this.socioRepository = socioRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoService = vehiculoService;
        this.propiedadGarageService = propiedadGarageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SocioResponseDTO registrarSocio(SocioRequestDTO dto) {
        if (socioRepository.existsByDni(dto.getDni())) {
            throw new DniDuplicadoException("Error de negocio: Ya existe un socio registrado con el DNI: " + dto.getDni());
        }

        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ErrorNegocio("Error de negocio: El nombre de usuario '" + dto.getNombreUsuario() + "' ya se encuentra en uso.");
        }

        LocalDate fechaFundacion = LocalDate.of(2000, 1, 1);
        if (dto.getFechaIngreso().isBefore(fechaFundacion)) {
            throw new ErrorNegocio("Error de negocio: La fecha de ingreso no puede ser anterior a la fecha de fundación (" + fechaFundacion + ").");
        }

        if (dto.getRol() != Rol.SOCIO && dto.getRol() != Rol.ADMINISTRADOR) {
            throw new ErrorNegocio("Error de negocio: El rol asignado no cuenta con los permisos permitidos para este tipo de registro.");
        }

        Socio socio = new Socio();
        socio.setDni(dto.getDni());
        socio.setNombre(dto.getNombre());
        socio.setApellido(dto.getApellido());
        socio.setDireccion(dto.getDireccion());
        socio.setTelefono(dto.getTelefono());
        socio.setNombreUsuario(dto.getNombreUsuario());
        socio.setClave(passwordEncoder.encode(dto.getClave()));
        socio.setFechaIngreso(dto.getFechaIngreso());
        socio.setRol(dto.getRol());

        Socio guardado = socioRepository.save(socio);
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public SocioResponseDTO buscarPorId(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio con ID: " + id));
        return mapearAResponseDTO(socio);
    }

    @Override
    @Transactional(readOnly = true)
    public SocioResponseDTO buscarPorDni(String dni) {
        Socio socio = socioRepository.findByDni(dni)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el socio con DNI: " + dni));
        return mapearAResponseDTO(socio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocioResponseDTO> listarTodos() {
        return socioRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO dto) {
        Socio socioExistente = socioRepository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se puede actualizar: Socio no encontrado con ID " + id));

        socioRepository.findByDni(dto.getDni()).ifPresent(socioConMismoDni -> {
            if (!socioConMismoDni.getId().equals(id)) {
                throw new ErrorNegocio("Error de negocio: El DNI " + dto.getDni() + " ya está asignado a otro socio.");
            }
        });

        socioExistente.setDni(dto.getDni());
        socioExistente.setNombre(dto.getNombre());
        socioExistente.setApellido(dto.getApellido());
        socioExistente.setDireccion(dto.getDireccion());
        socioExistente.setTelefono(dto.getTelefono());
        if (dto.getClave() != null && !dto.getClave().isBlank()) {
            socioExistente.setClave(passwordEncoder.encode(dto.getClave()));
        }

        Socio actualizado = socioRepository.save(socioExistente);
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarSocio(Long id) {
        if (!socioRepository.existsById(id)) {
            throw new RegistroNoEncontradoException("No se puede eliminar: Socio no encontrado con ID " + id);
        }
        socioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculosPorSocio(Long socioId, String usernameActual, boolean esSocio) {
        validarAccesoSocio(socioId, usernameActual, esSocio);
        return vehiculoService.listarPorSocio(socioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponseDTO> listarGarajesPorSocio(Long socioId, String usernameActual, boolean esSocio) {
        validarAccesoSocio(socioId, usernameActual, esSocio);
        return propiedadGarageService.listarPorSocio(socioId, usernameActual, esSocio);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoGarageSocioResponseDTO obtenerEstadoGarageSocio(Long socioId, String usernameActual, boolean esSocio) {
        validarAccesoSocio(socioId, usernameActual, esSocio);
        return propiedadGarageService.obtenerEstadoGarageSocio(socioId, usernameActual, esSocio);
    }

    private void validarAccesoSocio(Long socioId, String usernameActual, boolean esSocio) {
        if (esSocio) {
            Socio socio = socioRepository.findById(socioId)
                    .orElseThrow(() -> new RegistroNoEncontradoException("Socio no encontrado"));
            if (!socio.getNombreUsuario().equalsIgnoreCase(usernameActual)) {
                throw new AccessDeniedException("Acceso denegado: No tiene permisos para consultar información de otro socio.");
            }
        }
    }

    private SocioResponseDTO mapearAResponseDTO(Socio socio) {
        SocioResponseDTO dto = new SocioResponseDTO();
        dto.setId(socio.getId());
        dto.setDni(socio.getDni());
        dto.setNombre(socio.getNombre());
        dto.setApellido(socio.getApellido());
        dto.setDireccion(socio.getDireccion());
        dto.setTelefono(socio.getTelefono());
        dto.setNombreUsuario(socio.getNombreUsuario());
        dto.setFechaIngreso(socio.getFechaIngreso());
        dto.setRol(socio.getRol());
        return dto;
    }
}

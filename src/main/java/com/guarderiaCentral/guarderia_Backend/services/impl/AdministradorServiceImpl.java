package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.NombreUsuarioDuplicadoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.RegistroNoEncontradoException;
import com.guarderiaCentral.guarderia_Backend.exceptions.SysAdminProtegidoException;
import com.guarderiaCentral.guarderia_Backend.modelos.Administrador;
import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRepository;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorRequest;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorResponse;
import com.guarderiaCentral.guarderia_Backend.repositories.administradores.AdministradorUpdate;
import com.guarderiaCentral.guarderia_Backend.repositories.usuarios.UsuarioRepository;
import com.guarderiaCentral.guarderia_Backend.services.AdministradorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la entidad Administrador.
 * Maneja persistencia JPA, guardado inteligente (reactivación), protección de SYSADMIN
 * y borrado lógico.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdministradorResponse crear(AdministradorRequest request) {
        log.info("Iniciando creación de administrador con nombreUsuario: {}", request.getNombreUsuario());

        // Verificar si el nombreUsuario ya pertenece a otro registro (activo o inactivo) en toda la jerarquía
        Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(request.getNombreUsuario());

        if (duenoOpt.isPresent()) {
            Integer duenoId = duenoOpt.get();
            Optional<Administrador> inactivoOpt = administradorRepository.findByIdIncludingInactive(duenoId);

            if (inactivoOpt.isPresent()) {
                Administrador inactivo = inactivoOpt.get();
                if (Boolean.FALSE.equals(inactivo.getActivo())) {
                    log.info("Reactivando administrador inactivo con ID: {}", inactivo.getId());

                    AdministradorUpdate update = new AdministradorUpdate();
                    update.setNombre(request.getNombre());
                    update.setApellido(request.getApellido());
                    update.setDireccion(request.getDireccion());
                    update.setTelefono(request.getTelefono());
                    update.setNombreUsuario(request.getNombreUsuario());
                    if (request.getClave() != null && !request.getClave().isBlank()) {
                        update.setClave(passwordEncoder.encode(request.getClave()));
                    }

                    administradorRepository.updateEntity(inactivo, update);
                    inactivo.setActivo(true);
                    Administrador reactivado = administradorRepository.save(inactivo);
                    return administradorRepository.fromEntity(reactivado);
                }
            }
            throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + request.getNombreUsuario() + "' ya se encuentra registrado.");
        }

        Administrador administrador = administradorRepository.toEntity(request);
        administrador.setClave(passwordEncoder.encode(request.getClave()));
        administrador.setActivo(true);

        Administrador guardado = administradorRepository.save(administrador);
        log.info("Administrador creado exitosamente con ID: {}", guardado.getId());

        return administradorRepository.fromEntity(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdministradorResponse buscarPorId(Integer id) {
        log.info("Buscando administrador activo con ID: {}", id);
        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        return administradorRepository.fromEntity(admin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponse> listarTodos() {
        log.info("Listando todos los administradores activos.");
        return administradorRepository.findAll().stream()
                .filter(Administrador::getActivo)
                .map(administradorRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponse> listarTodosIncluyendoInactivas() {
        log.info("Listando todos los administradores (incluyendo inactivos).");
        return administradorRepository.findAllIncludingInactive().stream()
                .map(administradorRepository::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdministradorResponse actualizar(Integer id, AdministradorUpdate update) {
        log.info("Actualizando administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        if (admin.getRol() == Rol.SYSADMIN) {
            throw new SysAdminProtegidoException("No se permite realizar esta operación sobre un usuario con rol SYSADMIN.");
        }

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equals(admin.getNombreUsuario())) {
            Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(update.getNombreUsuario());
            if (duenoOpt.isPresent() && !duenoOpt.get().equals(id)) {
                throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + update.getNombreUsuario() + "' ya se encuentra registrado.");
            }
        }

        if (update.getClave() != null && !update.getClave().isBlank()) {
            update.setClave(passwordEncoder.encode(update.getClave()));
        }

        administradorRepository.updateEntity(admin, update);
        Administrador actualizado = administradorRepository.save(admin);
        log.info("Administrador con ID: {} actualizado exitosamente.", actualizado.getId());

        return administradorRepository.fromEntity(actualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Ejecutando borrado lógico para el administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        if (admin.getRol() == Rol.SYSADMIN) {
            throw new SysAdminProtegidoException("No se permite realizar esta operación sobre un usuario con rol SYSADMIN.");
        }

        admin.setActivo(false);
        administradorRepository.save(admin);
        log.info("Administrador con ID: {} desactivado correctamente.", id);
    }
}
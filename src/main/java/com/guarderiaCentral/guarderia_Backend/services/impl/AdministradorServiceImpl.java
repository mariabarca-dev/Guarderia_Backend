package com.guarderiaCentral.guarderia_Backend.services.impl;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import com.guarderiaCentral.guarderia_Backend.exceptions.DniDuplicadoException;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio para la entidad Administrador.
 * Maneja persistencia JPA, guardado inteligente (reactivación), protección de las cuentas SYSADMIN
 * frente al rol ADMINISTRADOR y borrado lógico.
 * <p>
 * El SYSADMIN opera sobre todas las cuentas. Si quien llama es un ADMINISTRADOR, las cuentas SYSADMIN
 * no se listan y no se pueden consultar, modificar, dar de baja ni reactivar. El SYSADMIN no puede dar de
 * baja su propia cuenta, para que el sistema no se quede sin SYSADMIN.
 * </p>
 * <p>
 * El DNI es opcional en el administrador; cuando se carga, es único entre todas las cuentas del sistema.
 * </p>
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
     * Solo se reactivan administradores comunes inactivos: una cuenta SYSADMIN inactiva no se reactiva por esta
     * vía, por lo que su nombre de usuario se informa como ya registrado.
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
                if (Boolean.FALSE.equals(inactivo.getActivo()) && inactivo.getRol() != Rol.SYSADMIN) {
                    // El DNI (si se envió) no puede pertenecer a otra cuenta distinta de la que se reactiva
                    validarDniUnico(request.getDni(), inactivo.getId());

                    log.info("Reactivando administrador inactivo con ID: {}", inactivo.getId());

                    AdministradorUpdate update = new AdministradorUpdate();
                    update.setNombre(request.getNombre());
                    update.setApellido(request.getApellido());
                    update.setDni(request.getDni());
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

        // El DNI (si se envió) no puede pertenecer a ninguna otra cuenta del sistema
        validarDniUnico(request.getDni(), null);

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
    public AdministradorResponse buscarPorId(Integer id, String nombreUsuarioLlamador) {
        log.info("Buscando administrador activo con ID: {}", id);
        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        validarAcceso(admin, nombreUsuarioLlamador);
        return administradorRepository.fromEntity(admin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponse> listarTodos(String nombreUsuarioLlamador) {
        log.info("Listando todos los administradores activos.");
        boolean llamadorEsSysAdmin = esSysAdmin(nombreUsuarioLlamador);
        return administradorRepository.findAll().stream()
                .filter(Administrador::getActivo)
                .filter(admin -> puedeVer(admin, llamadorEsSysAdmin))
                .map(administradorRepository::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponse> listarTodosIncluyendoInactivos(String nombreUsuarioLlamador) {
        log.info("Listando todos los administradores (incluyendo inactivos).");
        boolean llamadorEsSysAdmin = esSysAdmin(nombreUsuarioLlamador);
        return administradorRepository.findAllIncludingInactive().stream()
                .filter(admin -> puedeVer(admin, llamadorEsSysAdmin))
                .map(administradorRepository::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AdministradorResponse actualizar(Integer id, AdministradorUpdate update, String nombreUsuarioLlamador) {
        log.info("Actualizando administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        validarAcceso(admin, nombreUsuarioLlamador);

        if (update.getNombreUsuario() != null && !update.getNombreUsuario().equals(admin.getNombreUsuario())) {
            Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorNombreUsuarioIncluyendoInactivos(update.getNombreUsuario());
            if (duenoOpt.isPresent() && !duenoOpt.get().equals(id)) {
                throw new NombreUsuarioDuplicadoException("El nombre de usuario '" + update.getNombreUsuario() + "' ya se encuentra registrado.");
            }
        }

        if (update.getDni() != null && !update.getDni().equalsIgnoreCase(admin.getDni())) {
            validarDniUnico(update.getDni(), id);
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
     * El SYSADMIN no puede dar de baja su propia cuenta: se compara el nombre de usuario de la cuenta a borrar
     * con el de quien llama (que viene del token). Un administrador común sí puede darse de baja.
     */
    @Override
    @Transactional
    public void eliminar(Integer id, String nombreUsuarioLlamador) {
        log.info("Ejecutando borrado lógico para el administrador con ID: {}", id);

        Administrador admin = administradorRepository.findById(id)
                .filter(Administrador::getActivo)
                .orElseThrow(() -> new RegistroNoEncontradoException("No se encontró el administrador activo con ID: " + id));

        validarAcceso(admin, nombreUsuarioLlamador);

        if (admin.getRol() == Rol.SYSADMIN && admin.getNombreUsuario().equals(nombreUsuarioLlamador)) {
            log.warn("El SYSADMIN '{}' intentó dar de baja su propia cuenta.", nombreUsuarioLlamador);
            throw new BusinessException("Error: El SYSADMIN no puede dar de baja su propia cuenta.", HttpStatus.CONFLICT);
        }

        admin.setActivo(false);
        administradorRepository.save(admin);
        log.info("Administrador con ID: {} desactivado correctamente.", id);
    }

    /**
     * Valida que el DNI no pertenezca a ninguna otra cuenta del sistema (activa o inactiva, de cualquier tipo).
     * El DNI es opcional en el administrador: si es nulo o está en blanco, no se valida.
     *
     * @param dni       DNI a validar (puede ser nulo).
     * @param idExcluir ID de la cuenta que se está modificando o reactivando; null en un alta nueva.
     * @throws DniDuplicadoException Si el DNI ya pertenece a otra cuenta.
     */
    private void validarDniUnico(String dni, Integer idExcluir) {
        if (dni == null || dni.isBlank()) {
            return;
        }
        Optional<Integer> duenoOpt = usuarioRepository.buscarIdPorDniIncluyendoInactivos(dni);
        if (duenoOpt.isPresent() && (idExcluir == null || !duenoOpt.get().equals(idExcluir))) {
            throw new DniDuplicadoException("El DNI " + dni + " ya se encuentra registrado en otra cuenta.");
        }
    }

    /**
     * Indica si quien llama tiene rol SYSADMIN. Se resuelve contra la base y no contra el cliente.
     * Si el usuario no se encuentra, se lo trata como no SYSADMIN (la opción más restrictiva).
     *
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la operación.
     * @return true si la cuenta activa de quien llama tiene rol SYSADMIN.
     */
    private boolean esSysAdmin(String nombreUsuarioLlamador) {
        return usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuarioLlamador)
                .map(usuario -> usuario.getRol() == Rol.SYSADMIN)
                .orElse(false);
    }

    /**
     * Regla única de visibilidad: el SYSADMIN ve todas las cuentas; el ADMINISTRADOR no ve las SYSADMIN.
     *
     * @param admin               Cuenta a evaluar.
     * @param llamadorEsSysAdmin  true si quien llama es SYSADMIN.
     * @return true si quien llama puede ver y operar sobre la cuenta.
     */
    private boolean puedeVer(Administrador admin, boolean llamadorEsSysAdmin) {
        return llamadorEsSysAdmin || admin.getRol() != Rol.SYSADMIN;
    }

    /**
     * Valida que quien llama pueda operar sobre la cuenta indicada.
     *
     * @param admin                 Cuenta objetivo.
     * @param nombreUsuarioLlamador Nombre de usuario de quien realiza la operación.
     * @throws SysAdminProtegidoException Si quien llama es ADMINISTRADOR y la cuenta objetivo es SYSADMIN.
     */
    private void validarAcceso(Administrador admin, String nombreUsuarioLlamador) {
        if (!puedeVer(admin, esSysAdmin(nombreUsuarioLlamador))) {
            throw new SysAdminProtegidoException();
        }
    }
}
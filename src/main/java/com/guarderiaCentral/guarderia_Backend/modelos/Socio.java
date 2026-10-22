package com.guarderiaCentral.guarderia_Backend.modelos;

import com.guarderiaCentral.guarderia_Backend.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad que representa a un Socio del sistema, heredando de Usuario.
 * El DNI se hereda de {@link Usuario} y, a diferencia de los demás tipos de cuenta, es obligatorio para el socio:
 * además de exigirse en los DTOs, la entidad lo verifica antes de guardarse.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "socios")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Socio extends Usuario {

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    /**
     * Red de seguridad: impide guardar o modificar un socio sin DNI, aunque algún camino del código
     * se saltee la validación de los DTOs.
     *
     * @throws BusinessException Si el DNI es nulo o está en blanco.
     */
    @PrePersist
    @PreUpdate
    private void validarDniObligatorio() {
        if (getDni() == null || getDni().isBlank()) {
            throw new BusinessException("El DNI es obligatorio para un socio.");
        }
    }
}
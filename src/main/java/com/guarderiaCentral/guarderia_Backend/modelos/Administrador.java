package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//check euge 3
/**
 * Entidad que representa a un Administrador del sistema, heredando de Usuario.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {
    // Hereda todos los atributos de Usuario
}
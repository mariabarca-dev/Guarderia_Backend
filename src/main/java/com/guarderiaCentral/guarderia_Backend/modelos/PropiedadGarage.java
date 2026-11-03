package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * Entidad asociativa que representa la propiedad de un Garage por parte de un Socio.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "propiedades_garage")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "fecha_compra_garage", nullable = false)
    private LocalDate fechaCompraGarage;

    /** Meses contratados en la compra. Las compras anteriores a este campo quedan con 1 mes. */
    @Column(name = "meses_contratados", nullable = false, columnDefinition = "int default 1")
    private Integer mesesContratados = 1;

    /** Método de pago de la compra. Las compras anteriores a este campo quedan sin dato (null). */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 20, columnDefinition = "varchar(20)")
    private MetodoPago metodoPago;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
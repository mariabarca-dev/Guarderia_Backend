package com.guarderiaCentral.guarderia_Backend.modelos;

import com.guarderiaCentral.guarderia_Backend.config.ConstantesNegocio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entidad que representa un Garage dentro de una Zona.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "garages")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_garage", nullable = false, unique = true)
    private int numeroGarage;

    @Column(name = "lectura_luz", nullable = false)
    private double lecturaLuz;

    @Column(name = "servicio_mantenimiento", nullable = false)
    private boolean servicioMantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Calcula la cuota de esta cochera con su lectura de luz y su servicio de mantenimiento.
     * La cuota no se guarda: se calcula cada vez, por eso no se puede editar.
     *
     * @return Importe de la cuota con dos decimales.
     */
    public BigDecimal calcularCuota() {
        return calcularCuota(lecturaLuz, servicioMantenimiento);
    }

    /**
     * Calcula una cuota: valor base + lectura de luz x precio del kWh + mantenimiento, si lo tiene.
     *
     * @param lecturaLuz            Lectura del contador de luz, en kWh.
     * @param servicioMantenimiento Indica si la cochera tiene el servicio de mantenimiento.
     * @return Importe de la cuota con dos decimales.
     */
    public static BigDecimal calcularCuota(double lecturaLuz, boolean servicioMantenimiento) {
        BigDecimal luz = BigDecimal.valueOf(lecturaLuz).multiply(ConstantesNegocio.PRECIO_KWH);
        BigDecimal mantenimiento = servicioMantenimiento ? ConstantesNegocio.VALOR_MANTENIMIENTO : BigDecimal.ZERO;
        return ConstantesNegocio.VALOR_BASE_GARAGE.add(luz).add(mantenimiento).setScale(2, RoundingMode.HALF_UP);
    }
}
package com.guarderiaCentral.guarderia_Backend.repositories;

import jakarta.persistence.EntityNotFoundException;

import java.util.function.Supplier;

/**
 * Lectura tolerante de un dato de una entidad relacionada, usada por los métodos {@code fromEntity} que
 * agregan datos legibles (matrícula, letra de zona, DNI, etc.) a las respuestas.
 * <p>
 * Las entidades tienen baja lógica ({@code @SQLRestriction("activo = true")}). Una asignación o una compra dada
 * de baja puede apuntar a un vehículo, un garage o un socio que también se dio de baja: al leer su dato
 * JPA no lo encuentra y lanza {@link EntityNotFoundException}. En ese caso el dato queda en {@code null} y el
 * listado de historial sigue funcionando.
 * </p>
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
public final class RelacionSegura {

    private RelacionSegura() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Ejecuta la lectura indicada y devuelve su valor, o {@code null} si la entidad relacionada ya no está
     * disponible (dada de baja lógica).
     *
     * @param lectura lectura del dato (por ejemplo {@code () -> vehiculo.getSocio().getDni()})
     * @param <T>     tipo del dato leído
     * @return el dato leído, o {@code null} si la entidad relacionada no se encuentra
     */
    public static <T> T leer(Supplier<T> lectura) {
        try {
            return lectura.get();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
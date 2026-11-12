package pe.edu.pucp.reuse.bl.insignias;

import pe.edu.pucp.reuse.bl.BLException;

/**
 * Patron Specification (RF-18): encapsula una condicion de negocio que un candidato
 * cumple o no. Las especificaciones se combinan con {@link #y(Especificacion)} sin
 * que la clase que las evalua conozca cada regla en particular.
 */
@FunctionalInterface
public interface Especificacion<T> {

    boolean esSatisfechaPor(T candidato) throws BLException;

    /** Conjuncion: se cumple solo si se cumplen ambas. */
    default Especificacion<T> y(Especificacion<T> otra) {
        return candidato -> esSatisfechaPor(candidato) && otra.esSatisfechaPor(candidato);
    }

    /** Especificacion neutra para la conjuncion (siempre se cumple). */
    static <T> Especificacion<T> siempre() {
        return candidato -> true;
    }
}

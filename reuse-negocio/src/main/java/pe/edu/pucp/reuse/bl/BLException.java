package pe.edu.pucp.reuse.bl;

/**
 * Error de negocio: una regla no se cumple o la operacion no pudo completarse.
 * Es lo unico que ve el programa principal; los SQLException del DAO se
 * convierten en BLException con un mensaje claro.
 */
public class BLException extends Exception {
    
    public BLException(String mensaje) {
        super(mensaje);
    }

    public BLException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

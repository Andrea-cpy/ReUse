package pe.edu.pucp.reuse.integraciones;

/**
 * Error al comunicarse con un sistema externo (Google, servidor de correo).
 * <p>
 * El mensaje es tecnico y solo debe ir al log del servidor. La capa de negocio decide que
 * mensaje ve el usuario segun el motivo, igual que hace con los SQLException del DAO.
 */
public class IntegracionException extends Exception {

    /** Por que fallo la integracion; la capa de negocio lo usa para elegir el mensaje al usuario. */
    public enum Motivo {
        /** Falta configuracion (archivo .properties o variable de entorno). */
        SIN_CONFIGURAR,
        /** El sistema externo no acepto los datos (firma invalida, token vencido, otra audiencia...). */
        TOKEN_RECHAZADO,
        /** Los datos no vienen de quien deberian (emisor desconocido). */
        EMISOR_DESCONOCIDO,
        /** La respuesta no tiene el formato esperado. */
        FORMATO_INESPERADO
    }

    private final Motivo motivo;

    public IntegracionException(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public IntegracionException(Motivo motivo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}

package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public interface TransaccionBL extends RegistroBL<Transaccion> {

    /**
     * Confirma la cita. La transaccion pasa a CITA_CONFIRMADA, la cita a
     * CONFIRMADA, el anuncio a RESERVADO y las demas negociaciones del
     * anuncio se cancelan. Si algo falla, rollback.
     */
    void confirmarCita(int idTransaccion) throws BLException;

    /**
     * Registra la confirmacion del comprador o del vendedor; con ambas, la
     * transaccion queda COMPLETADA, el anuncio VENDIDO y la cita REALIZADA.
     */
    void confirmarEntrega(int idTransaccion, int idUsuario) throws BLException;

    void cancelar(int idTransaccion) throws BLException;

    ArrayList<Transaccion> listarPorAnuncio(int idAnuncio) throws BLException;
}

package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.transacciones.Oferta;

public interface OfertaBL extends RegistroBL<Oferta> {

    /**
     * Acepta la oferta, rechaza las demas ofertas pendientes del anuncio y
     * abre la transaccion vinculada a la oferta. Devuelve el id de la
     * transaccion creada.
     */
    int aceptar(int idOferta) throws BLException;

    void rechazar(int idOferta) throws BLException;

    ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws BLException;

    ArrayList<Oferta> listarPorComprador(int idComprador) throws BLException;

    /** El vendedor propone otro monto: la oferta PENDIENTE pasa a CONTRAOFERTADA con el nuevo monto. */
    void contraofertar(int idOferta, double nuevoMonto) throws BLException;

    /**
     * El comprador responde la contraoferta. Si la acepta, se acepta la oferta con el
     * nuevo monto (abre la transaccion y devuelve su id); si no, queda RECHAZADA y devuelve 0.
     */
    int responderContraoferta(int idOferta, boolean acepta) throws BLException;
}

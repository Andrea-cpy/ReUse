package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.transacciones.Oferta;

public interface OfertaBL extends RegistroBL<Oferta> {

    /**
     * RF-17: acepta la oferta, rechaza las demas ofertas pendientes del anuncio
     * y abre la transaccion (EN_NEGOCIACION) vinculada a la oferta, todo en una
     * transaccion de base de datos. Devuelve el id de la transaccion creada.
     */
    int aceptar(int idOferta) throws BLException;

    void rechazar(int idOferta) throws BLException;

    ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws BLException;
}

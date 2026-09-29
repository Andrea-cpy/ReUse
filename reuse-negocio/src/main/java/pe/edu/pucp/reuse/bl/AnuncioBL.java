package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;

/**
 * insert registra el anuncio junto con sus imagenes en una sola transaccion.
 * delete solo procede si el anuncio no tiene transacciones (RF-02).
 */
public interface AnuncioBL extends RegistroBL<Anuncio> {
}

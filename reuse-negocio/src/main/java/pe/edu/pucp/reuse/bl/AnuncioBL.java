package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;

/**
 * Insert registra el anuncio junto con sus imagenes en una sola transaccion.
 * Delete solo procede si el anuncio no tiene transacciones.
 */
public interface AnuncioBL extends RegistroBL<Anuncio> {
}

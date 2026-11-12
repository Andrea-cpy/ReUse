package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;

/**
 * Insert registra el anuncio junto con sus imagenes en una sola transaccion.
 * Delete solo procede si el anuncio no tiene transacciones.
 */
public interface AnuncioBL extends RegistroBL<Anuncio> {

    /** Anuncios activos del vendedor, en cualquier estado. */
    ArrayList<Anuncio> listarPorVendedor(int idVendedor) throws BLException;

    /** RF-14: todos los anuncios en cualquier estado, con su material y categoria (bandeja del administrador). */
    ArrayList<Anuncio> listarTodos() throws BLException;

    /**
     * Catalogo: solo anuncios DISPONIBLE. Los filtros nulos o en cero no se aplican.
     * idUsuarioExcluido deja fuera los anuncios propios del usuario que busca.
     */
    ArrayList<Anuncio> buscar(String texto, int idCategoria, CondicionMaterial condicion,
                              Double precioMinimo, Double precioMaximo, int idUsuarioExcluido) throws BLException;

    /** El vendedor retira su anuncio DISPONIBLE del catalogo (pasa a ARCHIVADO). */
    void archivar(int idAnuncio, int idVendedor) throws BLException;

    /** El vendedor vuelve a publicar un anuncio ARCHIVADO (pasa a DISPONIBLE). */
    void republicar(int idAnuncio, int idVendedor) throws BLException;

    /**
     * RF-14: moderacion del administrador sobre un anuncio. accion = OBSERVAR (DISPONIBLE a OBSERVADO),
     * HABILITAR (OBSERVADO a DISPONIBLE) o ARCHIVAR (DISPONIBLE u OBSERVADO a ARCHIVADO).
     */
    void moderar(int idAnuncio, String accion) throws BLException;
}

package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;

/**
 * Al reportar, un anuncio DISPONIBLE pasa a OBSERVADO. Al sancionar se
 * archiva; al desestimar vuelve a DISPONIBLE.
 */
public interface ReporteAnuncioBL extends RegistroBL<ReporteAnuncio> {

    void sancionar(int idReporte, int idAdministrador) throws BLException;

    void desestimar(int idReporte, int idAdministrador) throws BLException;
}

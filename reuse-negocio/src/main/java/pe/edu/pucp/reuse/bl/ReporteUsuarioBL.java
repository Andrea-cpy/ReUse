package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;

public interface ReporteUsuarioBL extends RegistroBL<ReporteUsuario> {

    /**
     * Sanciona el reporte y, si el denunciado acumula 4 o mas sanciones en
     * 90 dias, suspende su cuenta en la misma transaccion. Devuelve true si
     * la cuenta fue suspendida.
     */
    boolean sancionar(int idReporte, int idAdministrador) throws BLException;

    void desestimar(int idReporte, int idAdministrador) throws BLException;
}

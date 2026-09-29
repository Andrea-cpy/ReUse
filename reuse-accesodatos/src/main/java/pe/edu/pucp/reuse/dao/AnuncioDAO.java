package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;

public interface AnuncioDAO extends DAO<Anuncio> {

    /**
     * Cambia el estado solo si el anuncio sigue en estadoActual (control de
     * concurrencia optimista). Devuelve 0 si otro proceso ya lo cambio.
     */
    int cambiarEstado(int idAnuncio, EstadoAnuncio estadoActual, EstadoAnuncio estadoNuevo) throws SQLException;

    int contarTransacciones(int idAnuncio) throws SQLException;
}

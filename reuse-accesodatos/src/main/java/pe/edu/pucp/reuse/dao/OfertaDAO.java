package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;

public interface OfertaDAO extends DAO<Oferta> {

    int cambiarEstado(int idOferta, EstadoOferta estado) throws SQLException;

    // Rechaza las demas ofertas PENDIENTE del anuncio cuando se acepta una.
    int rechazarPendientes(int idAnuncio, int idOfertaAceptada) throws SQLException;

    ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws SQLException;
}

package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;

public interface PuntoEntregaDAO extends DAO<PuntoEntrega> {

    PuntoEntrega obtenerPorNombre(String nombre) throws SQLException;

    // Citas PROPUESTA o CONFIRMADA en el punto de entrega.
    int contarCitasPendientes(int idPuntoEntrega) throws SQLException;
}

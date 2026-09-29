package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;

public interface CitaEntregaDAO extends DAO<CitaEntrega> {

    CitaEntrega obtenerPorTransaccion(int idTransaccion) throws SQLException;

    int cambiarEstado(int idCita, EstadoCita estado) throws SQLException;
}

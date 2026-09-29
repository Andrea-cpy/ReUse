package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public interface TransaccionDAO extends DAO<Transaccion> {

    int cambiarEstado(int idTransaccion, EstadoTransaccion estado) throws SQLException;

    // Pasa a COMPLETADA o CANCELADA y registra fecha_fin con la hora de la base de datos.
    int finalizar(int idTransaccion, EstadoTransaccion estadoFinal) throws SQLException;

    int registrarConfirmacion(int idTransaccion, boolean delComprador) throws SQLException;

    ArrayList<Transaccion> listarPorAnuncio(int idAnuncio) throws SQLException;
}

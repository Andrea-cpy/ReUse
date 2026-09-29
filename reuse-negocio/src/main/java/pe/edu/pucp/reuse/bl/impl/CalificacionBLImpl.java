package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CalificacionBL;
import pe.edu.pucp.reuse.dao.CalificacionDAO;
import pe.edu.pucp.reuse.dao.impl.CalificacionDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.enums.TipoCalificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class CalificacionBLImpl extends BaseBLImpl implements CalificacionBL {

    private final CalificacionDAO calificacionDAO = new CalificacionDAOImpl();

    @Override
    public int insert(Calificacion calificacion) throws BLException {
        validarYCompletar(calificacion);
        try {
            if (calificacionDAO.obtenerPorTransaccionYCalificador(calificacion.getTransaccion().getIdTransaccion(),
                    calificacion.getCalificador().getIdUsuario()) != null) {
                throw new BLException("El usuario ya califico esta transaccion");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar calificaciones previas", e);
        }

        // La calificacion y la reputacion del calificado se guardan juntas.
        TransactionsManager.iniciar();
        try {
            int idCalificacion = calificacionDAO.insert(calificacion);
            usuarioDAO.recalcularReputacion(calificacion.getCalificado().getIdUsuario());
            TransactionsManager.commit();
            return idCalificacion;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar la calificacion; se hizo rollback", e);
        }
    }

    @Override
    public int update(Calificacion calificacion) throws BLException {
        validarYCompletar(calificacion);
        Calificacion actual = buscarCalificacion(calificacion.getIdCalificacion());
        if (actual.getTransaccion().getIdTransaccion() != calificacion.getTransaccion().getIdTransaccion()
                || actual.getCalificador().getIdUsuario() != calificacion.getCalificador().getIdUsuario()) {
            throw new BLException("No se puede cambiar la transaccion ni el calificador de una calificacion");
        }

        TransactionsManager.iniciar();
        try {
            int filas = calificacionDAO.update(calificacion);
            usuarioDAO.recalcularReputacion(calificacion.getCalificado().getIdUsuario());
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo actualizar la calificacion; se hizo rollback", e);
        }
    }

    @Override
    public int delete(int idCalificacion) throws BLException {
        Calificacion actual = buscarCalificacion(idCalificacion);

        TransactionsManager.iniciar();
        try {
            int filas = calificacionDAO.delete(idCalificacion);
            usuarioDAO.recalcularReputacion(actual.getCalificado().getIdUsuario());
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete la calificacion; se hizo rollback", e);
        }
    }

    @Override
    public Calificacion findById(int idCalificacion) throws BLException {
        try {
            return calificacionDAO.findById(idCalificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la calificacion", e);
        }
    }

    @Override
    public ArrayList<Calificacion> findAll() throws BLException {
        try {
            return calificacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las calificaciones", e);
        }
    }

    /**
     * Puntaje de 1 a 5, solo transacciones COMPLETADAS y solo entre sus
     * participantes. El calificado y el tipo se deducen del calificador.
     */
    private void validarYCompletar(Calificacion calificacion) throws BLException {
        if (calificacion == null) {
            throw new BLException("La calificacion no puede ser nula");
        }
        if (calificacion.getPuntaje() < 1 || calificacion.getPuntaje() > 5) {
            throw new BLException("El puntaje debe estar entre 1 y 5 (se recibio " + calificacion.getPuntaje() + ")");
        }
        validarLongitudMaxima(calificacion.getComentario(), 500, "El comentario");
        if (calificacion.getTransaccion() == null) {
            throw new BLException("La calificacion debe indicar la transaccion");
        }
        Transaccion transaccion = buscarTransaccion(calificacion.getTransaccion().getIdTransaccion());
        if (transaccion.getEstado() != EstadoTransaccion.COMPLETADA) {
            throw new BLException("Solo se pueden calificar transacciones COMPLETADAS (estado actual: "
                    + transaccion.getEstado() + ")");
        }
        UsuarioPUCP calificador = buscarUsuario(calificacion.getCalificador(), "calificador");
        int idComprador = transaccion.getComprador().getIdUsuario();
        int idVendedor = idVendedor(transaccion);

        UsuarioPUCP calificado = new UsuarioPUCP();
        if (calificador.getIdUsuario() == idComprador) {
            calificado.setIdUsuario(idVendedor);
            calificacion.setTipoCalificacion(TipoCalificacion.COMPRADOR_A_VENDEDOR);
        } else if (calificador.getIdUsuario() == idVendedor) {
            calificado.setIdUsuario(idComprador);
            calificacion.setTipoCalificacion(TipoCalificacion.VENDEDOR_A_COMPRADOR);
        } else {
            throw new BLException("Solo el comprador o el vendedor de la transaccion pueden calificar");
        }
        if (calificacion.getCalificado() != null
                && calificacion.getCalificado().getIdUsuario() != calificado.getIdUsuario()) {
            throw new BLException("Solo se puede calificar a la otra parte de la transaccion");
        }
        if (calificacion.getCalificado() == null) {
            calificacion.setCalificado(calificado);
        }
    }

    private Calificacion buscarCalificacion(int idCalificacion) throws BLException {
        Calificacion calificacion = findById(idCalificacion);
        if (calificacion == null || !calificacion.isActivo()) {
            throw new BLException("No existe la calificacion con id " + idCalificacion);
        }
        return calificacion;
    }
}

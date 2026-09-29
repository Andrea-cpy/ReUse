package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.ReporteUsuarioBL;
import pe.edu.pucp.reuse.dao.AdministradorDAO;
import pe.edu.pucp.reuse.dao.ReporteUsuarioDAO;
import pe.edu.pucp.reuse.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.reuse.dao.impl.ReporteUsuarioDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class ReporteUsuarioBLImpl extends BaseBLImpl implements ReporteUsuarioBL {

    // RF-13: suspension automatica al acumular 4 o mas sanciones en 90 dias.
    private static final int SANCIONES_PARA_SUSPENDER = 4;
    private static final int DIAS_VENTANA_SANCIONES = 90;

    private final ReporteUsuarioDAO reporteDAO = new ReporteUsuarioDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    /**
     * RF-04: registra la denuncia (reporte + reporte_usuario) y aumenta el
     * contador de reportes del denunciado, todo en una transaccion.
     */
    @Override
    public int insert(ReporteUsuario reporte) throws BLException {
        validarDatos(reporte);
        reporte.setEstadoRevision(EstadoRevisionReporte.PENDIENTE);

        TransactionsManager.iniciar();
        try {
            int idReporte = reporteDAO.insert(reporte);
            usuarioDAO.actualizarContadorReportes(reporte.getDenunciado().getIdUsuario(), 1);
            TransactionsManager.commit();
            return idReporte;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el reporte; se hizo rollback", e);
        }
    }

    @Override
    public int update(ReporteUsuario reporte) throws BLException {
        validarDatos(reporte);
        ReporteUsuario actual = buscarReporte(reporte.getIdReporte());
        if (actual.getEstadoRevision() != EstadoRevisionReporte.PENDIENTE) {
            throw new BLException("Solo se puede update un reporte PENDIENTE");
        }
        if (actual.getDenunciado().getIdUsuario() != reporte.getDenunciado().getIdUsuario()) {
            throw new BLException("No se puede cambiar el usuario denunciado");
        }

        TransactionsManager.iniciar();
        try {
            int filas = reporteDAO.update(reporte);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo actualizar el reporte; se hizo rollback", e);
        }
    }

    // Anula la denuncia (borrado logico de ambas tablas) y descuenta el contador del denunciado.
    @Override
    public int delete(int idReporte) throws BLException {
        ReporteUsuario actual = buscarReporte(idReporte);

        TransactionsManager.iniciar();
        try {
            int filas = reporteDAO.delete(idReporte);
            usuarioDAO.actualizarContadorReportes(actual.getDenunciado().getIdUsuario(), -1);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete el reporte; se hizo rollback", e);
        }
    }

    @Override
    public ReporteUsuario findById(int idReporte) throws BLException {
        try {
            return reporteDAO.findById(idReporte);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el reporte", e);
        }
    }

    @Override
    public ArrayList<ReporteUsuario> findAll() throws BLException {
        try {
            return reporteDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los reportes de usuarios", e);
        }
    }

    @Override
    public boolean sancionar(int idReporte, int idAdministrador) throws BLException {
        ReporteUsuario reporte = buscarReportePendiente(idReporte);
        validarAdministrador(idAdministrador);
        int idDenunciado = reporte.getDenunciado().getIdUsuario();

        TransactionsManager.iniciar();
        try {
            if (reporteDAO.resolver(idReporte, EstadoRevisionReporte.SANCIONADO, idAdministrador) == 0) {
                throw new BLException("El reporte " + idReporte + " ya fue resuelto por otro administrador");
            }
            boolean suspender = reporteDAO.contarSancionesRecientes(idDenunciado, DIAS_VENTANA_SANCIONES)
                    >= SANCIONES_PARA_SUSPENDER;
            if (suspender) {
                usuarioDAO.actualizarEstadoCuenta(idDenunciado, EstadoCuenta.SUSPENDIDA);
            }
            TransactionsManager.commit();
            return suspender;
        } catch (BLException e) {
            TransactionsManager.rollback();
            throw e;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo sancionar el reporte; se hizo rollback", e);
        }
    }

    @Override
    public void desestimar(int idReporte, int idAdministrador) throws BLException {
        buscarReportePendiente(idReporte);
        validarAdministrador(idAdministrador);
        try {
            if (reporteDAO.resolver(idReporte, EstadoRevisionReporte.DESESTIMADO, idAdministrador) == 0) {
                throw new BLException("El reporte " + idReporte + " ya fue resuelto por otro administrador");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo desestimar el reporte", e);
        }
    }

    private void validarDatos(ReporteUsuario reporte) throws BLException {
        if (reporte == null) {
            throw new BLException("El reporte no puede ser nulo");
        }
        validarTexto(reporte.getDescripcion(), "La descripcion del reporte es obligatoria");
        if (reporte.getMotivo() == null) {
            throw new BLException("El motivo del reporte es obligatorio");
        }
        UsuarioPUCP denunciante = buscarUsuario(reporte.getDenunciante(), "denunciante");
        UsuarioPUCP denunciado = buscarUsuario(reporte.getDenunciado(), "denunciado");
        if (denunciante.getIdUsuario() == denunciado.getIdUsuario()) {
            throw new BLException("Un usuario no puede reportarse a si mismo");
        }
        // Si la denuncia se vincula a una transaccion, ambos deben ser sus participantes.
        if (reporte.getTransaccion() != null) {
            Transaccion transaccion = buscarTransaccion(reporte.getTransaccion().getIdTransaccion());
            int idComprador = transaccion.getComprador().getIdUsuario();
            int idVendedor = idVendedor(transaccion);
            boolean participan = (denunciante.getIdUsuario() == idComprador && denunciado.getIdUsuario() == idVendedor)
                    || (denunciante.getIdUsuario() == idVendedor && denunciado.getIdUsuario() == idComprador);
            if (!participan) {
                throw new BLException("El denunciante y el denunciado deben ser las partes de la transaccion");
            }
        }
    }

    private void validarAdministrador(int idAdministrador) throws BLException {
        AdministradorPUCP administrador;
        try {
            administrador = administradorDAO.findById(idAdministrador);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el administrador", e);
        }
        if (administrador == null || !administrador.isActivo()) {
            throw new BLException("Solo un administrador activo puede resolver reportes");
        }
    }

    private ReporteUsuario buscarReporte(int idReporte) throws BLException {
        ReporteUsuario reporte = findById(idReporte);
        if (reporte == null || !reporte.isActivo()) {
            throw new BLException("No existe el reporte con id " + idReporte);
        }
        return reporte;
    }

    private ReporteUsuario buscarReportePendiente(int idReporte) throws BLException {
        ReporteUsuario reporte = buscarReporte(idReporte);
        if (reporte.getEstadoRevision() != EstadoRevisionReporte.PENDIENTE) {
            throw new BLException("El reporte " + idReporte + " ya fue resuelto (" + reporte.getEstadoRevision() + ")");
        }
        return reporte;
    }
}

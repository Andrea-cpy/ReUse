package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.ReporteAnuncioBL;
import pe.edu.pucp.reuse.dao.AdministradorDAO;
import pe.edu.pucp.reuse.dao.ReporteAnuncioDAO;
import pe.edu.pucp.reuse.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.reuse.dao.impl.ReporteAnuncioDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class ReporteAnuncioBLImpl extends BaseBLImpl implements ReporteAnuncioBL {

    private final ReporteAnuncioDAO reporteDAO = new ReporteAnuncioDAOImpl();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    /**
     * RF-04 y RF-14: registra la denuncia (reporte + reporte_anuncio) y, si el
     * anuncio estaba DISPONIBLE, lo deja OBSERVADO; todo en una transaccion.
     */
    @Override
    public int insert(ReporteAnuncio reporte) throws BLException {
        validarDatos(reporte);
        reporte.setEstadoRevision(EstadoRevisionReporte.PENDIENTE);

        TransactionsManager.iniciar();
        try {
            int idReporte = reporteDAO.insert(reporte);
            anuncioDAO.cambiarEstado(reporte.getAnuncio().getIdAnuncio(),
                    EstadoAnuncio.DISPONIBLE, EstadoAnuncio.OBSERVADO);
            TransactionsManager.commit();
            return idReporte;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el reporte del anuncio; se hizo rollback", e);
        }
    }

    @Override
    public int update(ReporteAnuncio reporte) throws BLException {
        validarDatos(reporte);
        ReporteAnuncio actual = buscarReporte(reporte.getIdReporte());
        if (actual.getEstadoRevision() != EstadoRevisionReporte.PENDIENTE) {
            throw new BLException("Solo se puede update un reporte PENDIENTE");
        }
        if (actual.getAnuncio().getIdAnuncio() != reporte.getAnuncio().getIdAnuncio()) {
            throw new BLException("No se puede cambiar el anuncio reportado");
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

    @Override
    public int delete(int idReporte) throws BLException {
        buscarReporte(idReporte);

        TransactionsManager.iniciar();
        try {
            int filas = reporteDAO.delete(idReporte);
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
    public ReporteAnuncio findById(int idReporte) throws BLException {
        try {
            return reporteDAO.findById(idReporte);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el reporte", e);
        }
    }

    @Override
    public ArrayList<ReporteAnuncio> findAll() throws BLException {
        try {
            return reporteDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los reportes de anuncios", e);
        }
    }

    // RF-14: el anuncio sancionado se archiva (sale del catalogo sin borrarse fisicamente).
    @Override
    public void sancionar(int idReporte, int idAdministrador) throws BLException {
        ReporteAnuncio reporte = buscarReportePendiente(idReporte);
        validarAdministrador(idAdministrador);
        Anuncio anuncio = buscarAnuncio(reporte.getAnuncio().getIdAnuncio());

        TransactionsManager.iniciar();
        try {
            if (reporteDAO.resolver(idReporte, EstadoRevisionReporte.SANCIONADO, idAdministrador) == 0) {
                throw new BLException("El reporte " + idReporte + " ya fue resuelto por otro administrador");
            }
            if (anuncio.getEstado() == EstadoAnuncio.OBSERVADO || anuncio.getEstado() == EstadoAnuncio.DISPONIBLE) {
                anuncioDAO.cambiarEstado(anuncio.getIdAnuncio(), anuncio.getEstado(), EstadoAnuncio.ARCHIVADO);
            }
            TransactionsManager.commit();
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

    // Si la denuncia no procede, el anuncio observado vuelve a estar disponible.
    @Override
    public void desestimar(int idReporte, int idAdministrador) throws BLException {
        ReporteAnuncio reporte = buscarReportePendiente(idReporte);
        validarAdministrador(idAdministrador);

        TransactionsManager.iniciar();
        try {
            if (reporteDAO.resolver(idReporte, EstadoRevisionReporte.DESESTIMADO, idAdministrador) == 0) {
                throw new BLException("El reporte " + idReporte + " ya fue resuelto por otro administrador");
            }
            anuncioDAO.cambiarEstado(reporte.getAnuncio().getIdAnuncio(),
                    EstadoAnuncio.OBSERVADO, EstadoAnuncio.DISPONIBLE);
            TransactionsManager.commit();
        } catch (BLException e) {
            TransactionsManager.rollback();
            throw e;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo desestimar el reporte; se hizo rollback", e);
        }
    }

    private void validarDatos(ReporteAnuncio reporte) throws BLException {
        if (reporte == null) {
            throw new BLException("El reporte no puede ser nulo");
        }
        validarTexto(reporte.getDescripcion(), "La descripcion del reporte es obligatoria");
        if (reporte.getMotivo() == null) {
            throw new BLException("El motivo del reporte es obligatorio");
        }
        UsuarioPUCP denunciante = buscarUsuario(reporte.getDenunciante(), "denunciante");
        Anuncio anuncio = buscarAnuncio(reporte.getAnuncio());
        if (anuncio.getVendedor().getIdUsuario() == denunciante.getIdUsuario()) {
            throw new BLException("Un usuario no puede reportar su propio anuncio");
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

    private ReporteAnuncio buscarReporte(int idReporte) throws BLException {
        ReporteAnuncio reporte = findById(idReporte);
        if (reporte == null || !reporte.isActivo()) {
            throw new BLException("No existe el reporte con id " + idReporte);
        }
        return reporte;
    }

    private ReporteAnuncio buscarReportePendiente(int idReporte) throws BLException {
        ReporteAnuncio reporte = buscarReporte(idReporte);
        if (reporte.getEstadoRevision() != EstadoRevisionReporte.PENDIENTE) {
            throw new BLException("El reporte " + idReporte + " ya fue resuelto (" + reporte.getEstadoRevision() + ")");
        }
        return reporte;
    }
}

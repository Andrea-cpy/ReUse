package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.PuntoEntregaBL;
import pe.edu.pucp.reuse.dao.PuntoEntregaDAO;
import pe.edu.pucp.reuse.dao.impl.PuntoEntregaDAOImpl;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;

// Administracion de los puntos seguros de entrega dentro del campus.
public class PuntoEntregaBLImpl extends BaseBLImpl implements PuntoEntregaBL {

    private final PuntoEntregaDAO puntoDAO = new PuntoEntregaDAOImpl();

    @Override
    public int insert(PuntoEntrega punto) throws BLException {
        validarDatos(punto);
        validarNombreUnico(punto);
        try {
            return puntoDAO.insert(punto);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el punto de entrega", e);
        }
    }

    @Override
    public int update(PuntoEntrega punto) throws BLException {
        validarDatos(punto);
        validarExiste(punto.getIdPuntoEntrega());
        validarNombreUnico(punto);
        try {
            return puntoDAO.update(punto);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el punto de entrega", e);
        }
    }

    @Override
    public int delete(int idPuntoEntrega) throws BLException {
        validarExiste(idPuntoEntrega);
        try {
            return puntoDAO.delete(idPuntoEntrega);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete el punto de entrega", e);
        }
    }

    @Override
    public PuntoEntrega findById(int idPuntoEntrega) throws BLException {
        try {
            return puntoDAO.findById(idPuntoEntrega);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el punto de entrega", e);
        }
    }

    @Override
    public ArrayList<PuntoEntrega> findAll() throws BLException {
        try {
            return puntoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los puntos de entrega", e);
        }
    }

    private void validarDatos(PuntoEntrega punto) throws BLException {
        if (punto == null) {
            throw new BLException("El punto de entrega no puede ser nulo");
        }
        validarTexto(punto.getNombre(), "El nombre del punto de entrega es obligatorio");
        validarLongitudMaxima(punto.getNombre(), 120, "El nombre del punto de entrega");
        validarTexto(punto.getUbicacion(), "La ubicacion del punto de entrega es obligatoria");
        validarLongitudMaxima(punto.getUbicacion(), 250, "La ubicacion del punto de entrega");
        validarLongitudMaxima(punto.getReferencia(), 250, "La referencia del punto de entrega");
    }

    private void validarExiste(int idPuntoEntrega) throws BLException {
        if (findById(idPuntoEntrega) == null) {
            throw new BLException("No existe un punto de entrega con id " + idPuntoEntrega);
        }
    }

    private void validarNombreUnico(PuntoEntrega punto) throws BLException {
        try {
            PuntoEntrega existente = puntoDAO.obtenerPorNombre(punto.getNombre());
            if (existente != null && existente.getIdPuntoEntrega() != punto.getIdPuntoEntrega()) {
                throw new BLException("Ya existe un punto de entrega llamado '" + punto.getNombre() + "'");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del punto de entrega", e);
        }
    }
}

package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.RespuestaRapidaBL;
import pe.edu.pucp.reuse.dao.RespuestaRapidaDAO;
import pe.edu.pucp.reuse.dao.impl.RespuestaRapidaDAOImpl;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;

// RF-16: atajos de texto predefinidos por cada usuario.
public class RespuestaRapidaBLImpl extends BaseBLImpl implements RespuestaRapidaBL {

    private static final int MAXIMO_POR_USUARIO = 10;

    private final RespuestaRapidaDAO respuestaDAO = new RespuestaRapidaDAOImpl();

    @Override
    public int insert(RespuestaRapida respuesta) throws BLException {
        validarDatos(respuesta);
        if (listarPorCreador(respuesta.getCreador().getIdUsuario()).size() >= MAXIMO_POR_USUARIO) {
            throw new BLException("Un usuario puede tener como maximo " + MAXIMO_POR_USUARIO + " respuestas rapidas");
        }
        try {
            return respuestaDAO.insert(respuesta);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la respuesta rapida", e);
        }
    }

    @Override
    public int update(RespuestaRapida respuesta) throws BLException {
        validarDatos(respuesta);
        RespuestaRapida actual = buscarRespuesta(respuesta.getIdRespuesta());
        if (actual.getCreador().getIdUsuario() != respuesta.getCreador().getIdUsuario()) {
            throw new BLException("Solo el creador puede editar su respuesta rapida");
        }
        try {
            return respuestaDAO.update(respuesta);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la respuesta rapida", e);
        }
    }

    @Override
    public int delete(int idRespuesta) throws BLException {
        buscarRespuesta(idRespuesta);
        try {
            return respuestaDAO.delete(idRespuesta);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la respuesta rapida", e);
        }
    }

    @Override
    public RespuestaRapida findById(int idRespuesta) throws BLException {
        try {
            return respuestaDAO.findById(idRespuesta);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la respuesta rapida", e);
        }
    }

    @Override
    public ArrayList<RespuestaRapida> findAll() throws BLException {
        try {
            return respuestaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las respuestas rapidas", e);
        }
    }

    @Override
    public ArrayList<RespuestaRapida> listarPorCreador(int idCreador) throws BLException {
        try {
            return respuestaDAO.listarPorCreador(idCreador);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las respuestas rapidas del usuario", e);
        }
    }

    private void validarDatos(RespuestaRapida respuesta) throws BLException {
        if (respuesta == null) {
            throw new BLException("La respuesta rapida no puede ser nula");
        }
        validarTexto(respuesta.getTexto(), "El texto de la respuesta rapida es obligatorio");
        validarLongitudMaxima(respuesta.getTexto(), 500, "El texto de la respuesta rapida");
        buscarUsuario(respuesta.getCreador(), "creador");
    }

    private RespuestaRapida buscarRespuesta(int idRespuesta) throws BLException {
        RespuestaRapida respuesta = findById(idRespuesta);
        if (respuesta == null || !respuesta.isActivo()) {
            throw new BLException("No existe la respuesta rapida con id " + idRespuesta);
        }
        return respuesta;
    }
}

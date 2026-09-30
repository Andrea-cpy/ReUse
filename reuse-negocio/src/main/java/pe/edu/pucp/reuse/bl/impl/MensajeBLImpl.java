package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.MensajeBL;
import pe.edu.pucp.reuse.dao.CanalChatDAO;
import pe.edu.pucp.reuse.dao.MensajeDAO;
import pe.edu.pucp.reuse.dao.RespuestaRapidaDAO;
import pe.edu.pucp.reuse.dao.impl.CanalChatDAOImpl;
import pe.edu.pucp.reuse.dao.impl.MensajeDAOImpl;
import pe.edu.pucp.reuse.dao.impl.RespuestaRapidaDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class MensajeBLImpl extends BaseBLImpl implements MensajeBL {

    private static final int LONGITUD_MAXIMA = 2000;

    private final MensajeDAO mensajeDAO = new MensajeDAOImpl();
    private final CanalChatDAO chatDAO = new CanalChatDAOImpl();
    private final RespuestaRapidaDAO respuestaDAO = new RespuestaRapidaDAOImpl();

    // Solo se escribe en un chat ACTIVO y solo lo hacen el comprador o el vendedor.
    @Override
    public int insert(Mensaje mensaje) throws BLException {
        validarDatos(mensaje);
        mensaje.setLeido(false);
        try {
            return mensajeDAO.insert(mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo enviar el mensaje", e);
        }
    }

    @Override
    public int update(Mensaje mensaje) throws BLException {
        validarDatos(mensaje);
        Mensaje actual = buscarMensaje(mensaje.getIdMensaje());
        if (actual.getEmisor().getIdUsuario() != mensaje.getEmisor().getIdUsuario()) {
            throw new BLException("Solo el emisor puede editar su mensaje");
        }
        try {
            return mensajeDAO.update(mensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el mensaje", e);
        }
    }

    @Override
    public int delete(int idMensaje) throws BLException {
        buscarMensaje(idMensaje);
        try {
            return mensajeDAO.delete(idMensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete el mensaje", e);
        }
    }

    @Override
    public Mensaje findById(int idMensaje) throws BLException {
        try {
            return mensajeDAO.findById(idMensaje);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el mensaje", e);
        }
    }

    @Override
    public ArrayList<Mensaje> findAll() throws BLException {
        try {
            return mensajeDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los mensajes", e);
        }
    }

    @Override
    public ArrayList<Mensaje> listarPorChat(int idChat) throws BLException {
        try {
            return mensajeDAO.listarPorChat(idChat);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los mensajes del chat", e);
        }
    }

    @Override
    public int enviarRespuestaRapida(int idChat, int idEmisor, int idRespuestaRapida) throws BLException {
        RespuestaRapida respuesta;
        try {
            respuesta = respuestaDAO.findById(idRespuestaRapida);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la respuesta rapida", e);
        }
        if (respuesta == null || !respuesta.isActivo()) {
            throw new BLException("No existe la respuesta rapida " + idRespuestaRapida);
        }
        if (respuesta.getCreador().getIdUsuario() != idEmisor) {
            throw new BLException("Solo se pueden usar respuestas rapidas propias");
        }
        CanalChat chat = new CanalChat();
        chat.setIdChat(idChat);
        UsuarioPUCP emisor = new UsuarioPUCP();
        emisor.setIdUsuario(idEmisor);
        return insert(new Mensaje(respuesta.getTexto(), chat, emisor));
    }

    private void validarDatos(Mensaje mensaje) throws BLException {
        if (mensaje == null) {
            throw new BLException("El mensaje no puede ser nulo");
        }
        validarTexto(mensaje.getContenido(), "El mensaje no puede estar vacio");
        validarLongitudMaxima(mensaje.getContenido(), LONGITUD_MAXIMA, "El mensaje");
        if (mensaje.getCanalChat() == null) {
            throw new BLException("El mensaje debe pertenecer a un chat");
        }
        CanalChat chat;
        try {
            chat = chatDAO.findById(mensaje.getCanalChat().getIdChat());
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el chat", e);
        }
        if (chat == null || !chat.isActivo()) {
            throw new BLException("No existe el chat " + mensaje.getCanalChat().getIdChat());
        }
        if (chat.getEstado() != EstadoCanalChat.ACTIVO) {
            throw new BLException("No se pueden enviar mensajes: el chat esta " + chat.getEstado());
        }
        UsuarioPUCP emisor = buscarUsuarioHabilitado(mensaje.getEmisor(), "emisor");
        if (emisor.getIdUsuario() != chat.getComprador().getIdUsuario()
                && emisor.getIdUsuario() != chat.getAnuncio().getVendedor().getIdUsuario()) {
            throw new BLException("Solo el comprador o el vendedor pueden escribir en este chat");
        }
    }

    private Mensaje buscarMensaje(int idMensaje) throws BLException {
        Mensaje mensaje = findById(idMensaje);
        if (mensaje == null || !mensaje.isActivo()) {
            throw new BLException("No existe el mensaje con id " + idMensaje);
        }
        return mensaje;
    }
}

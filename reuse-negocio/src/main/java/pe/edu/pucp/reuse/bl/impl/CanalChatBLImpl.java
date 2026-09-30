package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CanalChatBL;
import pe.edu.pucp.reuse.dao.BloqueoDAO;
import pe.edu.pucp.reuse.dao.CanalChatDAO;
import pe.edu.pucp.reuse.dao.impl.BloqueoDAOImpl;
import pe.edu.pucp.reuse.dao.impl.CanalChatDAOImpl;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class CanalChatBLImpl extends BaseBLImpl implements CanalChatBL {

    private final CanalChatDAO chatDAO = new CanalChatDAOImpl();
    private final BloqueoDAO bloqueoDAO = new BloqueoDAOImpl();

    // Solicitud de contacto inicial; el chat nace PENDIENTE.
    @Override
    public int insert(CanalChat chat) throws BLException {
        if (chat == null) {
            throw new BLException("El chat no puede ser nulo");
        }
        Anuncio anuncio = buscarAnuncio(chat.getAnuncio());
        if (anuncio.getEstado() != EstadoAnuncio.DISPONIBLE) {
            throw new BLException("Solo se puede contactar al vendedor de un anuncio DISPONIBLE");
        }
        UsuarioPUCP comprador = buscarUsuarioHabilitado(chat.getComprador(), "comprador");
        int idVendedor = anuncio.getVendedor().getIdUsuario();
        if (idVendedor == comprador.getIdUsuario()) {
            throw new BLException("El vendedor no puede abrir un chat con su propio anuncio");
        }
        validarSinBloqueo(comprador.getIdUsuario(), idVendedor);
        try {
            if (chatDAO.obtenerAbierto(anuncio.getIdAnuncio(), comprador.getIdUsuario()) != null) {
                throw new BLException("Ya existe una solicitud o chat abierto para este anuncio");
            }
            chat.setEstado(EstadoCanalChat.PENDIENTE);
            return chatDAO.insert(chat);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la solicitud de contacto", e);
        }
    }

    // Los participantes no cambian y el estado solo cambia con aceptar, rechazar o cerrar.
    @Override
    public int update(CanalChat chat) throws BLException {
        if (chat == null) {
            throw new BLException("El chat no puede ser nulo");
        }
        CanalChat actual = buscarChat(chat.getIdChat());
        if (chat.getAnuncio() == null || chat.getComprador() == null
                || chat.getAnuncio().getIdAnuncio() != actual.getAnuncio().getIdAnuncio()
                || chat.getComprador().getIdUsuario() != actual.getComprador().getIdUsuario()) {
            throw new BLException("No se puede cambiar el anuncio ni el comprador de un chat");
        }
        chat.setEstado(actual.getEstado());
        try {
            return chatDAO.update(chat);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el chat", e);
        }
    }

    @Override
    public int delete(int idChat) throws BLException {
        buscarChat(idChat);
        try {
            return chatDAO.delete(idChat);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete el chat", e);
        }
    }

    @Override
    public CanalChat findById(int idChat) throws BLException {
        try {
            return chatDAO.findById(idChat);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el chat", e);
        }
    }

    @Override
    public ArrayList<CanalChat> findAll() throws BLException {
        try {
            return chatDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los chats", e);
        }
    }

    @Override
    public void aceptarSolicitud(int idChat) throws BLException {
        CanalChat chat = buscarChatEnEstado(idChat, EstadoCanalChat.PENDIENTE, "aceptar la solicitud");
        validarSinBloqueo(chat.getComprador().getIdUsuario(), chat.getAnuncio().getVendedor().getIdUsuario());
        try {
            chatDAO.responderSolicitud(idChat, EstadoCanalChat.ACTIVO);
        } catch (SQLException e) {
            throw new BLException("No se pudo aceptar la solicitud", e);
        }
    }

    @Override
    public void rechazarSolicitud(int idChat) throws BLException {
        buscarChatEnEstado(idChat, EstadoCanalChat.PENDIENTE, "rechazar la solicitud");
        try {
            chatDAO.responderSolicitud(idChat, EstadoCanalChat.RECHAZADO);
        } catch (SQLException e) {
            throw new BLException("No se pudo rechazar la solicitud", e);
        }
    }

    @Override
    public void cerrar(int idChat) throws BLException {
        buscarChatEnEstado(idChat, EstadoCanalChat.ACTIVO, "cerrar el chat");
        try {
            chatDAO.cerrar(idChat, EstadoCanalChat.CERRADO);
        } catch (SQLException e) {
            throw new BLException("No se pudo cerrar el chat", e);
        }
    }

    // El bloqueo es bidireccional.
    private void validarSinBloqueo(int idUsuarioA, int idUsuarioB) throws BLException {
        try {
            if (bloqueoDAO.obtenerActivoEntre(idUsuarioA, idUsuarioB) != null) {
                throw new BLException("Existe un bloqueo activo entre el comprador y el vendedor");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar los bloqueos", e);
        }
    }

    private CanalChat buscarChat(int idChat) throws BLException {
        CanalChat chat = findById(idChat);
        if (chat == null || !chat.isActivo()) {
            throw new BLException("No existe el chat con id " + idChat);
        }
        return chat;
    }

    private CanalChat buscarChatEnEstado(int idChat, EstadoCanalChat esperado, String accion) throws BLException {
        CanalChat chat = buscarChat(idChat);
        if (chat.getEstado() != esperado) {
            throw new BLException("No se puede " + accion + ": el chat esta " + chat.getEstado()
                    + " y debe estar " + esperado);
        }
        return chat;
    }
}

package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.BloqueoBL;
import pe.edu.pucp.reuse.dao.BloqueoDAO;
import pe.edu.pucp.reuse.dao.CanalChatDAO;
import pe.edu.pucp.reuse.dao.impl.BloqueoDAOImpl;
import pe.edu.pucp.reuse.dao.impl.CanalChatDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.EstadoBloqueo;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class BloqueoBLImpl extends BaseBLImpl implements BloqueoBL {

    private final BloqueoDAO bloqueoDAO = new BloqueoDAOImpl();
    private final CanalChatDAO chatDAO = new CanalChatDAOImpl();

    /**
     * RF-10: registra el bloqueo y cierra (BLOQUEADO) los chats abiertos entre
     * ambos usuarios en la misma transaccion.
     */
    @Override
    public int insert(Bloqueo bloqueo) throws BLException {
        int[] usuarios = validarUsuarios(bloqueo);
        try {
            if (bloqueoDAO.obtenerActivoEntre(usuarios[0], usuarios[1]) != null) {
                throw new BLException("Ya existe un bloqueo activo entre estos usuarios");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar los bloqueos existentes", e);
        }
        bloqueo.setEstado(EstadoBloqueo.ACTIVO);

        TransactionsManager.iniciar();
        try {
            int idBloqueo = bloqueoDAO.insert(bloqueo);
            chatDAO.bloquearChatsEntre(usuarios[0], usuarios[1]);
            TransactionsManager.commit();
            return idBloqueo;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el bloqueo; se hizo rollback", e);
        }
    }

    @Override
    public int update(Bloqueo bloqueo) throws BLException {
        validarUsuarios(bloqueo);
        Bloqueo actual = buscarBloqueo(bloqueo.getIdBloqueo());
        if (actual.getBloqueador().getIdUsuario() != bloqueo.getBloqueador().getIdUsuario()
                || actual.getBloqueado().getIdUsuario() != bloqueo.getBloqueado().getIdUsuario()) {
            throw new BLException("No se pueden cambiar los usuarios de un bloqueo");
        }
        if (bloqueo.getEstado() == null) {
            throw new BLException("El estado del bloqueo es obligatorio");
        }
        try {
            return bloqueoDAO.update(bloqueo);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el bloqueo", e);
        }
    }

    @Override
    public int delete(int idBloqueo) throws BLException {
        buscarBloqueo(idBloqueo);
        try {
            return bloqueoDAO.delete(idBloqueo);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete el bloqueo", e);
        }
    }

    @Override
    public Bloqueo findById(int idBloqueo) throws BLException {
        try {
            return bloqueoDAO.findById(idBloqueo);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el bloqueo", e);
        }
    }

    @Override
    public ArrayList<Bloqueo> findAll() throws BLException {
        try {
            return bloqueoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los bloqueos", e);
        }
    }

    @Override
    public void desbloquear(int idBloqueo) throws BLException {
        Bloqueo bloqueo = buscarBloqueo(idBloqueo);
        if (bloqueo.getEstado() != EstadoBloqueo.ACTIVO) {
            throw new BLException("El bloqueo ya esta INACTIVO");
        }
        bloqueo.setEstado(EstadoBloqueo.INACTIVO);
        try {
            bloqueoDAO.update(bloqueo);
        } catch (SQLException e) {
            throw new BLException("No se pudo desbloquear", e);
        }
    }

    // Devuelve {idBloqueador, idBloqueado}.
    private int[] validarUsuarios(Bloqueo bloqueo) throws BLException {
        if (bloqueo == null) {
            throw new BLException("El bloqueo no puede ser nulo");
        }
        UsuarioPUCP bloqueador = buscarUsuario(bloqueo.getBloqueador(), "usuario que bloquea");
        UsuarioPUCP bloqueado = buscarUsuario(bloqueo.getBloqueado(), "usuario bloqueado");
        if (bloqueador.getIdUsuario() == bloqueado.getIdUsuario()) {
            throw new BLException("Un usuario no puede bloquearse a si mismo");
        }
        return new int[] {bloqueador.getIdUsuario(), bloqueado.getIdUsuario()};
    }

    private Bloqueo buscarBloqueo(int idBloqueo) throws BLException {
        Bloqueo bloqueo = findById(idBloqueo);
        if (bloqueo == null || !bloqueo.isActivo()) {
            throw new BLException("No existe el bloqueo con id " + idBloqueo);
        }
        return bloqueo;
    }
}

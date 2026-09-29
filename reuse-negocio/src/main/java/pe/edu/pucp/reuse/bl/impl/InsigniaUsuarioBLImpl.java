package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.InsigniaUsuarioBL;
import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.InsigniaUsuarioDAO;
import pe.edu.pucp.reuse.dao.impl.InsigniaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.InsigniaUsuarioDAOImpl;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class InsigniaUsuarioBLImpl extends BaseBLImpl implements InsigniaUsuarioBL {

    private final InsigniaUsuarioDAO insigniaUsuarioDAO = new InsigniaUsuarioDAOImpl();
    private final InsigniaDAO insigniaDAO = new InsigniaDAOImpl();

    // Otorgamiento manual. La pareja usuario-insignia es UNIQUE: si existia desactivada, se reactiva.
    @Override
    public int insert(InsigniaUsuario insigniaUsuario) throws BLException {
        if (insigniaUsuario == null) {
            throw new BLException("La insignia del usuario no puede ser nula");
        }
        UsuarioPUCP usuario = buscarUsuario(insigniaUsuario.getUsuario(), "usuario");
        Insignia insignia = buscarInsigniaActiva(insigniaUsuario.getInsignia());
        try {
            InsigniaUsuario existente = insigniaUsuarioDAO.obtenerPorUsuarioEInsignia(
                    usuario.getIdUsuario(), insignia.getIdInsignia());
            if (existente != null && existente.isActivo()) {
                throw new BLException("El usuario ya tiene la insignia '" + insignia.getNombre() + "'");
            }
            if (existente != null) {
                existente.setActivo(true);
                insigniaUsuarioDAO.update(existente);
                insigniaUsuario.setIdInsigniaUsuario(existente.getIdInsigniaUsuario());
                return existente.getIdInsigniaUsuario();
            }
            return insigniaUsuarioDAO.insert(insigniaUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo otorgar la insignia", e);
        }
    }

    @Override
    public int update(InsigniaUsuario insigniaUsuario) throws BLException {
        if (insigniaUsuario == null) {
            throw new BLException("La insignia del usuario no puede ser nula");
        }
        validarExiste(insigniaUsuario.getIdInsigniaUsuario());
        buscarUsuario(insigniaUsuario.getUsuario(), "usuario");
        buscarInsigniaActiva(insigniaUsuario.getInsignia());
        try {
            return insigniaUsuarioDAO.update(insigniaUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la insignia del usuario", e);
        }
    }

    @Override
    public int delete(int idInsigniaUsuario) throws BLException {
        validarExiste(idInsigniaUsuario);
        try {
            return insigniaUsuarioDAO.delete(idInsigniaUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo retirar la insignia", e);
        }
    }

    @Override
    public InsigniaUsuario findById(int idInsigniaUsuario) throws BLException {
        try {
            return insigniaUsuarioDAO.findById(idInsigniaUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la insignia del usuario", e);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> findAll() throws BLException {
        try {
            return insigniaUsuarioDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las insignias otorgadas", e);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> listarPorUsuario(int idUsuario) throws BLException {
        try {
            return insigniaUsuarioDAO.listarPorUsuario(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las insignias del usuario", e);
        }
    }

    @Override
    public boolean otorgarSiCumple(int idUsuario, int idInsignia) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        Insignia insignia;
        try {
            insignia = insigniaDAO.findById(idInsignia);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la insignia", e);
        }
        if (insignia == null || !insignia.isActivo() || insignia.getReglas().isEmpty()) {
            return false;
        }
        try {
            InsigniaUsuario existente = insigniaUsuarioDAO.obtenerPorUsuarioEInsignia(idUsuario, idInsignia);
            if (existente != null && existente.isActivo()) {
                return false;
            }
            // Todas las reglas deben cumplirse con las metricas reales calculadas en la BD.
            for (ReglaInsignia regla : insignia.getReglas()) {
                double valorReal = usuarioDAO.obtenerMetrica(idUsuario, regla.getTipoMetrica());
                if (!regla.seCumpleCon(valorReal)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo evaluar las reglas de la insignia", e);
        }
        insert(new InsigniaUsuario(usuario, insignia));
        return true;
    }

    private Insignia buscarInsigniaActiva(Insignia referencia) throws BLException {
        if (referencia == null) {
            throw new BLException("Debe indicar la insignia");
        }
        Insignia insignia;
        try {
            insignia = insigniaDAO.findById(referencia.getIdInsignia());
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la insignia", e);
        }
        if (insignia == null || !insignia.isActivo()) {
            throw new BLException("La insignia " + referencia.getIdInsignia() + " no existe o fue retirada");
        }
        return insignia;
    }

    private void validarExiste(int idInsigniaUsuario) throws BLException {
        if (findById(idInsigniaUsuario) == null) {
            throw new BLException("No existe el otorgamiento con id " + idInsigniaUsuario);
        }
    }
}

package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.InsigniaUsuarioBL;
import pe.edu.pucp.reuse.bl.insignias.EspecificacionesInsignia;
import pe.edu.pucp.reuse.bl.insignias.MetricasUsuario;
import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.InsigniaUsuarioDAO;
import pe.edu.pucp.reuse.dao.impl.InsigniaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.InsigniaUsuarioDAOImpl;
import pe.edu.pucp.reuse.dao.impl.ReglaInsigniaDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
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
        return otorgarSiCumple(usuario, insignia, new MetricasUsuario(idUsuario, usuarioDAO));
    }

    /**
     * RF-18: recorre las insignias activas y otorga las que el usuario cumple. Las metricas se
     * calculan una sola vez (MetricasUsuario) y cada insignia se evalua como una especificacion.
     */
    @Override
    public ArrayList<Insignia> evaluarInsignias(int idUsuario) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        ArrayList<Insignia> otorgadas = new ArrayList<>();
        // Pocas consultas: catalogo, todas las reglas y las insignias que el usuario ya tiene.
        Map<Integer, Insignia> candidatas = new LinkedHashMap<>();
        Set<TipoMetricaInsignia> metricasNecesarias = EnumSet.noneOf(TipoMetricaInsignia.class);
        try {
            Set<Integer> yaObtenidas = new HashSet<>();
            for (InsigniaUsuario otorgamiento : insigniaUsuarioDAO.listarPorUsuario(idUsuario)) {
                if (otorgamiento.isActivo() && otorgamiento.getInsignia() != null) {
                    yaObtenidas.add(otorgamiento.getInsignia().getIdInsignia());
                }
            }
            for (Insignia insignia : insigniaDAO.findAll()) {
                if (insignia.isActivo() && !yaObtenidas.contains(insignia.getIdInsignia())) {
                    candidatas.put(insignia.getIdInsignia(), insignia);
                }
            }
            for (ReglaInsignia regla : new ReglaInsigniaDAOImpl().findAll()) {
                Insignia insignia = regla.getInsignia() == null ? null : candidatas.get(regla.getInsignia().getIdInsignia());
                if (regla.isActivo() && insignia != null) {
                    insignia.agregarRegla(regla);
                    metricasNecesarias.add(regla.getTipoMetrica());
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo cargar las insignias y sus reglas", e);
        }
        MetricasUsuario metricas = new MetricasUsuario(idUsuario, usuarioDAO).precargar(metricasNecesarias);
        for (Insignia insignia : candidatas.values()) {
            if (otorgarSiCumple(usuario, insignia, metricas)) {
                otorgadas.add(insignia);
                new Notificador().avisar(idUsuario, TipoNotificacion.SISTEMA,
                        "Obtuviste la insignia '" + insignia.getNombre() + "': " + insignia.getDescripcion());
            }
        }
        return otorgadas;
    }

    /**
     * Version que no interrumpe la operacion que la invoca (calificar, completar una venta):
     * si la evaluacion falla, la operacion principal ya quedo guardada y solo se registra el error.
     */
    void evaluarSinFallar(int idUsuario) {
        try {
            evaluarInsignias(idUsuario);
        } catch (BLException | RuntimeException e) {
            Logger.getLogger(InsigniaUsuarioBLImpl.class.getName())
                    .log(Level.WARNING, "No se pudo evaluar las insignias del usuario " + idUsuario, e);
        }
    }

    private boolean otorgarSiCumple(UsuarioPUCP usuario, Insignia insignia, MetricasUsuario metricas)
            throws BLException {
        if (insignia == null || !insignia.isActivo() || insignia.getReglas().isEmpty()) {
            return false;
        }
        // Todas las reglas deben cumplirse con las metricas reales calculadas en la BD.
        if (!EspecificacionesInsignia.deInsignia(insignia).esSatisfechaPor(metricas)) {
            return false;
        }
        try {
            InsigniaUsuario existente = insigniaUsuarioDAO.obtenerPorUsuarioEInsignia(
                    usuario.getIdUsuario(), insignia.getIdInsignia());
            if (existente != null && existente.isActivo()) {
                return false;
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar las insignias del usuario", e);
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

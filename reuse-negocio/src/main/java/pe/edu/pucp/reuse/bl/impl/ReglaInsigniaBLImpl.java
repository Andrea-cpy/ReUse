package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.ReglaInsigniaBL;
import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.dao.impl.InsigniaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.ReglaInsigniaDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class ReglaInsigniaBLImpl extends BaseBLImpl implements ReglaInsigniaBL {

    private final ReglaInsigniaDAO reglaDAO = new ReglaInsigniaDAOImpl();
    private final InsigniaDAO insigniaDAO = new InsigniaDAOImpl();

    @Override
    public int insert(ReglaInsignia regla) throws BLException {
        validarRegla(regla);
        validarInsignia(regla);
        try {
            return reglaDAO.insert(regla);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la regla de la insignia", e);
        }
    }

    @Override
    public int update(ReglaInsignia regla) throws BLException {
        validarRegla(regla);
        validarInsignia(regla);
        validarExiste(regla.getIdRegla());
        try {
            return reglaDAO.update(regla);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la regla de la insignia", e);
        }
    }

    @Override
    public int delete(int idRegla) throws BLException {
        validarExiste(idRegla);
        try {
            return reglaDAO.delete(idRegla);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la regla de la insignia", e);
        }
    }

    @Override
    public ReglaInsignia findById(int idRegla) throws BLException {
        try {
            return reglaDAO.findById(idRegla);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la regla de la insignia", e);
        }
    }

    @Override
    public ArrayList<ReglaInsignia> findAll() throws BLException {
        try {
            return reglaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las reglas de insignias", e);
        }
    }

    @Override
    public ArrayList<ReglaInsignia> listarPorInsignia(int idInsignia) throws BLException {
        try {
            return reglaDAO.listarPorInsignia(idInsignia);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las reglas de la insignia", e);
        }
    }

    // Tambien la usa InsigniaBLImpl al registrar una insignia con sus reglas.
    static void validarRegla(ReglaInsignia regla) throws BLException {
        if (regla == null) {
            throw new BLException("La regla no puede ser nula");
        }
        if (regla.getTipoMetrica() == null || regla.getOperador() == null) {
            throw new BLException("La regla debe indicar la metrica y el operador");
        }
        if (regla.getValorObjetivo() < 0) {
            throw new BLException("El valor objetivo de la regla no puede ser negativo");
        }
        if (regla.getTipoMetrica() == TipoMetricaInsignia.CALIFICACION_PROMEDIO && regla.getValorObjetivo() > 5) {
            throw new BLException("La calificacion promedio objetivo no puede ser mayor que 5");
        }
    }

    private void validarInsignia(ReglaInsignia regla) throws BLException {
        if (regla.getInsignia() == null) {
            throw new BLException("La regla debe pertenecer a una insignia");
        }
        try {
            Insignia insignia = insigniaDAO.findById(regla.getInsignia().getIdInsignia());
            if (insignia == null || !insignia.isActivo()) {
                throw new BLException("No existe la insignia " + regla.getInsignia().getIdInsignia());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la insignia", e);
        }
    }

    private void validarExiste(int idRegla) throws BLException {
        if (findById(idRegla) == null) {
            throw new BLException("No existe una regla con id " + idRegla);
        }
    }
}

package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.InsigniaBL;
import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.dao.impl.InsigniaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.ReglaInsigniaDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class InsigniaBLImpl extends BaseBLImpl implements InsigniaBL {

    private final InsigniaDAO insigniaDAO = new InsigniaDAOImpl();
    private final ReglaInsigniaDAO reglaDAO = new ReglaInsigniaDAOImpl();

    // Maestro-detalle: la insignia y sus reglas se guardan juntas o no se guarda nada.
    @Override
    public int insert(Insignia insignia) throws BLException {
        validarDatos(insignia);
        validarNombreUnico(insignia);
        if (insignia.getReglas().isEmpty()) {
            throw new BLException("La insignia debe tener al menos una regla para poder otorgarse");
        }
        for (ReglaInsignia regla : insignia.getReglas()) {
            ReglaInsigniaBLImpl.validarRegla(regla);
        }

        TransactionsManager.iniciar();
        try {
            int idInsignia = insigniaDAO.insert(insignia);
            for (ReglaInsignia regla : insignia.getReglas()) {
                reglaDAO.insert(regla);
            }
            TransactionsManager.commit();
            return idInsignia;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar la insignia con sus reglas; se hizo rollback", e);
        }
    }

    // Solo la cabecera; las reglas se editan con ReglaInsigniaBL.
    @Override
    public int update(Insignia insignia) throws BLException {
        validarDatos(insignia);
        validarExiste(insignia.getIdInsignia());
        validarNombreUnico(insignia);
        try {
            return insigniaDAO.update(insignia);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la insignia", e);
        }
    }

    @Override
    public int delete(int idInsignia) throws BLException {
        validarExiste(idInsignia);

        TransactionsManager.iniciar();
        try {
            for (ReglaInsignia regla : reglaDAO.listarPorInsignia(idInsignia)) {
                reglaDAO.delete(regla.getIdRegla());
            }
            int filas = insigniaDAO.delete(idInsignia);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete la insignia; se hizo rollback", e);
        }
    }

    @Override
    public Insignia findById(int idInsignia) throws BLException {
        try {
            return insigniaDAO.findById(idInsignia);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la insignia", e);
        }
    }

    @Override
    public ArrayList<Insignia> findAll() throws BLException {
        try {
            return insigniaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las insignias", e);
        }
    }

    private void validarDatos(Insignia insignia) throws BLException {
        if (insignia == null) {
            throw new BLException("La insignia no puede ser nula");
        }
        validarTexto(insignia.getNombre(), "El nombre de la insignia es obligatorio");
        validarLongitudMaxima(insignia.getNombre(), 120, "El nombre de la insignia");
        validarTexto(insignia.getDescripcion(), "La descripcion de la insignia es obligatoria");
        validarLongitudMaxima(insignia.getDescripcion(), 400, "La descripcion de la insignia");
        if (insignia.getTipo() == null) {
            throw new BLException("El tipo de insignia es obligatorio");
        }
    }

    private void validarExiste(int idInsignia) throws BLException {
        if (findById(idInsignia) == null) {
            throw new BLException("No existe una insignia con id " + idInsignia);
        }
    }

    private void validarNombreUnico(Insignia insignia) throws BLException {
        try {
            Insignia existente = insigniaDAO.obtenerPorNombre(insignia.getNombre());
            if (existente != null && existente.getIdInsignia() != insignia.getIdInsignia()) {
                throw new BLException("Ya existe una insignia llamada '" + insignia.getNombre() + "'");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del nombre de la insignia", e);
        }
    }
}

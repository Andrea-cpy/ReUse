package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.FacultadBL;
import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.dao.FacultadDAO;
import pe.edu.pucp.reuse.dao.impl.CarreraDAOImpl;
import pe.edu.pucp.reuse.dao.impl.FacultadDAOImpl;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class FacultadBLImpl extends BaseBLImpl implements FacultadBL {

    private final FacultadDAO facultadDAO = new FacultadDAOImpl();
    private final CarreraDAO carreraDAO = new CarreraDAOImpl();

    @Override
    public int insert(Facultad facultad) throws BLException {
        validarDatos(facultad);
        validarNombreUnico(facultad);
        try {
            return facultadDAO.insert(facultad);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la facultad", e);
        }
    }

    @Override
    public int update(Facultad facultad) throws BLException {
        validarDatos(facultad);
        validarExiste(facultad.getIdFacultad());
        validarNombreUnico(facultad);
        try {
            return facultadDAO.update(facultad);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la facultad", e);
        }
    }

    @Override
    public int delete(int idFacultad) throws BLException {
        validarExiste(idFacultad);
        try {
            if (!carreraDAO.listarPorFacultad(idFacultad).isEmpty()) {
                throw new BLException("No se puede delete una facultad que tiene carreras activas");
            }
            return facultadDAO.delete(idFacultad);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la facultad", e);
        }
    }

    @Override
    public Facultad findById(int idFacultad) throws BLException {
        try {
            return facultadDAO.findById(idFacultad);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la facultad", e);
        }
    }

    @Override
    public ArrayList<Facultad> findAll() throws BLException {
        try {
            return facultadDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las facultades", e);
        }
    }

    private void validarDatos(Facultad facultad) throws BLException {
        if (facultad == null) {
            throw new BLException("La facultad no puede ser nula");
        }
        validarTexto(facultad.getNombre(), "El nombre de la facultad es obligatorio");
        validarLongitudMaxima(facultad.getNombre(), 120, "El nombre de la facultad");
    }

    private void validarExiste(int idFacultad) throws BLException {
        if (findById(idFacultad) == null) {
            throw new BLException("No existe una facultad con id " + idFacultad);
        }
    }

    private void validarNombreUnico(Facultad facultad) throws BLException {
        try {
            Facultad existente = facultadDAO.obtenerPorNombre(facultad.getNombre());
            if (existente != null && existente.getIdFacultad() != facultad.getIdFacultad()) {
                throw new BLException("Ya existe una facultad con el nombre '" + facultad.getNombre() + "'");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del nombre de la facultad", e);
        }
    }
}

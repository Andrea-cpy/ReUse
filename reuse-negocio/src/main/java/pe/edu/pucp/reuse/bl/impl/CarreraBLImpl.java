package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CarreraBL;
import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.dao.FacultadDAO;
import pe.edu.pucp.reuse.dao.impl.CarreraDAOImpl;
import pe.edu.pucp.reuse.dao.impl.FacultadDAOImpl;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class CarreraBLImpl extends BaseBLImpl implements CarreraBL {

    private final CarreraDAO carreraDAO = new CarreraDAOImpl();
    private final FacultadDAO facultadDAO = new FacultadDAOImpl();

    @Override
    public int insert(Carrera carrera) throws BLException {
        validarDatos(carrera);
        validarNombreUnico(carrera);
        try {
            return carreraDAO.insert(carrera);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la carrera", e);
        }
    }

    @Override
    public int update(Carrera carrera) throws BLException {
        validarDatos(carrera);
        validarExiste(carrera.getIdCarrera());
        validarNombreUnico(carrera);
        try {
            return carreraDAO.update(carrera);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la carrera", e);
        }
    }

    @Override
    public int delete(int idCarrera) throws BLException {
        validarExiste(idCarrera);
        try {
            return carreraDAO.delete(idCarrera);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la carrera", e);
        }
    }

    @Override
    public Carrera findById(int idCarrera) throws BLException {
        try {
            return carreraDAO.findById(idCarrera);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la carrera", e);
        }
    }

    @Override
    public ArrayList<Carrera> findAll() throws BLException {
        try {
            return carreraDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las carreras", e);
        }
    }

    @Override
    public ArrayList<Carrera> listarPorFacultad(int idFacultad) throws BLException {
        try {
            return carreraDAO.listarPorFacultad(idFacultad);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las carreras de la facultad", e);
        }
    }

    private void validarDatos(Carrera carrera) throws BLException {
        if (carrera == null) {
            throw new BLException("La carrera no puede ser nula");
        }
        validarTexto(carrera.getNombre(), "El nombre de la carrera es obligatorio");
        validarLongitudMaxima(carrera.getNombre(), 120, "El nombre de la carrera");
        if (carrera.getFacultad() == null) {
            throw new BLException("La carrera debe pertenecer a una facultad");
        }
        try {
            Facultad facultad = facultadDAO.findById(carrera.getFacultad().getIdFacultad());
            if (facultad == null || !facultad.isActivo()) {
                throw new BLException("No existe la facultad " + carrera.getFacultad().getIdFacultad());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la facultad", e);
        }
    }

    private void validarExiste(int idCarrera) throws BLException {
        if (findById(idCarrera) == null) {
            throw new BLException("No existe una carrera con id " + idCarrera);
        }
    }

    private void validarNombreUnico(Carrera carrera) throws BLException {
        try {
            Carrera existente = carreraDAO.obtenerPorNombre(carrera.getNombre());
            if (existente != null && existente.getIdCarrera() != carrera.getIdCarrera()) {
                throw new BLException("Ya existe una carrera con el nombre '" + carrera.getNombre() + "'");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del nombre de la carrera", e);
        }
    }
}

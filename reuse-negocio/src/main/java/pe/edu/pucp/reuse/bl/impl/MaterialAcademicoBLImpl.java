package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.MaterialAcademicoBL;
import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.dao.CategoriaMaterialDAO;
import pe.edu.pucp.reuse.dao.MaterialAcademicoDAO;
import pe.edu.pucp.reuse.dao.impl.CarreraDAOImpl;
import pe.edu.pucp.reuse.dao.impl.CategoriaMaterialDAOImpl;
import pe.edu.pucp.reuse.dao.impl.MaterialAcademicoDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;

public class MaterialAcademicoBLImpl extends BaseBLImpl implements MaterialAcademicoBL {

    private final MaterialAcademicoDAO materialDAO = new MaterialAcademicoDAOImpl();
    private final CategoriaMaterialDAO categoriaDAO = new CategoriaMaterialDAOImpl();
    private final CarreraDAO carreraDAO = new CarreraDAOImpl();

    // material_academico + material_carrera: ambas tablas o ninguna.
    @Override
    public int insert(MaterialAcademico material) throws BLException {
        validarDatos(material);

        TransactionsManager.iniciar();
        try {
            int idMaterial = materialDAO.insert(material);
            TransactionsManager.commit();
            return idMaterial;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el material academico con sus carreras", e);
        }
    }

    @Override
    public int update(MaterialAcademico material) throws BLException {
        validarDatos(material);
        validarExiste(material.getIdMaterial());

        TransactionsManager.iniciar();
        try {
            int filas = materialDAO.update(material);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo actualizar el material academico", e);
        }
    }

    @Override
    public int delete(int idMaterial) throws BLException {
        validarExiste(idMaterial);

        TransactionsManager.iniciar();
        try {
            int filas = materialDAO.delete(idMaterial);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete el material academico", e);
        }
    }

    @Override
    public MaterialAcademico findById(int idMaterial) throws BLException {
        try {
            return materialDAO.findById(idMaterial);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el material academico", e);
        }
    }

    @Override
    public ArrayList<MaterialAcademico> findAll() throws BLException {
        try {
            return materialDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los materiales academicos", e);
        }
    }

    private void validarDatos(MaterialAcademico material) throws BLException {
        if (material == null) {
            throw new BLException("El material academico no puede ser nulo");
        }
        validarTexto(material.getTitulo(), "El titulo del material es obligatorio");
        validarLongitudMaxima(material.getTitulo(), 200, "El titulo del material");
        if (material.getCategoria() == null) {
            throw new BLException("El material debe tener una categoria");
        }
        // Obs. 5 de la JP: un material se relaciona con una o mas carreras.
        if (material.getCarreras().isEmpty()) {
            throw new BLException("El material debe estar asociado al menos a una carrera");
        }
        try {
            CategoriaMaterial categoria = categoriaDAO.findById(material.getCategoria().getIdCategoria());
            if (categoria == null || !categoria.isActivo()) {
                throw new BLException("La categoria " + material.getCategoria().getIdCategoria()
                        + " no existe o no esta disponible para nuevas publicaciones");
            }
            Set<Integer> idsCarreras = new HashSet<>();
            for (Carrera carrera : material.getCarreras()) {
                if (!idsCarreras.add(carrera.getIdCarrera())) {
                    throw new BLException("La carrera " + carrera.getIdCarrera() + " esta repetida en el material");
                }
                Carrera existente = carreraDAO.findById(carrera.getIdCarrera());
                if (existente == null || !existente.isActivo()) {
                    throw new BLException("No existe la carrera " + carrera.getIdCarrera());
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la categoria o las carreras del material", e);
        }
    }

    private void validarExiste(int idMaterial) throws BLException {
        if (findById(idMaterial) == null) {
            throw new BLException("No existe un material academico con id " + idMaterial);
        }
    }
}

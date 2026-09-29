package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CategoriaMaterialBL;
import pe.edu.pucp.reuse.dao.CategoriaMaterialDAO;
import pe.edu.pucp.reuse.dao.impl.CategoriaMaterialDAOImpl;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;

public class CategoriaMaterialBLImpl extends BaseBLImpl implements CategoriaMaterialBL {

    private final CategoriaMaterialDAO categoriaDAO = new CategoriaMaterialDAOImpl();

    @Override
    public int insert(CategoriaMaterial categoria) throws BLException {
        validarDatos(categoria);
        validarNombreUnico(categoria);
        try {
            return categoriaDAO.insert(categoria);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la categoria", e);
        }
    }

    @Override
    public int update(CategoriaMaterial categoria) throws BLException {
        validarDatos(categoria);
        validarExiste(categoria.getIdCategoria());
        validarNombreUnico(categoria);
        try {
            return categoriaDAO.update(categoria);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la categoria", e);
        }
    }

    @Override
    public int delete(int idCategoria) throws BLException {
        validarExiste(idCategoria);
        try {
            return categoriaDAO.delete(idCategoria);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la categoria", e);
        }
    }

    @Override
    public CategoriaMaterial findById(int idCategoria) throws BLException {
        try {
            return categoriaDAO.findById(idCategoria);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la categoria", e);
        }
    }

    @Override
    public ArrayList<CategoriaMaterial> findAll() throws BLException {
        try {
            return categoriaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las categorias", e);
        }
    }

    private void validarDatos(CategoriaMaterial categoria) throws BLException {
        if (categoria == null) {
            throw new BLException("La categoria no puede ser nula");
        }
        validarTexto(categoria.getNombre(), "El nombre de la categoria es obligatorio");
        validarLongitudMaxima(categoria.getNombre(), 80, "El nombre de la categoria");
        validarLongitudMaxima(categoria.getDescripcion(), 300, "La descripcion de la categoria");
    }

    private void validarExiste(int idCategoria) throws BLException {
        if (findById(idCategoria) == null) {
            throw new BLException("No existe una categoria con id " + idCategoria);
        }
    }

    private void validarNombreUnico(CategoriaMaterial categoria) throws BLException {
        try {
            CategoriaMaterial existente = categoriaDAO.obtenerPorNombre(categoria.getNombre());
            if (existente != null && existente.getIdCategoria() != categoria.getIdCategoria()) {
                throw new BLException("Ya existe una categoria con el nombre '" + categoria.getNombre() + "'");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del nombre de la categoria", e);
        }
    }
}

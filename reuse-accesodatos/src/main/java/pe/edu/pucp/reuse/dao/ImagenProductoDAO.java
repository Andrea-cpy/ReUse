package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;

public interface ImagenProductoDAO extends DAO<ImagenProducto> {

    ArrayList<ImagenProducto> listarPorAnuncio(int idAnuncio) throws SQLException;

    int eliminarPorAnuncio(int idAnuncio) throws SQLException;
}

package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;

public interface CategoriaMaterialDAO extends DAO<CategoriaMaterial> {

    CategoriaMaterial obtenerPorNombre(String nombre) throws SQLException;
}

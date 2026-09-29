package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;

public interface FavoritoDAO extends DAO<Favorito> {

    Favorito obtenerActivo(int idUsuario, int idAnuncio) throws SQLException;

    ArrayList<Favorito> listarPorUsuario(int idUsuario) throws SQLException;
}

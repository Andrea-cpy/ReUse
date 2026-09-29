package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.FavoritoBL;
import pe.edu.pucp.reuse.dao.FavoritoDAO;
import pe.edu.pucp.reuse.dao.impl.FavoritoDAOImpl;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class FavoritoBLImpl extends BaseBLImpl implements FavoritoBL {

    private final FavoritoDAO favoritoDAO = new FavoritoDAOImpl();

    // RF-07: guardar un anuncio en favoritos (una sola vez y no el propio).
    @Override
    public int insert(Favorito favorito) throws BLException {
        validarDatos(favorito);
        try {
            if (favoritoDAO.obtenerActivo(favorito.getUsuario().getIdUsuario(),
                    favorito.getAnuncio().getIdAnuncio()) != null) {
                throw new BLException("El anuncio ya esta en los favoritos del usuario");
            }
            return favoritoDAO.insert(favorito);
        } catch (SQLException e) {
            throw new BLException("No se pudo guardar el favorito", e);
        }
    }

    @Override
    public int update(Favorito favorito) throws BLException {
        validarDatos(favorito);
        validarExiste(favorito.getIdFavorito());
        try {
            return favoritoDAO.update(favorito);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el favorito", e);
        }
    }

    // RF-07: quitar de favoritos.
    @Override
    public int delete(int idFavorito) throws BLException {
        validarExiste(idFavorito);
        try {
            return favoritoDAO.delete(idFavorito);
        } catch (SQLException e) {
            throw new BLException("No se pudo quitar el favorito", e);
        }
    }

    @Override
    public Favorito findById(int idFavorito) throws BLException {
        try {
            return favoritoDAO.findById(idFavorito);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el favorito", e);
        }
    }

    @Override
    public ArrayList<Favorito> findAll() throws BLException {
        try {
            return favoritoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los favoritos", e);
        }
    }

    @Override
    public ArrayList<Favorito> listarPorUsuario(int idUsuario) throws BLException {
        try {
            return favoritoDAO.listarPorUsuario(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los favoritos del usuario", e);
        }
    }

    private void validarDatos(Favorito favorito) throws BLException {
        if (favorito == null) {
            throw new BLException("El favorito no puede ser nulo");
        }
        UsuarioPUCP usuario = buscarUsuario(favorito.getUsuario(), "usuario");
        Anuncio anuncio = buscarAnuncio(favorito.getAnuncio());
        if (anuncio.getVendedor().getIdUsuario() == usuario.getIdUsuario()) {
            throw new BLException("Un usuario no puede guardar su propio anuncio en favoritos");
        }
    }

    private void validarExiste(int idFavorito) throws BLException {
        if (findById(idFavorito) == null) {
            throw new BLException("No existe un favorito con id " + idFavorito);
        }
    }
}

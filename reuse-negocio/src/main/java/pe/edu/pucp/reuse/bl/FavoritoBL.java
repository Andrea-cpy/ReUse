package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;

public interface FavoritoBL extends RegistroBL<Favorito> {

    ArrayList<Favorito> listarPorUsuario(int idUsuario) throws BLException;
}

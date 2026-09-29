package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;

public interface ImagenProductoBL extends RegistroBL<ImagenProducto> {

    ArrayList<ImagenProducto> listarPorAnuncio(int idAnuncio) throws BLException;
}

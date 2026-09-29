package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.ImagenProductoBL;
import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.dao.impl.ImagenProductoDAOImpl;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;

public class ImagenProductoBLImpl extends BaseBLImpl implements ImagenProductoBL {

    static final int MAXIMO_IMAGENES_POR_ANUNCIO = 5;
    private static final long PESO_MAXIMO_BYTES = 5L * 1024 * 1024;
    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("jpg", "jpeg", "png", "webp");

    private final ImagenProductoDAO imagenDAO = new ImagenProductoDAOImpl();

    @Override
    public int insert(ImagenProducto imagen) throws BLException {
        validarImagen(imagen);
        Anuncio anuncio = validarAnuncioEditable(imagen.getAnuncio());
        if (listarPorAnuncio(anuncio.getIdAnuncio()).size() >= MAXIMO_IMAGENES_POR_ANUNCIO) {
            throw new BLException("Un anuncio puede tener como maximo " + MAXIMO_IMAGENES_POR_ANUNCIO + " imagenes");
        }
        try {
            return imagenDAO.insert(imagen);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la imagen", e);
        }
    }

    @Override
    public int update(ImagenProducto imagen) throws BLException {
        validarImagen(imagen);
        validarExiste(imagen.getIdImagen());
        validarAnuncioEditable(imagen.getAnuncio());
        try {
            return imagenDAO.update(imagen);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la imagen", e);
        }
    }

    @Override
    public int delete(int idImagen) throws BLException {
        validarExiste(idImagen);
        try {
            return imagenDAO.delete(idImagen);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la imagen", e);
        }
    }

    @Override
    public ImagenProducto findById(int idImagen) throws BLException {
        try {
            return imagenDAO.findById(idImagen);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la imagen", e);
        }
    }

    @Override
    public ArrayList<ImagenProducto> findAll() throws BLException {
        try {
            return imagenDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las imagenes", e);
        }
    }

    @Override
    public ArrayList<ImagenProducto> listarPorAnuncio(int idAnuncio) throws BLException {
        try {
            return imagenDAO.listarPorAnuncio(idAnuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las imagenes del anuncio", e);
        }
    }

    // Tambien la usa AnuncioBLImpl al registrar un anuncio con sus imagenes.
    static void validarImagen(ImagenProducto imagen) throws BLException {
        if (imagen == null) {
            throw new BLException("La imagen no puede ser nula");
        }
        validarTexto(imagen.getUrl(), "La URL de la imagen es obligatoria");
        if (!imagen.getUrl().startsWith("http://") && !imagen.getUrl().startsWith("https://")) {
            throw new BLException("La URL de la imagen debe empezar con http:// o https://");
        }
        if (imagen.getPesoBytes() <= 0 || imagen.getPesoBytes() > PESO_MAXIMO_BYTES) {
            throw new BLException("La imagen debe pesar entre 1 byte y 5 MB");
        }
        if (imagen.getFormato() == null || !FORMATOS_PERMITIDOS.contains(imagen.getFormato().toLowerCase())) {
            throw new BLException("Formato de imagen no permitido: " + imagen.getFormato()
                    + " (use jpg, jpeg, png o webp)");
        }
    }

    private Anuncio validarAnuncioEditable(Anuncio referencia) throws BLException {
        Anuncio anuncio = buscarAnuncio(referencia);
        if (anuncio.getEstado() == EstadoAnuncio.VENDIDO || anuncio.getEstado() == EstadoAnuncio.ARCHIVADO) {
            throw new BLException("No se pueden cambiar las imagenes de un anuncio " + anuncio.getEstado());
        }
        return anuncio;
    }

    private void validarExiste(int idImagen) throws BLException {
        if (findById(idImagen) == null) {
            throw new BLException("No existe una imagen con id " + idImagen);
        }
    }
}

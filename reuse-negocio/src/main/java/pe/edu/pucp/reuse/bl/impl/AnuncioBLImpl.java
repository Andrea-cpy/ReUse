package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.AnuncioBL;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.dao.MaterialAcademicoDAO;
import pe.edu.pucp.reuse.dao.impl.ImagenProductoDAOImpl;
import pe.edu.pucp.reuse.dao.impl.MaterialAcademicoDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;

public class AnuncioBLImpl extends BaseBLImpl implements AnuncioBL {

    private final ImagenProductoDAO imagenDAO = new ImagenProductoDAOImpl();
    private final MaterialAcademicoDAO materialDAO = new MaterialAcademicoDAOImpl();

    /**
     * Registra el anuncio y todas sus imagenes en una sola transaccion: si
     * alguna imagen falla al guardarse, se hace rollback y el anuncio tampoco
     * queda registrado.
     */
    @Override
    public int insert(Anuncio anuncio) throws BLException {
        validarDatos(anuncio);
        if (anuncio.getImagenes().size() > ImagenProductoBLImpl.MAXIMO_IMAGENES_POR_ANUNCIO) {
            throw new BLException("Un anuncio puede tener como maximo "
                    + ImagenProductoBLImpl.MAXIMO_IMAGENES_POR_ANUNCIO + " imagenes");
        }
        for (ImagenProducto imagen : anuncio.getImagenes()) {
            ImagenProductoBLImpl.validarImagen(imagen);
        }
        anuncio.setEstado(EstadoAnuncio.DISPONIBLE);

        TransactionsManager.iniciar();
        try {
            int idAnuncio = anuncioDAO.insert(anuncio);
            for (ImagenProducto imagen : anuncio.getImagenes()) {
                imagenDAO.insert(imagen);
            }
            TransactionsManager.commit();
            return idAnuncio;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el anuncio con sus imagenes; se hizo rollback", e);
        }
    }

    // El estado no se edita aqui: cambia solo con las operaciones de negocio
    // (confirmar cita, confirmar entrega, moderacion).
    @Override
    public int update(Anuncio anuncio) throws BLException {
        validarDatos(anuncio);
        Anuncio actual = buscarAnuncio(anuncio.getIdAnuncio());
        if (actual.getVendedor().getIdUsuario() != anuncio.getVendedor().getIdUsuario()) {
            throw new BLException("No se puede cambiar el vendedor de un anuncio");
        }
        if (actual.getEstado() == EstadoAnuncio.VENDIDO || actual.getEstado() == EstadoAnuncio.ARCHIVADO) {
            throw new BLException("No se puede editar un anuncio " + actual.getEstado());
        }
        anuncio.setEstado(actual.getEstado());
        try {
            return anuncioDAO.update(anuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el anuncio", e);
        }
    }

    // RF-02: solo se eliminan anuncios sin transacciones; se eliminan tambien sus imagenes.
    @Override
    public int delete(int idAnuncio) throws BLException {
        buscarAnuncio(idAnuncio);
        try {
            if (anuncioDAO.contarTransacciones(idAnuncio) > 0) {
                throw new BLException("No se puede delete un anuncio que tiene transacciones");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar las transacciones del anuncio", e);
        }

        TransactionsManager.iniciar();
        try {
            imagenDAO.eliminarPorAnuncio(idAnuncio);
            int filas = anuncioDAO.delete(idAnuncio);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete el anuncio", e);
        }
    }

    @Override
    public Anuncio findById(int idAnuncio) throws BLException {
        try {
            return anuncioDAO.findById(idAnuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el anuncio", e);
        }
    }

    @Override
    public ArrayList<Anuncio> findAll() throws BLException {
        try {
            return anuncioDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los anuncios", e);
        }
    }

    private void validarDatos(Anuncio anuncio) throws BLException {
        if (anuncio == null) {
            throw new BLException("El anuncio no puede ser nulo");
        }
        validarTexto(anuncio.getTitulo(), "El titulo del anuncio es obligatorio");
        validarLongitudMaxima(anuncio.getTitulo(), 150, "El titulo del anuncio");
        validarTexto(anuncio.getDescripcion(), "La descripcion del anuncio es obligatoria");
        if (anuncio.getPrecio() <= 0) {
            throw new BLException("El precio del anuncio debe ser mayor que cero");
        }
        if (anuncio.getCondicion() == null) {
            throw new BLException("La condicion del material es obligatoria");
        }
        // Solo publica un usuario con la cuenta ACTIVA (verificada y no suspendida).
        buscarUsuarioHabilitado(anuncio.getVendedor(), "vendedor");
        if (anuncio.getMaterialAcademico() == null) {
            throw new BLException("El anuncio debe indicar el material academico");
        }
        try {
            MaterialAcademico material = materialDAO.findById(anuncio.getMaterialAcademico().getIdMaterial());
            if (material == null || !material.isActivo()) {
                throw new BLException("No existe el material academico " + anuncio.getMaterialAcademico().getIdMaterial());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el material academico", e);
        }
    }
}

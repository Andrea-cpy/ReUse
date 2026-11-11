package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
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

    // Solo se eliminan anuncios sin transacciones; se eliminan tambien sus imagenes.
    @Override
    public int delete(int idAnuncio) throws BLException {
        buscarAnuncio(idAnuncio);
        try {
            if (anuncioDAO.contarTransacciones(idAnuncio) > 0) {
                throw new BLException("No se puede eliminar un anuncio que tiene transacciones");
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
            throw new BLException("No se pudo eliminar el anuncio", e);
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


    @Override
    public ArrayList<Anuncio> listarPorVendedor(int idVendedor) throws BLException {
        ArrayList<Anuncio> resultado = new ArrayList<>();
        for (Anuncio anuncio : findAll()) {
            if (anuncio.getVendedor() != null && anuncio.getVendedor().getIdUsuario() == idVendedor) {
                resultado.add(anuncio);
            }
        }
        return resultado;
    }

    @Override
    public ArrayList<Anuncio> buscar(String texto, int idCategoria, CondicionMaterial condicion,
                                     Double precioMinimo, Double precioMaximo, int idUsuarioExcluido)
            throws BLException {
        if (precioMinimo != null && precioMaximo != null && precioMinimo > precioMaximo) {
            throw new BLException("El precio minimo no puede ser mayor que el precio maximo");
        }
        Map<Integer, MaterialAcademico> materiales = materialesPorId();
        String textoBuscado = texto == null ? "" : texto.trim().toLowerCase();
        ArrayList<Anuncio> resultado = new ArrayList<>();
        for (Anuncio anuncio : findAll()) {
            if (anuncio.getEstado() != EstadoAnuncio.DISPONIBLE) {
                continue;
            }
            if (idUsuarioExcluido > 0 && anuncio.getVendedor().getIdUsuario() == idUsuarioExcluido) {
                continue;
            }
            // El listado solo trae id y titulo del material: se completa con su categoria.
            MaterialAcademico material = materiales.get(anuncio.getMaterialAcademico().getIdMaterial());
            if (material != null) {
                anuncio.setMaterialAcademico(material);
            }
            if (!textoBuscado.isEmpty() && !contieneTexto(anuncio, textoBuscado)) {
                continue;
            }
            if (idCategoria > 0 && (material == null || material.getCategoria() == null
                    || material.getCategoria().getIdCategoria() != idCategoria)) {
                continue;
            }
            if (condicion != null && anuncio.getCondicion() != condicion) {
                continue;
            }
            if (precioMinimo != null && anuncio.getPrecio() < precioMinimo) {
                continue;
            }
            if (precioMaximo != null && anuncio.getPrecio() > precioMaximo) {
                continue;
            }
            resultado.add(anuncio);
        }
        return resultado;
    }

    @Override
    public void archivar(int idAnuncio, int idVendedor) throws BLException {
        cambiarEstadoPropio(idAnuncio, idVendedor, EstadoAnuncio.DISPONIBLE, EstadoAnuncio.ARCHIVADO);
    }

    @Override
    public void republicar(int idAnuncio, int idVendedor) throws BLException {
        cambiarEstadoPropio(idAnuncio, idVendedor, EstadoAnuncio.ARCHIVADO, EstadoAnuncio.DISPONIBLE);
    }

    private void cambiarEstadoPropio(int idAnuncio, int idVendedor, EstadoAnuncio desde, EstadoAnuncio hacia)
            throws BLException {
        Anuncio anuncio = findById(idAnuncio);
        if (anuncio == null || !anuncio.isActivo()) {
            throw new BLException("No existe el anuncio con id " + idAnuncio);
        }
        if (anuncio.getVendedor().getIdUsuario() != idVendedor) {
            throw new BLException("Solo el vendedor puede cambiar el estado de su anuncio");
        }
        if (anuncio.getEstado() != desde) {
            throw new BLException("El anuncio debe estar " + desde + " (estado actual: " + anuncio.getEstado() + ")");
        }
        try {
            if (anuncioDAO.cambiarEstado(idAnuncio, desde, hacia) == 0) {
                throw new BLException("El anuncio cambio de estado mientras se procesaba; intente de nuevo");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo cambiar el estado del anuncio", e);
        }
    }

    private Map<Integer, MaterialAcademico> materialesPorId() throws BLException {
        Map<Integer, MaterialAcademico> materiales = new HashMap<>();
        try {
            for (MaterialAcademico material : materialDAO.findAll()) {
                materiales.put(material.getIdMaterial(), material);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los materiales academicos", e);
        }
        return materiales;
    }

    private static boolean contieneTexto(Anuncio anuncio, String texto) {
        return contiene(anuncio.getTitulo(), texto) || contiene(anuncio.getDescripcion(), texto)
                || (anuncio.getMaterialAcademico() != null && contiene(anuncio.getMaterialAcademico().getTitulo(), texto));
    }

    private static boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
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

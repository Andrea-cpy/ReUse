package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.OfertaBL;
import pe.edu.pucp.reuse.dao.OfertaDAO;
import pe.edu.pucp.reuse.dao.impl.OfertaDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class OfertaBLImpl extends BaseBLImpl implements OfertaBL {

    private final OfertaDAO ofertaDAO = new OfertaDAOImpl();

    @Override
    public int insert(Oferta oferta) throws BLException {
        validarDatos(oferta);
        oferta.setEstado(EstadoOferta.PENDIENTE);
        try {
            return ofertaDAO.insert(oferta);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la oferta", e);
        }
    }

    // Solo se puede cambiar el monto de una oferta que sigue PENDIENTE.
    @Override
    public int update(Oferta oferta) throws BLException {
        validarDatos(oferta);
        Oferta actual = buscarOferta(oferta.getIdOferta());
        if (actual.getEstado() != EstadoOferta.PENDIENTE) {
            throw new BLException("Solo se puede actualizar una oferta PENDIENTE (estado actual: "
                    + actual.getEstado() + ")");
        }
        oferta.setEstado(actual.getEstado());
        try {
            return ofertaDAO.update(oferta);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la oferta", e);
        }
    }

    @Override
    public int delete(int idOferta) throws BLException {
        Oferta actual = buscarOferta(idOferta);
        if (actual.getEstado() == EstadoOferta.ACEPTADA) {
            throw new BLException("No se puede eliminar una oferta ACEPTADA: ya dio origen a una transaccion");
        }
        try {
            return ofertaDAO.delete(idOferta);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la oferta", e);
        }
    }

    @Override
    public Oferta findById(int idOferta) throws BLException {
        try {
            return ofertaDAO.findById(idOferta);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la oferta", e);
        }
    }

    @Override
    public ArrayList<Oferta> findAll() throws BLException {
        try {
            return ofertaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las ofertas", e);
        }
    }

    @Override
    public ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws BLException {
        try {
            return ofertaDAO.listarPorAnuncio(idAnuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las ofertas del anuncio", e);
        }
    }

    /**
     * Operacion compuesta (tres escrituras): acepta la oferta, rechaza las demas
     * ofertas pendientes y crea la transaccion (o vincula la oferta a la
     * negociacion que el comprador ya tenia abierta). Si cualquiera falla, rollback.
     */
    @Override
    public int aceptar(int idOferta) throws BLException {
        Oferta oferta = buscarOferta(idOferta);
        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new BLException("Solo se puede aceptar una oferta PENDIENTE (estado actual: "
                    + oferta.getEstado() + ")");
        }
        Anuncio anuncio = buscarAnuncio(oferta.getAnuncio().getIdAnuncio());
        if (anuncio.getEstado() != EstadoAnuncio.DISPONIBLE) {
            throw new BLException("El anuncio ya no esta DISPONIBLE (estado: " + anuncio.getEstado() + ")");
        }
        Transaccion negociacion = buscarNegociacionAbierta(anuncio.getIdAnuncio(),
                oferta.getComprador().getIdUsuario());

        TransactionsManager.iniciar();
        try {
            ofertaDAO.cambiarEstado(idOferta, EstadoOferta.ACEPTADA);
            ofertaDAO.rechazarPendientes(anuncio.getIdAnuncio(), idOferta);
            int idTransaccion;
            if (negociacion == null) {
                Transaccion transaccion = new Transaccion(anuncio, oferta.getComprador());
                transaccion.setOferta(oferta);
                idTransaccion = transaccionDAO.insert(transaccion);
            } else {
                negociacion.setOferta(oferta);
                transaccionDAO.update(negociacion);
                idTransaccion = negociacion.getIdTransaccion();
            }
            TransactionsManager.commit();
            oferta.setEstado(EstadoOferta.ACEPTADA);
            return idTransaccion;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo aceptar la oferta; se hizo rollback", e);
        }
    }

    @Override
    public void rechazar(int idOferta) throws BLException {
        Oferta oferta = buscarOferta(idOferta);
        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new BLException("Solo se puede rechazar una oferta PENDIENTE");
        }
        try {
            ofertaDAO.cambiarEstado(idOferta, EstadoOferta.RECHAZADA);
        } catch (SQLException e) {
            throw new BLException("No se pudo rechazar la oferta", e);
        }
    }

    // Monto mayor a cero y no mayor al precio publicado; no se oferta por el propio anuncio.

    @Override
    public ArrayList<Oferta> listarPorComprador(int idComprador) throws BLException {
        ArrayList<Oferta> resultado = new ArrayList<>();
        for (Oferta oferta : findAll()) {
            if (oferta.getComprador() != null && oferta.getComprador().getIdUsuario() == idComprador) {
                resultado.add(oferta);
            }
        }
        return resultado;
    }

    @Override
    public void contraofertar(int idOferta, double nuevoMonto) throws BLException {
        Oferta oferta = buscarOferta(idOferta);
        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new BLException("Solo se puede contraofertar una oferta PENDIENTE (estado actual: "
                    + oferta.getEstado() + ")");
        }
        Anuncio anuncio = buscarAnuncio(oferta.getAnuncio().getIdAnuncio());
        if (nuevoMonto <= oferta.getMontoPropuesto()) {
            throw new BLException("La contraoferta debe ser mayor que lo ofrecido (S/ "
                    + String.format("%.2f", oferta.getMontoPropuesto()) + ")");
        }
        if (nuevoMonto > anuncio.getPrecio()) {
            throw new BLException(String.format("La contraoferta (S/ %.2f) no puede superar el precio publicado (S/ %.2f)",
                    nuevoMonto, anuncio.getPrecio()));
        }
        oferta.setMontoPropuesto(nuevoMonto);
        oferta.setEstado(EstadoOferta.CONTRAOFERTADA);
        try {
            ofertaDAO.update(oferta);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la contraoferta", e);
        }
    }

    @Override
    public int responderContraoferta(int idOferta, boolean acepta) throws BLException {
        Oferta oferta = buscarOferta(idOferta);
        if (oferta.getEstado() != EstadoOferta.CONTRAOFERTADA) {
            throw new BLException("La oferta no tiene una contraoferta pendiente (estado actual: "
                    + oferta.getEstado() + ")");
        }
        try {
            if (!acepta) {
                ofertaDAO.cambiarEstado(idOferta, EstadoOferta.RECHAZADA);
                return 0;
            }
            // Vuelve a PENDIENTE con el monto del vendedor y se acepta con el flujo normal.
            ofertaDAO.cambiarEstado(idOferta, EstadoOferta.PENDIENTE);
        } catch (SQLException e) {
            throw new BLException("No se pudo responder la contraoferta", e);
        }
        return aceptar(idOferta);
    }

    private void validarDatos(Oferta oferta) throws BLException {
        if (oferta == null) {
            throw new BLException("La oferta no puede ser nula");
        }
        if (oferta.getMontoPropuesto() <= 0) {
            throw new BLException("El monto de la oferta debe ser mayor que cero");
        }
        UsuarioPUCP comprador = buscarUsuarioHabilitado(oferta.getComprador(), "comprador");
        Anuncio anuncio = buscarAnuncio(oferta.getAnuncio());
        if (anuncio.getEstado() != EstadoAnuncio.DISPONIBLE) {
            throw new BLException("Solo se puede ofertar por un anuncio DISPONIBLE (estado: " + anuncio.getEstado() + ")");
        }
        if (anuncio.getVendedor().getIdUsuario() == comprador.getIdUsuario()) {
            throw new BLException("El vendedor no puede ofertar por su propio anuncio");
        }
        // RF-10: con un bloqueo activo (en cualquier sentido) no se puede negociar.
        if (new BloqueoBLImpl().obtenerActivoEntre(comprador.getIdUsuario(), anuncio.getVendedor().getIdUsuario()) != null) {
            throw new BLException("No puedes ofertar: existe un bloqueo activo entre tu cuenta y la del vendedor");
        }
        if (oferta.getMontoPropuesto() > anuncio.getPrecio()) {
            throw new BLException(String.format("La oferta (S/ %.2f) no puede superar el precio publicado (S/ %.2f)",
                    oferta.getMontoPropuesto(), anuncio.getPrecio()));
        }
    }

    private Transaccion buscarNegociacionAbierta(int idAnuncio, int idComprador) throws BLException {
        try {
            for (Transaccion transaccion : transaccionDAO.listarPorAnuncio(idAnuncio)) {
                if (transaccion.getComprador().getIdUsuario() == idComprador
                        && transaccion.getEstado() == EstadoTransaccion.EN_NEGOCIACION) {
                    return transaccion;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar las negociaciones del anuncio", e);
        }
    }

    private Oferta buscarOferta(int idOferta) throws BLException {
        Oferta oferta = findById(idOferta);
        if (oferta == null || !oferta.isActivo()) {
            throw new BLException("No existe la oferta con id " + idOferta);
        }
        return oferta;
    }
}

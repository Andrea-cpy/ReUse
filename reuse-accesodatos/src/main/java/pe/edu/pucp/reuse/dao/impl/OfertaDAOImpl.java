package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.OfertaDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;

public class OfertaDAOImpl extends RegistroDAOImpl<Oferta> implements OfertaDAO {

    // La fecha de la oferta no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Oferta oferta) throws SQLException {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta no puede ser nula");
        }
        String sql = "{call insertar_oferta(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setDouble("p_monto_propuesto", oferta.getMontoPropuesto());
            cmd.setString("p_estado", oferta.getEstado().name());
            cmd.setInt("p_id_comprador", oferta.getComprador().getIdUsuario());
            cmd.setInt("p_id_anuncio", oferta.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", oferta.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(oferta.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            oferta.setIdOferta(cmd.getInt("p_id"));
            return oferta.getIdOferta();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Oferta oferta) throws SQLException {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta no puede ser nula");
        }
        String sql = "{call modificar_oferta(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", oferta.getIdOferta());
            cmd.setDouble("p_monto_propuesto", oferta.getMontoPropuesto());
            cmd.setString("p_estado", oferta.getEstado().name());
            cmd.setInt("p_id_comprador", oferta.getComprador().getIdUsuario());
            cmd.setInt("p_id_anuncio", oferta.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", oferta.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(oferta.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idOferta) throws SQLException {
        String sql = "{call eliminar_oferta(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idOferta);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Oferta findById(int idOferta) throws SQLException {
        String sql = "{call buscar_oferta_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idOferta);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Oferta()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Oferta> findAll() throws SQLException {
        String sql = "{call listar_ofertas()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Oferta> ofertas = new ArrayList<>();
            while (rs.next()) {
                ofertas.add(mapear(rs, new Oferta()));
            }
            return ofertas;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cambiarEstado(int idOferta, EstadoOferta estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        String sql = "{call cambiar_estado_oferta(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idOferta);
            cmd.setString("p_estado", estado.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int rechazarPendientes(int idAnuncio, int idOfertaAceptada) throws SQLException {
        String sql = "{call rechazar_ofertas_pendientes(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            cmd.setInt("p_id_oferta_aceptada", idOfertaAceptada);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = "{call listar_ofertas_por_anuncio(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Oferta> ofertas = new ArrayList<>();
                while (rs.next()) {
                    ofertas.add(mapear(rs, new Oferta()));
                }
                return ofertas;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Oferta mapear(ResultSet rs, Oferta oferta) throws SQLException {
        super.mapear(rs, oferta);
        oferta.setIdOferta(rs.getInt("id_oferta"));
        oferta.setMontoPropuesto(rs.getDouble("monto_propuesto"));
        oferta.setEstado(EstadoOferta.valueOf(rs.getString("estado")));
        oferta.setFecha(leerFechaHora(rs, "fecha"));
        oferta.setComprador(mapearUsuarioReferencia(rs, "comprador"));
        oferta.setAnuncio(mapearAnuncioReferencia(rs));
        return oferta;
    }
}

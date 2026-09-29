package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.OfertaDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;

public class OfertaDAOImpl extends RegistroDAOImpl<Oferta> implements OfertaDAO {

    private static final String SELECT_BASE = """
            SELECT o.id_oferta, o.monto_propuesto, o.estado, o.fecha,
                   u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
                   u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
                   o.activo, o.fecha_creacion, o.fecha_modificacion, o.usuario_creacion, o.usuario_modificacion
            FROM oferta o
            JOIN usuario u ON u.id_usuario = o.id_comprador
            JOIN anuncio a ON a.id_anuncio = o.id_anuncio
            """;

    // La fecha de la oferta no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Oferta oferta) throws SQLException {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta no puede ser nula");
        }
        String sql = """
                INSERT INTO oferta (monto_propuesto, estado, id_comprador, id_anuncio, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setDouble(1, oferta.getMontoPropuesto());
            cmd.setString(2, oferta.getEstado().name());
            cmd.setInt(3, oferta.getComprador().getIdUsuario());
            cmd.setInt(4, oferta.getAnuncio().getIdAnuncio());
            cmd.setBoolean(5, oferta.isActivo());
            cmd.setString(6, usuarioAuditoria(oferta.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la oferta");
            }
            oferta.setIdOferta(leerIdGenerado(cmd));
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
        String sql = """
                UPDATE oferta
                SET monto_propuesto = ?, estado = ?, id_comprador = ?, id_anuncio = ?, activo = ?,
                    usuario_modificacion = ?
                WHERE id_oferta = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setDouble(1, oferta.getMontoPropuesto());
            cmd.setString(2, oferta.getEstado().name());
            cmd.setInt(3, oferta.getComprador().getIdUsuario());
            cmd.setInt(4, oferta.getAnuncio().getIdAnuncio());
            cmd.setBoolean(5, oferta.isActivo());
            cmd.setString(6, usuarioAuditoria(oferta.getUsuarioModificacion()));
            cmd.setInt(7, oferta.getIdOferta());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idOferta) throws SQLException {
        String sql = """
                UPDATE oferta SET activo = 0 WHERE id_oferta = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idOferta);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Oferta findById(int idOferta) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.id_oferta = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idOferta);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Oferta()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Oferta> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE o.activo = 1 ORDER BY o.fecha DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = """
                UPDATE oferta SET estado = ? WHERE id_oferta = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estado.name());
            cmd.setInt(2, idOferta);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int rechazarPendientes(int idAnuncio, int idOfertaAceptada) throws SQLException {
        String sql = """
                UPDATE oferta SET estado = 'RECHAZADA'
                WHERE id_anuncio = ? AND id_oferta <> ? AND estado = 'PENDIENTE' AND activo = 1
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            cmd.setInt(2, idOfertaAceptada);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Oferta> listarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.id_anuncio = ? AND o.activo = 1 ORDER BY o.fecha DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
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

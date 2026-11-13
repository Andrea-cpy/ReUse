package pe.edu.pucp.reuse.bl.insignias;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.dao.UsuarioDAO;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;

/**
 * Candidato que evaluan las especificaciones de insignias: las metricas reales de un
 * usuario (calculadas por el procedimiento obtener_metrica_usuario). Cada metrica se
 * consulta una sola vez aunque varias reglas o insignias la usen.
 */
public class MetricasUsuario {

    private final int idUsuario;
    private final UsuarioDAO usuarioDAO;
    private final Map<TipoMetricaInsignia, Double> valores = new ConcurrentHashMap<>();

    public MetricasUsuario(int idUsuario, UsuarioDAO usuarioDAO) {
        this.idUsuario = idUsuario;
        this.usuarioDAO = usuarioDAO;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public double valor(TipoMetricaInsignia metrica) throws BLException {
        Double valor = valores.get(metrica);
        if (valor == null) {
            try {
                valor = usuarioDAO.obtenerMetrica(idUsuario, metrica);
            } catch (SQLException e) {
                throw new BLException("No se pudo calcular la metrica " + metrica + " del usuario " + idUsuario, e);
            }
            valores.put(metrica, valor);
        }
        return valor;
    }

    /**
     * Calcula de una vez varias metricas en paralelo: cada procedimiento abre su propia
     * conexion a la BD, asi que consultarlas a la vez reduce el tiempo de respuesta.
     */
    public MetricasUsuario precargar(Collection<TipoMetricaInsignia> metricas) throws BLException {
        try {
            metricas.parallelStream().distinct().forEach(metrica -> {
                try {
                    valor(metrica);
                } catch (BLException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof BLException causa) {
                throw causa;
            }
            throw e;
        }
        return this;
    }

    public Map<TipoMetricaInsignia, Double> comoMapa() {
        return Map.copyOf(valores);
    }
}

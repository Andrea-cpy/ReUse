package pe.edu.pucp.reuse.bl.insignias;

import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

/**
 * Construye las especificaciones a partir de las reglas guardadas en la BD
 * (tabla regla_insignia). Agregar una insignia nueva no requiere tocar codigo:
 * basta con registrar sus reglas.
 */
public final class EspecificacionesInsignia {

    private EspecificacionesInsignia() {
    }

    /** Una regla: METRICA operador valorObjetivo (por ejemplo VENTAS_COMPLETADAS >= 5). */
    public static Especificacion<MetricasUsuario> deRegla(ReglaInsignia regla) {
        return metricas -> regla.seCumpleCon(metricas.valor(regla.getTipoMetrica()));
    }

    /** Una insignia se otorga solo si se cumplen todas sus reglas (conjuncion). */
    public static Especificacion<MetricasUsuario> deInsignia(Insignia insignia) {
        Especificacion<MetricasUsuario> especificacion = Especificacion.siempre();
        for (ReglaInsignia regla : insignia.getReglas()) {
            especificacion = especificacion.y(deRegla(regla));
        }
        return especificacion;
    }
}

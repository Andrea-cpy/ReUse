package pe.edu.pucp.reuse.modelo.gamificacion;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.OperadorComparacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;

public class ReglaInsignia extends Registro {

    private int idRegla;
    private TipoMetricaInsignia tipoMetrica;
    private OperadorComparacion operador;
    private double valorObjetivo;

    private Insignia insignia;

    public ReglaInsignia() {
    }

    public ReglaInsignia(TipoMetricaInsignia tipoMetrica, OperadorComparacion operador, double valorObjetivo,
                         Insignia insignia) {
        this();
        this.tipoMetrica = tipoMetrica;
        this.operador = operador;
        this.valorObjetivo = valorObjetivo;
        setInsignia(insignia);
    }

    public boolean seCumpleCon(double valorReal) {
        return operador.comparar(valorReal, valorObjetivo);
    }

    public int getIdRegla() {
        return idRegla;
    }

    public void setIdRegla(int idRegla) {
        this.idRegla = idRegla;
    }

    public TipoMetricaInsignia getTipoMetrica() {
        return tipoMetrica;
    }

    public void setTipoMetrica(TipoMetricaInsignia tipoMetrica) {
        this.tipoMetrica = tipoMetrica;
    }

    public OperadorComparacion getOperador() {
        return operador;
    }

    public void setOperador(OperadorComparacion operador) {
        this.operador = operador;
    }

    public double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public Insignia getInsignia() {
        return insignia;
    }

    public final void setInsignia(Insignia insignia) {
        if (this.insignia == insignia) {
            return;
        }
        Insignia anterior = this.insignia;
        this.insignia = insignia;
        if (anterior != null) {
            anterior.quitarRegla(this);
        }
        if (insignia != null) {
            insignia.agregarRegla(this);
        }
    }
}

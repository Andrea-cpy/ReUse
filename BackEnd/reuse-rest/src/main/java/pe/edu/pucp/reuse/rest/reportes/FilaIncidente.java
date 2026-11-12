package pe.edu.pucp.reuse.rest.reportes;

/** Fila del reporte PDF de incidentes (ReporteIncidentes.jrxml). Los getters son los $F{...} del reporte. */
public class FilaIncidente {
    private final Integer id;
    private final String tipo;
    private final String reportado;
    private final String motivo;
    private final String denunciante;
    private final String fecha;
    private final String estado;
    private final String descripcion;
    private final String revisor;

    public FilaIncidente(Integer id, String tipo, String reportado, String motivo, String denunciante,
                         String fecha, String estado, String descripcion, String revisor) {
        this.id = id;
        this.tipo = tipo;
        this.reportado = reportado;
        this.motivo = motivo;
        this.denunciante = denunciante;
        this.fecha = fecha;
        this.estado = estado;
        this.descripcion = descripcion;
        this.revisor = revisor;
    }

    public Integer getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getReportado() {
        return reportado;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDenunciante() {
        return denunciante;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getRevisor() {
        return revisor;
    }
}

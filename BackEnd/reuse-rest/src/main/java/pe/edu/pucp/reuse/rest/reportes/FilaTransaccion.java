package pe.edu.pucp.reuse.rest.reportes;

/** Fila del reporte PDF de transacciones (ReporteTransacciones.jrxml). */
public class FilaTransaccion {
    private final Integer id;
    private final String anuncio;
    private final String vendedor;
    private final String comprador;
    private final String estado;
    private final String fechaInicio;
    private final String fechaFin;
    private final Double monto;

    public FilaTransaccion(Integer id, String anuncio, String vendedor, String comprador, String estado,
                           String fechaInicio, String fechaFin, Double monto) {
        this.id = id;
        this.anuncio = anuncio;
        this.vendedor = vendedor;
        this.comprador = comprador;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.monto = monto;
    }

    public Integer getId() {
        return id;
    }

    public String getAnuncio() {
        return anuncio;
    }

    public String getVendedor() {
        return vendedor;
    }

    public String getComprador() {
        return comprador;
    }

    public String getEstado() {
        return estado;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public Double getMonto() {
        return monto;
    }
}

package pe.edu.pucp.reuse.rest.dto;

/** RF-18: reputacion, nivel y metricas de un usuario. */
public class ReputacionDTO {
    private int idUsuario;
    private String nivel;
    private double reputacionVendedor;
    private double reputacionComprador;
    private int ventasCompletadas;
    private int comprasCompletadas;
    private int transaccionesCompletadas;
    private double calificacionPromedio;
    private int sancionesRecientes;

    public ReputacionDTO() {
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public double getReputacionVendedor() {
        return reputacionVendedor;
    }

    public void setReputacionVendedor(double reputacionVendedor) {
        this.reputacionVendedor = reputacionVendedor;
    }

    public double getReputacionComprador() {
        return reputacionComprador;
    }

    public void setReputacionComprador(double reputacionComprador) {
        this.reputacionComprador = reputacionComprador;
    }

    public int getVentasCompletadas() {
        return ventasCompletadas;
    }

    public void setVentasCompletadas(int ventasCompletadas) {
        this.ventasCompletadas = ventasCompletadas;
    }

    public int getComprasCompletadas() {
        return comprasCompletadas;
    }

    public void setComprasCompletadas(int comprasCompletadas) {
        this.comprasCompletadas = comprasCompletadas;
    }

    public int getTransaccionesCompletadas() {
        return transaccionesCompletadas;
    }

    public void setTransaccionesCompletadas(int transaccionesCompletadas) {
        this.transaccionesCompletadas = transaccionesCompletadas;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    public int getSancionesRecientes() {
        return sancionesRecientes;
    }

    public void setSancionesRecientes(int sancionesRecientes) {
        this.sancionesRecientes = sancionesRecientes;
    }
}

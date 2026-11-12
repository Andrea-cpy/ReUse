package pe.edu.pucp.reuse.rest.reportes;

/** Fila del reporte PDF de reputacion y conducta de usuarios (ReporteUsuarios.jrxml). */
public class FilaUsuario {
    private final String codigo;
    private final String nombre;
    private final String carrera;
    private final String estado;
    private final Double reputacionVendedor;
    private final Double reputacionComprador;
    private final Integer reportesRecibidos;
    private final Integer sancionesRecientes;

    public FilaUsuario(String codigo, String nombre, String carrera, String estado, Double reputacionVendedor,
                       Double reputacionComprador, Integer reportesRecibidos, Integer sancionesRecientes) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.carrera = carrera;
        this.estado = estado;
        this.reputacionVendedor = reputacionVendedor;
        this.reputacionComprador = reputacionComprador;
        this.reportesRecibidos = reportesRecibidos;
        this.sancionesRecientes = sancionesRecientes;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getEstado() {
        return estado;
    }

    public Double getReputacionVendedor() {
        return reputacionVendedor;
    }

    public Double getReputacionComprador() {
        return reputacionComprador;
    }

    public Integer getReportesRecibidos() {
        return reportesRecibidos;
    }

    public Integer getSancionesRecientes() {
        return sancionesRecientes;
    }
}

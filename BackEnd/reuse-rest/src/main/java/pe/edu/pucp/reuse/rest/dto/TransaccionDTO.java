package pe.edu.pucp.reuse.rest.dto;

/** Transaccion con su oferta y su cita (si las tiene). */
public class TransaccionDTO {
    private int idTransaccion;
    private String fechaInicio;
    private String fechaFin;
    private String estado;
    private boolean confirmacionComprador;
    private boolean confirmacionVendedor;
    private int idAnuncio;
    private String tituloAnuncio;
    private double precioAnuncio;
    private int idVendedor;
    private String nombreVendedor;
    private int idComprador;
    private String nombreComprador;
    private double montoAcordado;
    private int idCita;
    private String fechaCita;
    private String estadoCita;

    public TransaccionDTO() {
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isConfirmacionComprador() {
        return confirmacionComprador;
    }

    public void setConfirmacionComprador(boolean confirmacionComprador) {
        this.confirmacionComprador = confirmacionComprador;
    }

    public boolean isConfirmacionVendedor() {
        return confirmacionVendedor;
    }

    public void setConfirmacionVendedor(boolean confirmacionVendedor) {
        this.confirmacionVendedor = confirmacionVendedor;
    }

    public int getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(int idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getTituloAnuncio() {
        return tituloAnuncio;
    }

    public void setTituloAnuncio(String tituloAnuncio) {
        this.tituloAnuncio = tituloAnuncio;
    }

    public double getPrecioAnuncio() {
        return precioAnuncio;
    }

    public void setPrecioAnuncio(double precioAnuncio) {
        this.precioAnuncio = precioAnuncio;
    }

    public int getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(int idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public int getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(int idComprador) {
        this.idComprador = idComprador;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }

    public double getMontoAcordado() {
        return montoAcordado;
    }

    public void setMontoAcordado(double montoAcordado) {
        this.montoAcordado = montoAcordado;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public String getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(String fechaCita) {
        this.fechaCita = fechaCita;
    }

    public String getEstadoCita() {
        return estadoCita;
    }

    public void setEstadoCita(String estadoCita) {
        this.estadoCita = estadoCita;
    }
}

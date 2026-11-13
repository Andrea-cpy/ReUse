package pe.edu.pucp.reuse.rest.dto;

public class OfertaDTO {
    private int idOferta;
    private double montoPropuesto;
    private String estado;
    private String fecha;
    private int idComprador;
    private String nombreComprador;
    private int idAnuncio;
    private String tituloAnuncio;
    private double precioAnuncio;
    private int idVendedor;

    public OfertaDTO() {
    }

    public int getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(int idOferta) {
        this.idOferta = idOferta;
    }

    public double getMontoPropuesto() {
        return montoPropuesto;
    }

    public void setMontoPropuesto(double montoPropuesto) {
        this.montoPropuesto = montoPropuesto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
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
}

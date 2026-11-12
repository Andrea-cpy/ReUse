package pe.edu.pucp.reuse.rest.dto;

/** RF-04, RF-13, RF-14: denuncia de un usuario o de un anuncio (tipo USUARIO | ANUNCIO). Las fechas viajan en ISO. */
public class DenunciaDTO {
    private int idReporte;
    private String tipo;
    private String motivo;
    private String descripcion;
    private String fechaRegistro;
    private String estadoRevision;
    private String fechaRevision;
    private int idDenunciante;
    private String nombreDenunciante;
    private int idRevisor;
    private String nombreRevisor;
    private int idDenunciado;
    private String nombreDenunciado;
    private String codigoDenunciado;
    private int idTransaccion;
    private int idAnuncio;
    private String tituloAnuncio;
    private String estadoAnuncio;
    private int idVendedorAnuncio;
    private boolean cuentaSuspendida;

    public DenunciaDTO() {
    }

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstadoRevision() {
        return estadoRevision;
    }

    public void setEstadoRevision(String estadoRevision) {
        this.estadoRevision = estadoRevision;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public int getIdDenunciante() {
        return idDenunciante;
    }

    public void setIdDenunciante(int idDenunciante) {
        this.idDenunciante = idDenunciante;
    }

    public String getNombreDenunciante() {
        return nombreDenunciante;
    }

    public void setNombreDenunciante(String nombreDenunciante) {
        this.nombreDenunciante = nombreDenunciante;
    }

    public int getIdRevisor() {
        return idRevisor;
    }

    public void setIdRevisor(int idRevisor) {
        this.idRevisor = idRevisor;
    }

    public String getNombreRevisor() {
        return nombreRevisor;
    }

    public void setNombreRevisor(String nombreRevisor) {
        this.nombreRevisor = nombreRevisor;
    }

    public int getIdDenunciado() {
        return idDenunciado;
    }

    public void setIdDenunciado(int idDenunciado) {
        this.idDenunciado = idDenunciado;
    }

    public String getNombreDenunciado() {
        return nombreDenunciado;
    }

    public void setNombreDenunciado(String nombreDenunciado) {
        this.nombreDenunciado = nombreDenunciado;
    }

    public String getCodigoDenunciado() {
        return codigoDenunciado;
    }

    public void setCodigoDenunciado(String codigoDenunciado) {
        this.codigoDenunciado = codigoDenunciado;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
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

    public String getEstadoAnuncio() {
        return estadoAnuncio;
    }

    public void setEstadoAnuncio(String estadoAnuncio) {
        this.estadoAnuncio = estadoAnuncio;
    }

    public int getIdVendedorAnuncio() {
        return idVendedorAnuncio;
    }

    public void setIdVendedorAnuncio(int idVendedorAnuncio) {
        this.idVendedorAnuncio = idVendedorAnuncio;
    }

    public boolean isCuentaSuspendida() {
        return cuentaSuspendida;
    }

    public void setCuentaSuspendida(boolean cuentaSuspendida) {
        this.cuentaSuspendida = cuentaSuspendida;
    }
}

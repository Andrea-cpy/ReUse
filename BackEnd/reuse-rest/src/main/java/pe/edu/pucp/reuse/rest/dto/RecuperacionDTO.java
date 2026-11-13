package pe.edu.pucp.reuse.rest.dto;

/** RF-01: recuperacion de contrasena (solicitar codigo y restablecer). */
public class RecuperacionDTO {
    private String correo;
    private String codigo;
    private String contrasenaNueva;

    public RecuperacionDTO() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }

    public void setContrasenaNueva(String contrasenaNueva) {
        this.contrasenaNueva = contrasenaNueva;
    }
}

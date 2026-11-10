package pe.edu.pucp.reuse.rest.dto;

/** Cambio de contrasena del propio usuario. */
public class CambioContrasenaDTO {
    private String contrasenaActual;
    private String contrasenaNueva;

    public CambioContrasenaDTO() {
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }

    public void setContrasenaNueva(String contrasenaNueva) {
        this.contrasenaNueva = contrasenaNueva;
    }
}

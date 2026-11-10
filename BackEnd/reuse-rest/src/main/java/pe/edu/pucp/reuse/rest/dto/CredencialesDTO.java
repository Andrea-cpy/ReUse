package pe.edu.pucp.reuse.rest.dto;

/** Correo institucional y contrasena para iniciar sesion. */
public class CredencialesDTO {
    private String correo;
    private String contrasena;

    public CredencialesDTO() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}

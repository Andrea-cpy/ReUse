package pe.edu.pucp.reuse.modelo.usuarios;

/**
 * Identidad institucional confirmada por Google en el ingreso o registro con Google.
 * La crea la capa de negocio despues de validar el token y aplicar las reglas de ReUse
 * (cuenta @pucp.edu.pe del dominio PUCP con correo verificado).
 */
public class CuentaGoogle {
    private final String correo;
    private final String codigoPUCP;
    private final String nombres;
    private final String apellidos;

    public CuentaGoogle(String correo, String codigoPUCP, String nombres, String apellidos) {
        this.correo = correo;
        this.codigoPUCP = codigoPUCP;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    /** Correo institucional en minusculas. */
    public String getCorreo() {
        return correo;
    }

    /** Codigo PUCP deducido del correo de alumno, o null si el correo no tiene ese formato. */
    public String getCodigoPUCP() {
        return codigoPUCP;
    }

    /** Nombres segun Google (given_name); puede ser null. */
    public String getNombres() {
        return nombres;
    }

    /** Apellidos segun Google (family_name); puede ser null. */
    public String getApellidos() {
        return apellidos;
    }
}

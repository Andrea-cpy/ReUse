package pe.edu.pucp.reuse.integraciones.google;

/**
 * Datos de un ID token de Google cuya firma, Client ID, emisor y vencimiento ya fueron
 * validados. Son los valores tal como los envia Google: no se aplica ninguna regla de ReUse
 * (dominio PUCP, correo verificado, codigo PUCP); eso le corresponde a la capa de negocio.
 */
public final class DatosTokenGoogle {
    private final String correo;
    private final Boolean emailVerificado;
    private final String hd;
    private final String nombres;
    private final String apellidos;

    DatosTokenGoogle(String correo, Boolean emailVerificado, String hd, String nombres, String apellidos) {
        this.correo = correo;
        this.emailVerificado = emailVerificado;
        this.hd = hd;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    /** Claim "email", sin normalizar. */
    public String getCorreo() {
        return correo;
    }

    /** Claim "email_verified"; puede ser null si Google no lo envio. */
    public Boolean getEmailVerificado() {
        return emailVerificado;
    }

    /** Claim "hd" (dominio de Google Workspace); null en cuentas Google personales. */
    public String getHd() {
        return hd;
    }

    /** Claim "given_name"; puede ser null. */
    public String getNombres() {
        return nombres;
    }

    /** Claim "family_name"; puede ser null. */
    public String getApellidos() {
        return apellidos;
    }
}

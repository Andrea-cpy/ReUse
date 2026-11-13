package pe.edu.pucp.reuse.rest.dto;

/** Datos de un estudiante. 'contrasena' solo se recibe (registro); nunca se devuelve. */
public class UsuarioDTO {
    private int idUsuario;
    private String codigoPUCP;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String contrasena;
    private int idCarrera;
    private String nombreCarrera;
    private int idFacultad;
    private String nombreFacultad;
    private boolean verificado;
    private String estadoCuenta;
    private double reputacionVendedor;
    private double reputacionComprador;
    private int contadorReportes;
    private String fotoPerfil;
    private String fechaRegistro;
    private boolean administrador;

    public UsuarioDTO() {
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCodigoPUCP() {
        return codigoPUCP;
    }

    public void setCodigoPUCP(String codigoPUCP) {
        this.codigoPUCP = codigoPUCP;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombreCarrera() {
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera) {
        this.nombreCarrera = nombreCarrera;
    }

    public int getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(int idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getNombreFacultad() {
        return nombreFacultad;
    }

    public void setNombreFacultad(String nombreFacultad) {
        this.nombreFacultad = nombreFacultad;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
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

    public int getContadorReportes() {
        return contadorReportes;
    }

    public void setContadorReportes(int contadorReportes) {
        this.contadorReportes = contadorReportes;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isAdministrador() {
        return administrador;
    }

    public void setAdministrador(boolean administrador) {
        this.administrador = administrador;
    }
}

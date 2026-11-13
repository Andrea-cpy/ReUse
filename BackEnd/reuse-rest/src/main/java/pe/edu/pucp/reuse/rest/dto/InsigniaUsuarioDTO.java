package pe.edu.pucp.reuse.rest.dto;

/** RF-18: insignia obtenida por un usuario. */
public class InsigniaUsuarioDTO {
    private int idInsigniaUsuario;
    private int idInsignia;
    private String nombre;
    private String descripcion;
    private String icono;
    private String tipo;
    private String fechaObtencion;

    public InsigniaUsuarioDTO() {
    }

    public int getIdInsigniaUsuario() {
        return idInsigniaUsuario;
    }

    public void setIdInsigniaUsuario(int idInsigniaUsuario) {
        this.idInsigniaUsuario = idInsigniaUsuario;
    }

    public int getIdInsignia() {
        return idInsignia;
    }

    public void setIdInsignia(int idInsignia) {
        this.idInsignia = idInsignia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaObtencion() {
        return fechaObtencion;
    }

    public void setFechaObtencion(String fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }
}

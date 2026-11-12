package pe.edu.pucp.reuse.rest.dto;

import java.util.ArrayList;
import java.util.List;

/** RF-18: insignia con sus reglas de obtencion. */
public class InsigniaDTO {
    private int idInsignia;
    private String nombre;
    private String descripcion;
    private String icono;
    private String tipo;
    private boolean activo;
    private List<ReglaInsigniaDTO> reglas = new ArrayList<>();

    public InsigniaDTO() {
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ReglaInsigniaDTO> getReglas() {
        return reglas;
    }

    public void setReglas(List<ReglaInsigniaDTO> reglas) {
        this.reglas = reglas;
    }
}

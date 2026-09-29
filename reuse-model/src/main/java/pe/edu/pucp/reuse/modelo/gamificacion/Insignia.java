package pe.edu.pucp.reuse.modelo.gamificacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;

/**
 * El antiguo atributo "activa" ahora es la columna de borrado logico
 * "activo" heredada de Registro. La evaluacion de reglas para otorgar la
 * insignia la hace InsigniaUsuarioBL con las metricas calculadas en la base.
 */
public class Insignia extends Registro {

    private int idInsignia;
    private String nombre;
    private String descripcion;
    private String icono;
    private TipoInsignia tipo;

    private final List<ReglaInsignia> reglas;
    private final List<InsigniaUsuario> otorgamientos;

    public Insignia() {
        this.reglas = new ArrayList<>();
        this.otorgamientos = new ArrayList<>();
    }

    public Insignia(String nombre, String descripcion, String icono, TipoInsignia tipo) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.tipo = tipo;
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

    public TipoInsignia getTipo() {
        return tipo;
    }

    public void setTipo(TipoInsignia tipo) {
        this.tipo = tipo;
    }

    public List<ReglaInsignia> getReglas() {
        return Collections.unmodifiableList(reglas);
    }

    public List<InsigniaUsuario> getOtorgamientos() {
        return Collections.unmodifiableList(otorgamientos);
    }

    public void agregarRegla(ReglaInsignia regla) {
        if (regla == null || reglas.contains(regla)) {
            return;
        }
        reglas.add(regla);
        regla.setInsignia(this);
    }

    public void quitarRegla(ReglaInsignia regla) {
        if (reglas.remove(regla) && regla.getInsignia() == this) {
            regla.setInsignia(null);
        }
    }

    public void agregarOtorgamiento(InsigniaUsuario insigniaUsuario) {
        if (insigniaUsuario == null || otorgamientos.contains(insigniaUsuario)) {
            return;
        }
        otorgamientos.add(insigniaUsuario);
        insigniaUsuario.setInsignia(this);
    }

    public void quitarOtorgamiento(InsigniaUsuario insigniaUsuario) {
        if (otorgamientos.remove(insigniaUsuario) && insigniaUsuario.getInsignia() == this) {
            insigniaUsuario.setInsignia(null);
        }
    }
}

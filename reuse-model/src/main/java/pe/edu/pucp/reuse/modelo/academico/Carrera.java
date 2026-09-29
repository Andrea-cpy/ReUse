package pe.edu.pucp.reuse.modelo.academico;

import pe.edu.pucp.reuse.modelo.Registro;

/**
 * Catalogo de carreras (antes era un enum). Cada carrera pertenece a una
 * facultad; usuarios y materiales apuntan solo a la carrera y obtienen la
 * facultad navegando por ella.
 */
public class Carrera extends Registro {

    private int idCarrera;
    private String nombre;

    private Facultad facultad;

    public Carrera() {
    }

    public Carrera(String nombre, Facultad facultad) {
        this();
        this.nombre = nombre;
        setFacultad(facultad);
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public final void setFacultad(Facultad facultad) {
        if (this.facultad == facultad) {
            return;
        }
        Facultad anterior = this.facultad;
        this.facultad = facultad;
        if (anterior != null) {
            anterior.quitarCarrera(this);
        }
        if (facultad != null) {
            facultad.agregarCarrera(this);
        }
    }
}

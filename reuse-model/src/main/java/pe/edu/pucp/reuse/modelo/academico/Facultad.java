package pe.edu.pucp.reuse.modelo.academico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;

/**
 * Catalogo de facultades (antes era un enum). Agregar una facultad ya no
 * requiere recompilar: basta con insertar una fila en la tabla facultad.
 */
public class Facultad extends Registro {

    private int idFacultad;
    private String nombre;

    private final List<Carrera> carreras;

    public Facultad() {
        this.carreras = new ArrayList<>();
    }

    public Facultad(String nombre) {
        this();
        this.nombre = nombre;
    }

    public int getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(int idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Carrera> getCarreras() {
        return Collections.unmodifiableList(carreras);
    }

    public void agregarCarrera(Carrera carrera) {
        if (carrera == null || carreras.contains(carrera)) {
            return;
        }
        carreras.add(carrera);
        carrera.setFacultad(this);
    }

    public void quitarCarrera(Carrera carrera) {
        if (carreras.remove(carrera) && carrera.getFacultad() == this) {
            carrera.setFacultad(null);
        }
    }
}

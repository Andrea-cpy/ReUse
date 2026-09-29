package pe.edu.pucp.reuse.modelo.catalogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;

public class CategoriaMaterial extends Registro {

    private int idCategoria;
    private String nombre;
    private String descripcion;

    private final List<MaterialAcademico> materiales;

    public CategoriaMaterial() {
        this.materiales = new ArrayList<>();
    }

    public CategoriaMaterial(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
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

    public List<MaterialAcademico> getMateriales() {
        return Collections.unmodifiableList(materiales);
    }

    public void agregarMaterial(MaterialAcademico material) {
        if (material == null || materiales.contains(material)) {
            return;
        }
        materiales.add(material);
        material.setCategoria(this);
    }

    public void quitarMaterial(MaterialAcademico material) {
        if (materiales.remove(material) && material.getCategoria() == this) {
            material.setCategoria(null);
        }
    }
}

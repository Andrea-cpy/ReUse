package pe.edu.pucp.reuse.modelo.catalogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

/**
 * Un material puede estar relacionado con una o mas carreras (tabla
 * intermedia material_carrera). Las facultades se obtienen navegando
 * por las carreras.
 */
public class MaterialAcademico extends Registro {

    private int idMaterial;
    private String titulo;

    private CategoriaMaterial categoria;
    private List<Carrera> carreras;
    private final List<Anuncio> anuncios;

    public MaterialAcademico() {
        this.carreras = new ArrayList<>();
        this.anuncios = new ArrayList<>();
    }

    public MaterialAcademico(String titulo, CategoriaMaterial categoria, List<Carrera> carreras) {
        this();
        this.titulo = titulo;
        setCategoria(categoria);
        setCarreras(carreras);
    }

    public List<Facultad> getFacultades() {
        List<Facultad> facultades = new ArrayList<>();
        for (Carrera carrera : carreras) {
            Facultad facultad = carrera.getFacultad();
            if (facultad != null && !contieneFacultad(facultades, facultad.getIdFacultad())) {
                facultades.add(facultad);
            }
        }
        return Collections.unmodifiableList(facultades);
    }

    private static boolean contieneFacultad(List<Facultad> facultades, int idFacultad) {
        for (Facultad facultad : facultades) {
            if (facultad.getIdFacultad() == idFacultad) {
                return true;
            }
        }
        return false;
    }

    public int getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public CategoriaMaterial getCategoria() {
        return categoria;
    }

    public final void setCategoria(CategoriaMaterial categoria) {
        if (this.categoria == categoria) {
            return;
        }
        CategoriaMaterial anterior = this.categoria;
        this.categoria = categoria;
        if (anterior != null) {
            anterior.quitarMaterial(this);
        }
        if (categoria != null) {
            categoria.agregarMaterial(this);
        }
    }

    public List<Carrera> getCarreras() {
        return Collections.unmodifiableList(carreras);
    }

    public final void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras == null ? new ArrayList<>() : new ArrayList<>(carreras);
    }

    public void agregarCarrera(Carrera carrera) {
        if (carrera != null && !carreras.contains(carrera)) {
            carreras.add(carrera);
        }
    }

    public void quitarCarrera(Carrera carrera) {
        carreras.remove(carrera);
    }

    public List<Anuncio> getAnuncios() {
        return Collections.unmodifiableList(anuncios);
    }

    public void agregarAnuncio(Anuncio anuncio) {
        if (anuncio == null || anuncios.contains(anuncio)) {
            return;
        }
        anuncios.add(anuncio);
        anuncio.setMaterialAcademico(this);
    }

    public void quitarAnuncio(Anuncio anuncio) {
        if (anuncios.remove(anuncio) && anuncio.getMaterialAcademico() == this) {
            anuncio.setMaterialAcademico(null);
        }
    }
}

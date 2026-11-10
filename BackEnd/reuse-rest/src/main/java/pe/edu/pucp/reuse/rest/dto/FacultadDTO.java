package pe.edu.pucp.reuse.rest.dto;

public class FacultadDTO {
    private int idFacultad;
    private String nombre;

    public FacultadDTO() {
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
}

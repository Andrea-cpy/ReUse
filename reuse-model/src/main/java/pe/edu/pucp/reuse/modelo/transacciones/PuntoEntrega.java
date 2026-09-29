package pe.edu.pucp.reuse.modelo.transacciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;

public class PuntoEntrega extends Registro {

    private int idPuntoEntrega;
    private String nombre;
    private String referencia;
    private String ubicacion;

    private final List<CitaEntrega> citas;

    public PuntoEntrega() {
        this.citas = new ArrayList<>();
    }

    public PuntoEntrega(String nombre, String referencia, String ubicacion) {
        this();
        this.nombre = nombre;
        this.referencia = referencia;
        this.ubicacion = ubicacion;
    }

    public int getIdPuntoEntrega() {
        return idPuntoEntrega;
    }

    public void setIdPuntoEntrega(int idPuntoEntrega) {
        this.idPuntoEntrega = idPuntoEntrega;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public List<CitaEntrega> getCitas() {
        return Collections.unmodifiableList(citas);
    }

    public void agregarCita(CitaEntrega cita) {
        if (cita == null || citas.contains(cita)) {
            return;
        }
        citas.add(cita);
        cita.setPuntoEntrega(this);
    }

    public void quitarCita(CitaEntrega cita) {
        if (citas.remove(cita) && cita.getPuntoEntrega() == this) {
            cita.setPuntoEntrega(null);
        }
    }
}

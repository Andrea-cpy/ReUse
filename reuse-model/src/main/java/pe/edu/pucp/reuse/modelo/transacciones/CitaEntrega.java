package pe.edu.pucp.reuse.modelo.transacciones;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;

public class CitaEntrega extends Registro {

    private int idCita;
    // Fecha y hora acordadas por comprador y vendedor (dato del negocio, no de auditoria).
    private LocalDateTime fechaHora;
    private EstadoCita estado;

    private PuntoEntrega puntoEntrega;
    private Transaccion transaccion;

    public CitaEntrega() {
        this.estado = EstadoCita.PROPUESTA;
    }

    public CitaEntrega(LocalDateTime fechaHora, Transaccion transaccion, PuntoEntrega puntoEntrega) {
        this();
        this.fechaHora = fechaHora;
        setPuntoEntrega(puntoEntrega);
        setTransaccion(transaccion);
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public PuntoEntrega getPuntoEntrega() {
        return puntoEntrega;
    }

    public final void setPuntoEntrega(PuntoEntrega puntoEntrega) {
        if (this.puntoEntrega == puntoEntrega) {
            return;
        }
        PuntoEntrega anterior = this.puntoEntrega;
        this.puntoEntrega = puntoEntrega;
        if (anterior != null) {
            anterior.quitarCita(this);
        }
        if (puntoEntrega != null) {
            puntoEntrega.agregarCita(this);
        }
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public final void setTransaccion(Transaccion transaccion) {
        if (this.transaccion == transaccion) {
            return;
        }
        Transaccion anterior = this.transaccion;
        this.transaccion = transaccion;
        if (anterior != null && anterior.getCitaEntrega() == this) {
            anterior.setCitaEntrega(null);
        }
        if (transaccion != null && transaccion.getCitaEntrega() != this) {
            transaccion.setCitaEntrega(this);
        }
    }
}

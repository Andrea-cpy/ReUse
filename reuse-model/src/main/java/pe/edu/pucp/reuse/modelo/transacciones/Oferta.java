package pe.edu.pucp.reuse.modelo.transacciones;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Oferta extends Registro {

    private int idOferta;
    private double montoPropuesto;
    private EstadoOferta estado;
    private LocalDateTime fecha;

    private UsuarioPUCP comprador;
    private Anuncio anuncio;
    private Transaccion transaccion;

    public Oferta() {
        this.estado = EstadoOferta.PENDIENTE;
    }

    public Oferta(double montoPropuesto, UsuarioPUCP comprador, Anuncio anuncio) {
        this();
        this.montoPropuesto = montoPropuesto;
        setAnuncio(anuncio);
        setComprador(comprador);
    }

    public int getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(int idOferta) {
        this.idOferta = idOferta;
    }

    public double getMontoPropuesto() {
        return montoPropuesto;
    }

    public void setMontoPropuesto(double montoPropuesto) {
        this.montoPropuesto = montoPropuesto;
    }

    public EstadoOferta getEstado() {
        return estado;
    }

    public void setEstado(EstadoOferta estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public UsuarioPUCP getComprador() {
        return comprador;
    }

    public final void setComprador(UsuarioPUCP comprador) {
        if (this.comprador == comprador) {
            return;
        }
        UsuarioPUCP anterior = this.comprador;
        this.comprador = comprador;
        if (anterior != null) {
            anterior.quitarOfertaRealizada(this);
        }
        if (comprador != null) {
            comprador.agregarOfertaRealizada(this);
        }
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public final void setAnuncio(Anuncio anuncio) {
        if (this.anuncio == anuncio) {
            return;
        }
        Anuncio anterior = this.anuncio;
        this.anuncio = anuncio;
        if (anterior != null) {
            anterior.quitarOferta(this);
        }
        if (anuncio != null) {
            anuncio.agregarOferta(this);
        }
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Transaccion transaccion) {
        if (this.transaccion == transaccion) {
            return;
        }
        Transaccion anterior = this.transaccion;
        this.transaccion = transaccion;
        if (anterior != null && anterior.getOferta() == this) {
            anterior.setOferta(null);
        }
        if (transaccion != null && transaccion.getOferta() != this) {
            transaccion.setOferta(this);
        }
    }
}

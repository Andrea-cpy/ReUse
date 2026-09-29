package pe.edu.pucp.reuse.modelo.transacciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Las transiciones de estado (confirmar cita, confirmar entrega, cancelar)
 * se hacen en la capa de negocio (TransaccionBL) dentro de una transaccion
 * de base de datos, porque modifican varias tablas a la vez.
 */
public class Transaccion extends Registro {

    private int idTransaccion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoTransaccion estado;
    private boolean confirmacionComprador;
    private boolean confirmacionVendedor;

    private Anuncio anuncio;
    private UsuarioPUCP comprador;
    private Oferta oferta;
    private CitaEntrega citaEntrega;
    private final List<Calificacion> calificaciones;
    private final List<ReporteUsuario> reportes;

    public Transaccion() {
        this.estado = EstadoTransaccion.EN_NEGOCIACION;
        this.confirmacionComprador = false;
        this.confirmacionVendedor = false;
        this.calificaciones = new ArrayList<>();
        this.reportes = new ArrayList<>();
    }

    // El id y la fecha de inicio los genera la base de datos.
    public Transaccion(Anuncio anuncio, UsuarioPUCP comprador) {
        this();
        setAnuncio(anuncio);
        setComprador(comprador);
    }

    public boolean estaCompletada() {
        return estado == EstadoTransaccion.COMPLETADA;
    }

    // El vendedor no se duplica: se obtiene navegando por el anuncio.
    public UsuarioPUCP getVendedor() {
        return anuncio == null ? null : anuncio.getVendedor();
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public boolean isConfirmacionComprador() {
        return confirmacionComprador;
    }

    public void setConfirmacionComprador(boolean confirmacionComprador) {
        this.confirmacionComprador = confirmacionComprador;
    }

    public boolean isConfirmacionVendedor() {
        return confirmacionVendedor;
    }

    public void setConfirmacionVendedor(boolean confirmacionVendedor) {
        this.confirmacionVendedor = confirmacionVendedor;
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
            anterior.quitarTransaccion(this);
        }
        if (anuncio != null) {
            anuncio.agregarTransaccion(this);
        }
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
            anterior.quitarCompraRealizada(this);
        }
        if (comprador != null) {
            comprador.agregarCompraRealizada(this);
        }
    }

    public Oferta getOferta() {
        return oferta;
    }

    public void setOferta(Oferta oferta) {
        if (this.oferta == oferta) {
            return;
        }
        Oferta anterior = this.oferta;
        this.oferta = oferta;
        if (anterior != null && anterior.getTransaccion() == this) {
            anterior.setTransaccion(null);
        }
        if (oferta != null && oferta.getTransaccion() != this) {
            oferta.setTransaccion(this);
        }
    }

    public CitaEntrega getCitaEntrega() {
        return citaEntrega;
    }

    public void setCitaEntrega(CitaEntrega citaEntrega) {
        if (this.citaEntrega == citaEntrega) {
            return;
        }
        CitaEntrega anterior = this.citaEntrega;
        this.citaEntrega = citaEntrega;
        if (anterior != null && anterior.getTransaccion() == this) {
            anterior.setTransaccion(null);
        }
        if (citaEntrega != null && citaEntrega.getTransaccion() != this) {
            citaEntrega.setTransaccion(this);
        }
    }

    public List<Calificacion> getCalificaciones() {
        return Collections.unmodifiableList(calificaciones);
    }

    public List<ReporteUsuario> getReportes() {
        return Collections.unmodifiableList(reportes);
    }

    public void agregarCalificacion(Calificacion calificacion) {
        if (calificacion == null || calificaciones.contains(calificacion)) {
            return;
        }
        calificaciones.add(calificacion);
        calificacion.setTransaccion(this);
    }

    public void quitarCalificacion(Calificacion calificacion) {
        if (calificaciones.remove(calificacion) && calificacion.getTransaccion() == this) {
            calificacion.setTransaccion(null);
        }
    }

    public void agregarReporte(ReporteUsuario reporte) {
        if (reporte == null || reportes.contains(reporte)) {
            return;
        }
        reportes.add(reporte);
        reporte.setTransaccion(this);
    }

    public void quitarReporte(ReporteUsuario reporte) {
        if (reportes.remove(reporte) && reporte.getTransaccion() == this) {
            reporte.setTransaccion(null);
        }
    }
}

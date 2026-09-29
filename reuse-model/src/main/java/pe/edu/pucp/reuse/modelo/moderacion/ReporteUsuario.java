package pe.edu.pucp.reuse.modelo.moderacion;

import pe.edu.pucp.reuse.modelo.enums.MotivoReporteUsuario;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class ReporteUsuario extends Reporte {

    private MotivoReporteUsuario motivo;

    private UsuarioPUCP denunciado;
    private Transaccion transaccion;

    public ReporteUsuario() {
    }

    public ReporteUsuario(String descripcion, MotivoReporteUsuario motivo, UsuarioPUCP denunciante,
                          UsuarioPUCP denunciado) {
        super(descripcion, denunciante);
        this.motivo = motivo;
        setDenunciado(denunciado);
    }

    @Override
    public UsuarioPUCP getUsuarioResponsable() {
        return denunciado;
    }

    public MotivoReporteUsuario getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoReporteUsuario motivo) {
        this.motivo = motivo;
    }

    public UsuarioPUCP getDenunciado() {
        return denunciado;
    }

    public final void setDenunciado(UsuarioPUCP denunciado) {
        if (this.denunciado == denunciado) {
            return;
        }
        UsuarioPUCP anterior = this.denunciado;
        this.denunciado = denunciado;
        if (anterior != null) {
            anterior.quitarReporteRecibido(this);
        }
        if (denunciado != null) {
            denunciado.agregarReporteRecibido(this);
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
        if (anterior != null) {
            anterior.quitarReporte(this);
        }
        if (transaccion != null) {
            transaccion.agregarReporte(this);
        }
    }
}

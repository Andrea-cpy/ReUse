package pe.edu.pucp.reuse.modelo.usuarios;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;
import pe.edu.pucp.reuse.modelo.moderacion.Reporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class UsuarioPUCP extends Registro {

    private int idUsuario;
    private String codigoPUCP;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoInstitucional;
    private String contrasena;
    private Carrera carrera;
    private boolean verificado;
    private EstadoCuenta estadoCuenta;
    private double reputacion;
    private int contadorReportes;
    private String fotoPerfil;
    private LocalDateTime fechaRegistro;

    private final List<Anuncio> anunciosPublicados;
    private final List<Notificacion> notificaciones;
    private final List<Favorito> favoritos;
    private final List<RespuestaRapida> respuestasRapidas;
    private final List<Oferta> ofertasRealizadas;
    private final List<Transaccion> comprasRealizadas;
    private final List<Reporte> reportesRealizados;
    private final List<ReporteUsuario> reportesRecibidos;
    private final List<Bloqueo> bloqueosRealizados;
    private final List<Bloqueo> bloqueosRecibidos;
    private final List<CanalChat> chatsComoComprador;
    private final List<Calificacion> calificacionesRealizadas;
    private final List<Calificacion> calificacionesRecibidas;
    private final List<InsigniaUsuario> insignias;

    public UsuarioPUCP() {
        this.verificado = false;
        this.estadoCuenta = EstadoCuenta.PENDIENTE_VERIFICACION;
        this.reputacion = 0;
        this.contadorReportes = 0;
        this.anunciosPublicados = new ArrayList<>();
        this.notificaciones = new ArrayList<>();
        this.favoritos = new ArrayList<>();
        this.respuestasRapidas = new ArrayList<>();
        this.ofertasRealizadas = new ArrayList<>();
        this.comprasRealizadas = new ArrayList<>();
        this.reportesRealizados = new ArrayList<>();
        this.reportesRecibidos = new ArrayList<>();
        this.bloqueosRealizados = new ArrayList<>();
        this.bloqueosRecibidos = new ArrayList<>();
        this.chatsComoComprador = new ArrayList<>();
        this.calificacionesRealizadas = new ArrayList<>();
        this.calificacionesRecibidas = new ArrayList<>();
        this.insignias = new ArrayList<>();
    }

    // El id y la fecha de registro los genera la base de datos.
    public UsuarioPUCP(String codigoPUCP, String nombres, String apellidoPaterno, String apellidoMaterno,
                       String correoInstitucional, String contrasena, Carrera carrera) {
        this();
        this.codigoPUCP = codigoPUCP;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correoInstitucional = correoInstitucional;
        this.contrasena = contrasena;
        this.carrera = carrera;
    }

    // La facultad no se guarda en el usuario: se obtiene navegando por la carrera.
    public Facultad getFacultad() {
        return carrera == null ? null : carrera.getFacultad();
    }

    public String getNombreCompleto() {
        return apellidoMaterno == null
                ? nombres + " " + apellidoPaterno
                : nombres + " " + apellidoPaterno + " " + apellidoMaterno;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCodigoPUCP() {
        return codigoPUCP;
    }

    public void setCodigoPUCP(String codigoPUCP) {
        this.codigoPUCP = codigoPUCP;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public EstadoCuenta getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(EstadoCuenta estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public double getReputacion() {
        return reputacion;
    }

    public void setReputacion(double reputacion) {
        this.reputacion = reputacion;
    }

    public int getContadorReportes() {
        return contadorReportes;
    }

    public void setContadorReportes(int contadorReportes) {
        this.contadorReportes = contadorReportes;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Anuncio> getAnunciosPublicados() {
        return Collections.unmodifiableList(anunciosPublicados);
    }

    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }

    public List<Favorito> getFavoritos() {
        return Collections.unmodifiableList(favoritos);
    }

    public List<RespuestaRapida> getRespuestasRapidas() {
        return Collections.unmodifiableList(respuestasRapidas);
    }

    public List<Oferta> getOfertasRealizadas() {
        return Collections.unmodifiableList(ofertasRealizadas);
    }

    public List<Transaccion> getComprasRealizadas() {
        return Collections.unmodifiableList(comprasRealizadas);
    }

    // Las ventas se obtienen navegando por los anuncios publicados.
    public List<Transaccion> getVentasRealizadas() {
        List<Transaccion> ventas = new ArrayList<>();
        for (Anuncio anuncio : anunciosPublicados) {
            ventas.addAll(anuncio.getTransacciones());
        }
        return Collections.unmodifiableList(ventas);
    }

    public List<Reporte> getReportesRealizados() {
        return Collections.unmodifiableList(reportesRealizados);
    }

    public List<ReporteUsuario> getReportesRecibidos() {
        return Collections.unmodifiableList(reportesRecibidos);
    }

    public List<Bloqueo> getBloqueosRealizados() {
        return Collections.unmodifiableList(bloqueosRealizados);
    }

    public List<Bloqueo> getBloqueosRecibidos() {
        return Collections.unmodifiableList(bloqueosRecibidos);
    }

    public List<CanalChat> getChatsComoComprador() {
        return Collections.unmodifiableList(chatsComoComprador);
    }

    // Los chats como vendedor se obtienen navegando por los anuncios publicados.
    public List<CanalChat> getChatsComoVendedor() {
        List<CanalChat> chats = new ArrayList<>();
        for (Anuncio anuncio : anunciosPublicados) {
            chats.addAll(anuncio.getCanalesChat());
        }
        return Collections.unmodifiableList(chats);
    }

    public List<Calificacion> getCalificacionesRealizadas() {
        return Collections.unmodifiableList(calificacionesRealizadas);
    }

    public List<Calificacion> getCalificacionesRecibidas() {
        return Collections.unmodifiableList(calificacionesRecibidas);
    }

    public List<InsigniaUsuario> getInsignias() {
        return Collections.unmodifiableList(insignias);
    }

    public void agregarAnuncioPublicado(Anuncio anuncio) {
        if (anuncio == null || anunciosPublicados.contains(anuncio)) {
            return;
        }
        anunciosPublicados.add(anuncio);
        anuncio.setVendedor(this);
    }

    public void quitarAnuncioPublicado(Anuncio anuncio) {
        if (anunciosPublicados.remove(anuncio) && anuncio.getVendedor() == this) {
            anuncio.setVendedor(null);
        }
    }

    public void agregarNotificacion(Notificacion notificacion) {
        if (notificacion == null || notificaciones.contains(notificacion)) {
            return;
        }
        notificaciones.add(notificacion);
        notificacion.setDestinatario(this);
    }

    public void quitarNotificacion(Notificacion notificacion) {
        if (notificaciones.remove(notificacion) && notificacion.getDestinatario() == this) {
            notificacion.setDestinatario(null);
        }
    }

    public void agregarFavorito(Favorito favorito) {
        if (favorito == null || favoritos.contains(favorito)) {
            return;
        }
        favoritos.add(favorito);
        favorito.setUsuario(this);
    }

    public void quitarFavorito(Favorito favorito) {
        if (favoritos.remove(favorito) && favorito.getUsuario() == this) {
            favorito.setUsuario(null);
        }
    }

    public void agregarRespuestaRapida(RespuestaRapida respuestaRapida) {
        if (respuestaRapida == null || respuestasRapidas.contains(respuestaRapida)) {
            return;
        }
        respuestasRapidas.add(respuestaRapida);
        respuestaRapida.setCreador(this);
    }

    public void quitarRespuestaRapida(RespuestaRapida respuestaRapida) {
        if (respuestasRapidas.remove(respuestaRapida) && respuestaRapida.getCreador() == this) {
            respuestaRapida.setCreador(null);
        }
    }

    public void agregarOfertaRealizada(Oferta oferta) {
        if (oferta == null || ofertasRealizadas.contains(oferta)) {
            return;
        }
        ofertasRealizadas.add(oferta);
        oferta.setComprador(this);
    }

    public void quitarOfertaRealizada(Oferta oferta) {
        if (ofertasRealizadas.remove(oferta) && oferta.getComprador() == this) {
            oferta.setComprador(null);
        }
    }

    public void agregarCompraRealizada(Transaccion transaccion) {
        if (transaccion == null || comprasRealizadas.contains(transaccion)) {
            return;
        }
        comprasRealizadas.add(transaccion);
        transaccion.setComprador(this);
    }

    public void quitarCompraRealizada(Transaccion transaccion) {
        if (comprasRealizadas.remove(transaccion) && transaccion.getComprador() == this) {
            transaccion.setComprador(null);
        }
    }

    public void agregarReporteRealizado(Reporte reporte) {
        if (reporte == null || reportesRealizados.contains(reporte)) {
            return;
        }
        reportesRealizados.add(reporte);
        reporte.setDenunciante(this);
    }

    public void quitarReporteRealizado(Reporte reporte) {
        if (reportesRealizados.remove(reporte) && reporte.getDenunciante() == this) {
            reporte.setDenunciante(null);
        }
    }

    // contadorReportes ya no se calcula aqui: lo mantiene la capa de negocio en la base de datos.
    public void agregarReporteRecibido(ReporteUsuario reporteUsuario) {
        if (reporteUsuario == null || reportesRecibidos.contains(reporteUsuario)) {
            return;
        }
        reportesRecibidos.add(reporteUsuario);
        reporteUsuario.setDenunciado(this);
    }

    public void quitarReporteRecibido(ReporteUsuario reporteUsuario) {
        if (reportesRecibidos.remove(reporteUsuario) && reporteUsuario.getDenunciado() == this) {
            reporteUsuario.setDenunciado(null);
        }
    }

    public void agregarBloqueoRealizado(Bloqueo bloqueo) {
        if (bloqueo == null || bloqueosRealizados.contains(bloqueo)) {
            return;
        }
        bloqueosRealizados.add(bloqueo);
        bloqueo.setBloqueador(this);
    }

    public void quitarBloqueoRealizado(Bloqueo bloqueo) {
        if (bloqueosRealizados.remove(bloqueo) && bloqueo.getBloqueador() == this) {
            bloqueo.setBloqueador(null);
        }
    }

    public void agregarBloqueoRecibido(Bloqueo bloqueo) {
        if (bloqueo == null || bloqueosRecibidos.contains(bloqueo)) {
            return;
        }
        bloqueosRecibidos.add(bloqueo);
        bloqueo.setBloqueado(this);
    }

    public void quitarBloqueoRecibido(Bloqueo bloqueo) {
        if (bloqueosRecibidos.remove(bloqueo) && bloqueo.getBloqueado() == this) {
            bloqueo.setBloqueado(null);
        }
    }

    public void agregarChatComoComprador(CanalChat canalChat) {
        if (canalChat == null || chatsComoComprador.contains(canalChat)) {
            return;
        }
        chatsComoComprador.add(canalChat);
        canalChat.setComprador(this);
    }

    public void quitarChatComoComprador(CanalChat canalChat) {
        if (chatsComoComprador.remove(canalChat) && canalChat.getComprador() == this) {
            canalChat.setComprador(null);
        }
    }

    public void agregarCalificacionRealizada(Calificacion calificacion) {
        if (calificacion == null || calificacionesRealizadas.contains(calificacion)) {
            return;
        }
        calificacionesRealizadas.add(calificacion);
        calificacion.setCalificador(this);
    }

    public void quitarCalificacionRealizada(Calificacion calificacion) {
        if (calificacionesRealizadas.remove(calificacion) && calificacion.getCalificador() == this) {
            calificacion.setCalificador(null);
        }
    }

    public void agregarCalificacionRecibida(Calificacion calificacion) {
        if (calificacion == null || calificacionesRecibidas.contains(calificacion)) {
            return;
        }
        calificacionesRecibidas.add(calificacion);
        calificacion.setCalificado(this);
    }

    public void quitarCalificacionRecibida(Calificacion calificacion) {
        if (calificacionesRecibidas.remove(calificacion) && calificacion.getCalificado() == this) {
            calificacion.setCalificado(null);
        }
    }

    public void agregarInsignia(InsigniaUsuario insigniaUsuario) {
        if (insigniaUsuario == null || insignias.contains(insigniaUsuario)) {
            return;
        }
        insignias.add(insigniaUsuario);
        insigniaUsuario.setUsuario(this);
    }

    public void quitarInsignia(InsigniaUsuario insigniaUsuario) {
        if (insignias.remove(insigniaUsuario) && insigniaUsuario.getUsuario() == this) {
            insigniaUsuario.setUsuario(null);
        }
    }
}

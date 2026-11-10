package pe.edu.pucp.reuse.rest.dto;

public class RespuestaRapidaDTO {
    private int idRespuesta;
    private String texto;
    private int idCreador;

    public RespuestaRapidaDTO() {
    }

    public int getIdRespuesta() {
        return idRespuesta;
    }

    public void setIdRespuesta(int idRespuesta) {
        this.idRespuesta = idRespuesta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(int idCreador) {
        this.idCreador = idCreador;
    }
}

package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;

public interface MensajeBL extends RegistroBL<Mensaje> {

    ArrayList<Mensaje> listarPorChat(int idChat) throws BLException;

    // Envia como mensaje el texto de una respuesta rapida del emisor.
    int enviarRespuestaRapida(int idChat, int idEmisor, int idRespuestaRapida) throws BLException;
}

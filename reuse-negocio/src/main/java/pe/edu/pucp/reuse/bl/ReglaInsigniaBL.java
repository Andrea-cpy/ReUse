package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public interface ReglaInsigniaBL extends RegistroBL<ReglaInsignia> {

    ArrayList<ReglaInsignia> listarPorInsignia(int idInsignia) throws BLException;
}

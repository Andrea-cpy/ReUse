package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.academico.Carrera;

public interface CarreraBL extends RegistroBL<Carrera> {

    ArrayList<Carrera> listarPorFacultad(int idFacultad) throws BLException;
}

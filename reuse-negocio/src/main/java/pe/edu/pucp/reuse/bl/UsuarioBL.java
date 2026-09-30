package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public interface UsuarioBL extends RegistroBL<UsuarioPUCP> {

    UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws BLException;

    void verificarCuenta(int idUsuario) throws BLException;

    void suspenderCuenta(int idUsuario) throws BLException;

    void recalcularReputacion(int idUsuario) throws BLException;

    NivelReputacion obtenerNivelReputacion(int idUsuario) throws BLException;

    double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws BLException;
}

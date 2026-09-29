package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

/**
 * Administrador = usuario + administrador: insert, update y delete se
 * ejecutan en una transaccion porque escriben en dos tablas.
 */
public interface AdministradorBL extends RegistroBL<AdministradorPUCP> {
}

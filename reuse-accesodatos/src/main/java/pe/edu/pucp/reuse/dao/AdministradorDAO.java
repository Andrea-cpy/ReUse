package pe.edu.pucp.reuse.dao;

import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

/**
 * Administrador es un subtipo de usuario (tablas usuario + administrador).
 * insert, update y delete escriben en ambas tablas y requieren una
 * transaccion abierta por la capa de negocio.
 */
public interface AdministradorDAO extends DAO<AdministradorPUCP> {
}

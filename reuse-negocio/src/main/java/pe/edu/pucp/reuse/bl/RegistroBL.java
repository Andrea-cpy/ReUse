package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

/**
 * Operaciones CRUD de negocio comunes a todas las entidades. Cada metodo
 * valida las reglas antes de llamar al DAO.
 */
public interface RegistroBL<T> {

    int insert(T entidad) throws BLException;

    int update(T entidad) throws BLException;

    int delete(int id) throws BLException;

    T findById(int id) throws BLException;

    ArrayList<T> findAll() throws BLException;
}

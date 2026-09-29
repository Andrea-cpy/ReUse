package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

public interface DAO<T> {

    int insert(T entidad) throws SQLException;

    int update(T entidad) throws SQLException;

    int delete(int id) throws SQLException;

    T findById(int id) throws SQLException;

    ArrayList<T> findAll() throws SQLException;
}

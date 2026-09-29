package pe.edu.pucp.reuse.dao;

import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;

/**
 * Material academico con sus carreras (tabla intermedia material_carrera, N:M).
 * insert, update y delete escriben en ambas tablas y requieren una
 * transaccion abierta por la capa de negocio.
 */
public interface MaterialAcademicoDAO extends DAO<MaterialAcademico> {
}

package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;

/**
 * Material + sus carreras (N:M): insert, update y delete se ejecutan en
 * una transaccion porque escriben en material_academico y material_carrera.
 */
public interface MaterialAcademicoBL extends RegistroBL<MaterialAcademico> {
}

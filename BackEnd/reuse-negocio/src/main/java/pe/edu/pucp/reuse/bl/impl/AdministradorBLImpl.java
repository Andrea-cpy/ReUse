package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.AdministradorBL;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.dao.AdministradorDAO;
import pe.edu.pucp.reuse.dao.impl.AdministradorDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

public class AdministradorBLImpl extends BaseBLImpl implements AdministradorBL {

    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    // Un administrador es un usuario: reutiliza sus validaciones (correo PUCP, codigo, unicidad).
    private final UsuarioBLImpl usuarioBL = new UsuarioBLImpl();

    @Override
    public int insert(AdministradorPUCP administrador) throws BLException {
        validarDatos(administrador);
        usuarioBL.validarDatosPersonales(administrador);
        UsuarioBLImpl.validarContrasena(administrador.getContrasena());
        usuarioBL.validarUnicidad(administrador);
        administrador.setVerificado(true);
        administrador.setEstadoCuenta(EstadoCuenta.ACTIVA);
        administrador.setContrasena(UsuarioBLImpl.hashear(administrador.getContrasena()));

        // usuario + administrador: ambas filas o ninguna.
        TransactionsManager.iniciar();
        try {
            int idUsuario = administradorDAO.insert(administrador);
            TransactionsManager.commit();
            return idUsuario;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo registrar el administrador", e);
        }
    }

    // Igual que en usuario, solo se modifican los datos del perfil.
    @Override
    public int update(AdministradorPUCP administrador) throws BLException {
        validarDatos(administrador);
        validarExiste(administrador.getIdUsuario());

        TransactionsManager.iniciar();
        try {
            int filas = administradorDAO.update(administrador);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo actualizar el administrador", e);
        }
    }

    @Override
    public int delete(int idUsuario) throws BLException {
        validarExiste(idUsuario);
        try {
            if (administradorDAO.contarReportesPendientes(idUsuario) > 0) {
                throw new BLException("No se puede eliminar un administrador con reportes PENDIENTE asignados;"
                        + " reasignelos primero");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar los reportes del administrador", e);
        }

        TransactionsManager.iniciar();
        try {
            int filas = administradorDAO.delete(idUsuario);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo eliminar el administrador", e);
        }
    }

    @Override
    public AdministradorPUCP findById(int idUsuario) throws BLException {
        try {
            return administradorDAO.findById(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el administrador", e);
        }
    }

    @Override
    public ArrayList<AdministradorPUCP> findAll() throws BLException {
        try {
            return administradorDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los administradores", e);
        }
    }

    private void validarDatos(AdministradorPUCP administrador) throws BLException {
        usuarioBL.validarPerfil(administrador);
        if (administrador.getCarrera() != null) {
            throw new BLException("Un administrador no tiene carrera");
        }
    }

    private void validarExiste(int idUsuario) throws BLException {
        if (findById(idUsuario) == null) {
            throw new BLException("No existe un administrador con id " + idUsuario);
        }
    }
}

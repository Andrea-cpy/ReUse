package pe.edu.pucp.reuse.rest;

import java.util.HashMap;
import java.util.Map;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/**
 * Los listados de transacciones y chats traen solo el id del vendedor.
 * Esta clase completa su nombre consultando a la capa de negocio una vez por usuario.
 */
final class NombresUsuario {
    private final UsuarioBL usuarioBL = new UsuarioBLImpl();
    private final Map<Integer, String> cache = new HashMap<>();

    String de(int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return null;
        }
        if (!cache.containsKey(idUsuario)) {
            UsuarioPUCP usuario = usuarioBL.findById(idUsuario);
            cache.put(idUsuario, usuario == null ? null : UsuarioMapper.nombre(usuario));
        }
        return cache.get(idUsuario);
    }
}

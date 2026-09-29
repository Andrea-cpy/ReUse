/*
 * ReUse - Laboratorio N. 06: programa de prueba de las tres capas.
 * Solo usa la capa de negocio (BL): nunca llama a un DAO ni escribe SQL.
 * Requiere haber ejecutado sql/MySQL/reuse_schema.sql y que db.properties
 * apunte al esquema reuse_db.
 */
package pe.edu.pucp.reuse.main;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.ToIntFunction;

import pe.edu.pucp.reuse.bl.AnuncioBL;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CalificacionBL;
import pe.edu.pucp.reuse.bl.CanalChatBL;
import pe.edu.pucp.reuse.bl.CarreraBL;
import pe.edu.pucp.reuse.bl.CategoriaMaterialBL;
import pe.edu.pucp.reuse.bl.CitaEntregaBL;
import pe.edu.pucp.reuse.bl.FacultadBL;
import pe.edu.pucp.reuse.bl.FavoritoBL;
import pe.edu.pucp.reuse.bl.InsigniaUsuarioBL;
import pe.edu.pucp.reuse.bl.MaterialAcademicoBL;
import pe.edu.pucp.reuse.bl.MensajeBL;
import pe.edu.pucp.reuse.bl.OfertaBL;
import pe.edu.pucp.reuse.bl.PuntoEntregaBL;
import pe.edu.pucp.reuse.bl.ReporteAnuncioBL;
import pe.edu.pucp.reuse.bl.TransaccionBL;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.bl.impl.AnuncioBLImpl;
import pe.edu.pucp.reuse.bl.impl.CalificacionBLImpl;
import pe.edu.pucp.reuse.bl.impl.CanalChatBLImpl;
import pe.edu.pucp.reuse.bl.impl.CarreraBLImpl;
import pe.edu.pucp.reuse.bl.impl.CategoriaMaterialBLImpl;
import pe.edu.pucp.reuse.bl.impl.CitaEntregaBLImpl;
import pe.edu.pucp.reuse.bl.impl.FacultadBLImpl;
import pe.edu.pucp.reuse.bl.impl.FavoritoBLImpl;
import pe.edu.pucp.reuse.bl.impl.InsigniaUsuarioBLImpl;
import pe.edu.pucp.reuse.bl.impl.MaterialAcademicoBLImpl;
import pe.edu.pucp.reuse.bl.impl.MensajeBLImpl;
import pe.edu.pucp.reuse.bl.impl.OfertaBLImpl;
import pe.edu.pucp.reuse.bl.impl.PuntoEntregaBLImpl;
import pe.edu.pucp.reuse.bl.impl.ReporteAnuncioBLImpl;
import pe.edu.pucp.reuse.bl.impl.TransaccionBLImpl;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteAnuncio;
import pe.edu.pucp.reuse.modelo.enums.TipoCalificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Principal {

    // Datos iniciales cargados por sql/MySQL/reuse_schema.sql.
    private static final int ID_CARRERA_MATEMATICAS = 16;
    private static final int ID_CARRERA_INDUSTRIAL = 25;
    private static final int ID_CARRERA_INFORMATICA = 26;
    private static final int ID_CATEGORIA_LIBRO = 1;
    private static final int ID_PUNTO_BIBLIOTECA = 1;
    private static final int ID_PUNTO_COMEDOR = 2;
    private static final int ID_PUNTO_DESHABILITADO = 3;
    private static final int ID_USUARIO_BRUNO = 2;
    private static final int ID_ADMINISTRADOR = 7;
    private static final int ID_INSIGNIA_PRIMERA_VENTA = 1;

    // Sufijo y codigos distintos en cada ejecucion para no chocar con las reglas de unicidad.
    private static final long RUN = System.currentTimeMillis() % 1_000_000L;
    private static long secuenciaCodigo = 10_000_000L + (System.currentTimeMillis() / 1000) % 80_000_000L;

    private static final FacultadBL facultadBL = new FacultadBLImpl();
    private static final CarreraBL carreraBL = new CarreraBLImpl();
    private static final UsuarioBL usuarioBL = new UsuarioBLImpl();
    private static final CategoriaMaterialBL categoriaBL = new CategoriaMaterialBLImpl();
    private static final MaterialAcademicoBL materialBL = new MaterialAcademicoBLImpl();
    private static final AnuncioBL anuncioBL = new AnuncioBLImpl();
    private static final FavoritoBL favoritoBL = new FavoritoBLImpl();
    private static final CanalChatBL chatBL = new CanalChatBLImpl();
    private static final MensajeBL mensajeBL = new MensajeBLImpl();
    private static final OfertaBL ofertaBL = new OfertaBLImpl();
    private static final TransaccionBL transaccionBL = new TransaccionBLImpl();
    private static final PuntoEntregaBL puntoEntregaBL = new PuntoEntregaBLImpl();
    private static final CitaEntregaBL citaBL = new CitaEntregaBLImpl();
    private static final ReporteAnuncioBL reporteAnuncioBL = new ReporteAnuncioBLImpl();
    private static final CalificacionBL calificacionBL = new CalificacionBLImpl();
    private static final InsigniaUsuarioBL insigniaUsuarioBL = new InsigniaUsuarioBLImpl();

    public static void main(String[] args) {
        try {
            probarFacultadesYCarreras();
            UsuarioPUCP[] usuarios = probarUsuarios();
            UsuarioPUCP vendedor = usuarios[0];
            UsuarioPUCP comprador = usuarios[1];
            MaterialAcademico material = probarMateriales();
            Anuncio anuncio = probarAnuncios(vendedor, comprador, material);
            int idTransaccion = probarOfertas(vendedor, comprador, anuncio);
            probarCitasYTransacciones(vendedor, comprador, material, anuncio, idTransaccion);
            System.out.println();
            System.out.println("Pruebas terminadas correctamente.");
        } catch (BLException ex) {
            System.out.println();
            System.out.println("La prueba se detuvo por un error de negocio: " + ex.getMessage());
            if (ex.getCause() != null) {
                System.out.println("  Causa: " + ex.getCause().getMessage());
            }
        } catch (RuntimeException ex) {
            System.out.println();
            System.out.println("Error de infraestructura (revise db.properties y la conexion): " + ex.getMessage());
        }
    }

    // ================================================================
    // 1. FACULTAD y CARRERA: catalogos que reemplazan a los enums (Obs. 1)
    // ================================================================
    private static void probarFacultadesYCarreras() throws BLException {
        titulo("1. FACULTAD y CARRERA (tablas en lugar de ENUM)");

        Facultad facultad = new Facultad("Facultad de Prueba " + RUN);
        facultadBL.insert(facultad);
        System.out.println("Insertada facultad:  id " + facultad.getIdFacultad() + " (id generado por AUTO_INCREMENT)");

        facultad = facultadBL.findById(facultad.getIdFacultad());
        System.out.println("Recuperada:          " + facultad.getNombre() + " | creada " + facultad.getFechaCreacion()
                + " por " + facultad.getUsuarioCreacion());

        facultad.setNombre("Facultad de Prueba " + RUN + " (editada)");
        facultad.setUsuarioModificacion("tester");
        facultadBL.update(facultad);
        System.out.println("Modificada:          " + facultadBL.findById(facultad.getIdFacultad()).getNombre());

        Carrera carrera = new Carrera("Carrera de Prueba " + RUN, facultad);
        carreraBL.insert(carrera);
        carrera = carreraBL.findById(carrera.getIdCarrera());
        System.out.println("Insertada carrera:   id " + carrera.getIdCarrera() + " " + carrera.getNombre()
                + " -> facultad " + carrera.getFacultad().getNombre());

        carrera.setNombre("Carrera de Prueba " + RUN + " (editada)");
        carreraBL.update(carrera);
        System.out.println("Carrera modificada:  " + carreraBL.findById(carrera.getIdCarrera()).getNombre());
        System.out.println("Listado:             " + facultadBL.findAll().size() + " facultades y "
                + carreraBL.findAll().size() + " carreras activas");

        int idFacultad = facultad.getIdFacultad();
        rechazo("Eliminar una facultad con carreras activas", () -> facultadBL.delete(idFacultad));

        carreraBL.delete(carrera.getIdCarrera());
        facultadBL.delete(idFacultad);
        System.out.println("Eliminadas (logico): carrera activo=" + carreraBL.findById(carrera.getIdCarrera()).isActivo()
                + ", facultad activo=" + facultadBL.findById(idFacultad).isActivo()
                + ", facultad en listado=" + contiene(facultadBL.findAll(), Facultad::getIdFacultad, idFacultad));
    }

    // ================================================================
    // 2. USUARIO (Entidad 1): correo @pucp.edu.pe, UNIQUE, fecha de la BD
    // ================================================================
    private static UsuarioPUCP[] probarUsuarios() throws BLException {
        titulo("2. USUARIO");
        Carrera informatica = carreraBL.findById(ID_CARRERA_INFORMATICA);
        Carrera industrial = carreraBL.findById(ID_CARRERA_INDUSTRIAL);

        rechazo("Correo que no es @pucp.edu.pe", () -> usuarioBL.insert(
                nuevoUsuario("Correo", "Invalido", informatica, "prueba" + RUN + "@gmail.com")));
        rechazo("Codigo PUCP de 7 digitos", () -> {
            UsuarioPUCP usuario = nuevoUsuario("Codigo", "Corto", informatica, null);
            usuario.setCodigoPUCP("1234567");
            usuarioBL.insert(usuario);
        });
        rechazo("Correo repetido (UNIQUE)", () -> usuarioBL.insert(
                nuevoUsuario("Correo", "Repetido", informatica, "a20260001@pucp.edu.pe")));

        UsuarioPUCP vendedor = nuevoUsuario("Valeria", "Vendedora", informatica, null);
        usuarioBL.insert(vendedor);
        UsuarioPUCP comprador = nuevoUsuario("Carlos", "Comprador", industrial, null);
        usuarioBL.insert(comprador);
        System.out.println("Insertados:          vendedor id " + vendedor.getIdUsuario() + ", comprador id "
                + comprador.getIdUsuario() + " (estado " + usuarioBL.findById(vendedor.getIdUsuario()).getEstadoCuenta() + ")");

        usuarioBL.verificarCuenta(vendedor.getIdUsuario());
        usuarioBL.verificarCuenta(comprador.getIdUsuario());

        vendedor = usuarioBL.findById(vendedor.getIdUsuario());
        System.out.println("Recuperado:          " + vendedor.getNombreCompleto() + " | " + vendedor.getCorreoInstitucional()
                + " | " + vendedor.getEstadoCuenta() + " | registrado " + vendedor.getFechaRegistro());
        System.out.println("Navegacion:          carrera " + vendedor.getCarrera().getNombre()
                + " -> facultad " + vendedor.getFacultad().getNombre());

        vendedor.setApellidoMaterno("Prueba");
        vendedor.setFotoPerfil("https://example.org/reuse/perfil-" + RUN + ".jpg");
        usuarioBL.update(vendedor);
        System.out.println("Modificado:          " + usuarioBL.findById(vendedor.getIdUsuario()).getNombreCompleto());

        UsuarioPUCP temporal = nuevoUsuario("Temporal", "Eliminable", informatica, null);
        usuarioBL.insert(temporal);
        usuarioBL.delete(temporal.getIdUsuario());
        System.out.println("Eliminado (logico):  id " + temporal.getIdUsuario() + " activo="
                + usuarioBL.findById(temporal.getIdUsuario()).isActivo() + ", en listado="
                + contiene(usuarioBL.findAll(), UsuarioPUCP::getIdUsuario, temporal.getIdUsuario()));
        System.out.println("Listado:             " + usuarioBL.findAll().size() + " usuarios activos");

        comprador = usuarioBL.findById(comprador.getIdUsuario());
        return new UsuarioPUCP[] {vendedor, comprador};
    }

    // ================================================================
    // 3. MATERIAL ACADEMICO: relacion N:M con carreras (Obs. 5), en transaccion
    // ================================================================
    private static MaterialAcademico probarMateriales() throws BLException {
        titulo("3. MATERIAL ACADEMICO (una o mas carreras)");
        Carrera informatica = carreraBL.findById(ID_CARRERA_INFORMATICA);
        Carrera industrial = carreraBL.findById(ID_CARRERA_INDUSTRIAL);
        Carrera matematicas = carreraBL.findById(ID_CARRERA_MATEMATICAS);

        rechazo("Material sin carreras", () -> materialBL.insert(new MaterialAcademico(
                "Sin carreras " + RUN, categoriaBL.findById(ID_CATEGORIA_LIBRO), List.of())));

        MaterialAcademico material = new MaterialAcademico("Fisica universitaria " + RUN,
                categoriaBL.findById(ID_CATEGORIA_LIBRO), List.of(informatica, industrial));
        materialBL.insert(material);
        material = materialBL.findById(material.getIdMaterial());
        System.out.println("Insertado:           id " + material.getIdMaterial() + " " + material.getTitulo()
                + " | categoria " + material.getCategoria().getNombre());
        imprimirCarreras(material);

        material.agregarCarrera(matematicas);
        material.setTitulo("Fisica universitaria " + RUN + " (13a ed.)");
        materialBL.update(material);
        material = materialBL.findById(material.getIdMaterial());
        System.out.println("Modificado:          " + material.getTitulo());
        imprimirCarreras(material);
        System.out.println("Listado:             " + materialBL.findAll().size() + " materiales activos");

        MaterialAcademico temporal = new MaterialAcademico("Material temporal " + RUN,
                categoriaBL.findById(ID_CATEGORIA_LIBRO), List.of(informatica));
        materialBL.insert(temporal);
        materialBL.delete(temporal.getIdMaterial());
        System.out.println("Eliminado (logico):  id " + temporal.getIdMaterial() + " activo="
                + materialBL.findById(temporal.getIdMaterial()).isActivo());
        return material;
    }

    // ================================================================
    // 4. ANUNCIO (Entidad 2): anuncio + imagenes en una transaccion
    // ================================================================
    private static Anuncio probarAnuncios(UsuarioPUCP vendedor, UsuarioPUCP comprador, MaterialAcademico material)
            throws BLException {
        titulo("4. ANUNCIO");

        rechazo("Precio igual a 0", () -> anuncioBL.insert(new Anuncio("Libro gratis " + RUN, 0,
                "Sin precio", CondicionMaterial.USADO, vendedor, material)));

        Anuncio anuncio = new Anuncio("Libro de fisica " + RUN, 60.00, "Buen estado, sin subrayados.",
                CondicionMaterial.USADO, vendedor, material);
        new ImagenProducto("https://example.org/reuse/fisica-portada-" + RUN + ".jpg", 150_000, "jpg", anuncio);
        new ImagenProducto("https://example.org/reuse/fisica-indice-" + RUN + ".png", 90_000, "png", anuncio);
        anuncioBL.insert(anuncio);
        Anuncio recuperado = anuncioBL.findById(anuncio.getIdAnuncio());
        System.out.println("Insertado (COMMIT):  id " + recuperado.getIdAnuncio() + " " + recuperado.getTitulo()
                + " | " + recuperado.getEstado() + " | " + recuperado.getImagenes().size() + " imagenes"
                + " | publicado " + recuperado.getFechaPublicacion());

        // ROLLBACK: la segunda imagen excede el VARCHAR(500) de la columna url.
        Anuncio fallido = new Anuncio("Anuncio que no debe quedar " + RUN, 20.00, "Prueba de rollback",
                CondicionMaterial.NUEVO, vendedor, material);
        new ImagenProducto("https://example.org/reuse/ok-" + RUN + ".jpg", 1_000, "jpg", fallido);
        new ImagenProducto("https://example.org/" + "x".repeat(600) + ".jpg", 1_000, "jpg", fallido);
        rechazo("Anuncio cuya segunda imagen falla en la BD", () -> anuncioBL.insert(fallido));
        System.out.println("  Verificacion:      anuncio id " + fallido.getIdAnuncio() + " en BD = "
                + (anuncioBL.findById(fallido.getIdAnuncio()) != null)
                + " -> ROLLBACK: ni el anuncio ni su primera imagen se guardaron");

        recuperado.setPrecio(55.00);
        recuperado.setDescripcion("Buen estado, sin subrayados. Precio rebajado.");
        anuncioBL.update(recuperado);
        System.out.printf("Modificado:          precio S/ %.2f%n", anuncioBL.findById(anuncio.getIdAnuncio()).getPrecio());
        System.out.println("Listado:             " + anuncioBL.findAll().size() + " anuncios activos");

        Anuncio temporal = new Anuncio("Anuncio temporal " + RUN, 10.00, "Se eliminara",
                CondicionMaterial.NUEVO, vendedor, material);
        anuncioBL.insert(temporal);
        anuncioBL.delete(temporal.getIdAnuncio());
        System.out.println("Eliminado (logico):  id " + temporal.getIdAnuncio() + " activo="
                + anuncioBL.findById(temporal.getIdAnuncio()).isActivo()
                + ", en listado=" + contiene(anuncioBL.findAll(), Anuncio::getIdAnuncio, temporal.getIdAnuncio()));

        // RF-07 favoritos y RF-09/RF-08 solicitud de contacto y chat.
        favoritoBL.insert(new Favorito(comprador, recuperado));
        rechazo("Guardar dos veces el mismo favorito", () -> favoritoBL.insert(new Favorito(comprador, recuperado)));
        System.out.println("Favoritos comprador: " + favoritoBL.listarPorUsuario(comprador.getIdUsuario()).size());

        CanalChat chat = new CanalChat(recuperado, comprador);
        chatBL.insert(chat);
        rechazo("Escribir en un chat PENDIENTE", () -> mensajeBL.insert(new Mensaje("Hola", chat, comprador)));
        chatBL.aceptarSolicitud(chat.getIdChat());
        mensajeBL.insert(new Mensaje("Hola, sigue disponible el libro?", chat, comprador));
        mensajeBL.insert(new Mensaje("Si, podemos coordinar la entrega.", chat, vendedor));
        System.out.println("Chat " + chat.getIdChat() + ":             " + chatBL.findById(chat.getIdChat()).getEstado()
                + " con " + mensajeBL.listarPorChat(chat.getIdChat()).size() + " mensajes");
        return recuperado;
    }

    // ================================================================
    // 5. OFERTA (Entidad 3): aceptar = 3 escrituras en una transaccion
    // ================================================================
    private static int probarOfertas(UsuarioPUCP vendedor, UsuarioPUCP comprador, Anuncio anuncio)
            throws BLException {
        titulo("5. OFERTA");

        rechazo("Oferta de S/ 0", () -> ofertaBL.insert(new Oferta(0, comprador, anuncio)));
        rechazo("Oferta mayor al precio publicado", () -> ofertaBL.insert(new Oferta(999, comprador, anuncio)));
        rechazo("El vendedor oferta por su propio anuncio", () -> ofertaBL.insert(new Oferta(40, vendedor, anuncio)));

        Oferta oferta = new Oferta(45.00, comprador, anuncio);
        ofertaBL.insert(oferta);
        oferta = ofertaBL.findById(oferta.getIdOferta());
        System.out.printf("Insertada:           id %d | S/ %.2f | %s | fecha de la BD %s%n",
                oferta.getIdOferta(), oferta.getMontoPropuesto(), oferta.getEstado(), oferta.getFecha());

        oferta.setMontoPropuesto(48.00);
        ofertaBL.update(oferta);
        System.out.printf("Modificada:          S/ %.2f%n", ofertaBL.findById(oferta.getIdOferta()).getMontoPropuesto());

        Oferta temporal = new Oferta(30.00, comprador, anuncio);
        ofertaBL.insert(temporal);
        ofertaBL.delete(temporal.getIdOferta());
        System.out.println("Eliminada (logico):  id " + temporal.getIdOferta() + " activo="
                + ofertaBL.findById(temporal.getIdOferta()).isActivo());
        System.out.println("Listado:             " + ofertaBL.findAll().size() + " ofertas activas");

        int idTransaccion = ofertaBL.aceptar(oferta.getIdOferta());
        System.out.println("Aceptada (COMMIT):   oferta " + ofertaBL.findById(oferta.getIdOferta()).getEstado()
                + " -> se creo la transaccion " + idTransaccion);
        return idTransaccion;
    }

    // ================================================================
    // 6. CITA DE ENTREGA y 7. TRANSACCION (Entidad 4): commit y rollback
    // ================================================================
    private static void probarCitasYTransacciones(UsuarioPUCP vendedor, UsuarioPUCP comprador,
                                                  MaterialAcademico material, Anuncio anuncio, int idTransaccion)
            throws BLException {
        titulo("6. CITA DE ENTREGA");
        PuntoEntrega biblioteca = puntoEntregaBL.findById(ID_PUNTO_BIBLIOTECA);
        PuntoEntrega comedor = puntoEntregaBL.findById(ID_PUNTO_COMEDOR);
        PuntoEntrega deshabilitado = puntoEntregaBL.findById(ID_PUNTO_DESHABILITADO);

        Transaccion t1 = transaccionBL.findById(idTransaccion);
        rechazo("Cita en un punto de entrega deshabilitado", () -> citaBL.insert(
                new CitaEntrega(LocalDateTime.now().plusDays(2), t1, deshabilitado)));
        rechazo("Cita en una fecha pasada", () -> citaBL.insert(
                new CitaEntrega(LocalDateTime.now().minusDays(1), t1, biblioteca)));

        CitaEntrega cita = new CitaEntrega(LocalDateTime.now().plusDays(2).withNano(0), t1, biblioteca);
        citaBL.insert(cita);
        cita = citaBL.findById(cita.getIdCita());
        System.out.println("Insertada:           cita " + cita.getIdCita() + " | " + cita.getEstado() + " | "
                + cita.getFechaHora() + " en " + cita.getPuntoEntrega().getNombre());

        cita.setFechaHora(LocalDateTime.now().plusDays(3).withNano(0));
        cita.setPuntoEntrega(comedor);
        citaBL.update(cita);
        cita = citaBL.findById(cita.getIdCita());
        System.out.println("Renegociada:         " + cita.getFechaHora() + " en " + cita.getPuntoEntrega().getNombre());
        System.out.println("Listado:             " + citaBL.findAll().size() + " citas activas");

        titulo("7. TRANSACCION");
        UsuarioPUCP bruno = usuarioBL.findById(ID_USUARIO_BRUNO);
        System.out.println("Recuperada:          " + describir(transaccionBL.findById(idTransaccion)));

        rechazo("El vendedor negocia su propio anuncio", () -> transaccionBL.insert(new Transaccion(anuncio, vendedor)));
        Transaccion t2 = new Transaccion(anuncio, bruno);
        transaccionBL.insert(t2);
        System.out.println("Insertada:           " + describir(transaccionBL.findById(t2.getIdTransaccion())));

        Oferta ofertaBruno = new Oferta(50.00, bruno, anuncio);
        ofertaBL.insert(ofertaBruno);
        Transaccion t2Recuperada = transaccionBL.findById(t2.getIdTransaccion());
        t2Recuperada.setOferta(ofertaBruno);
        transaccionBL.update(t2Recuperada);
        System.out.printf("Modificada:          transaccion %d vinculada a la oferta %d (S/ %.2f)%n",
                t2.getIdTransaccion(), ofertaBruno.getIdOferta(), ofertaBruno.getMontoPropuesto());
        System.out.println("Listado:             " + transaccionBL.findAll().size() + " transacciones activas");

        CitaEntrega citaBruno = new CitaEntrega(LocalDateTime.now().plusDays(4).withNano(0), t2Recuperada, biblioteca);
        citaBL.insert(citaBruno);
        citaBL.delete(citaBruno.getIdCita());
        System.out.println("Cita eliminada:      cita " + citaBruno.getIdCita() + " activo="
                + citaBL.findById(citaBruno.getIdCita()).isActivo());

        // COMMIT: confirmar la cita modifica 4 tablas a la vez.
        transaccionBL.confirmarCita(idTransaccion);
        System.out.println("confirmarCita COMMIT:");
        System.out.println("  " + describir(transaccionBL.findById(idTransaccion)));
        System.out.println("  cita " + citaBL.findById(cita.getIdCita()).getEstado()
                + " | anuncio " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado()
                + " | competidora: " + describir(transaccionBL.findById(t2.getIdTransaccion())));

        // RF-11: se necesitan ambas confirmaciones.
        transaccionBL.confirmarEntrega(idTransaccion, comprador.getIdUsuario());
        System.out.println("Confirma comprador:  " + describir(transaccionBL.findById(idTransaccion)));
        transaccionBL.confirmarEntrega(idTransaccion, vendedor.getIdUsuario());
        System.out.println("Confirma vendedor:   " + describir(transaccionBL.findById(idTransaccion)));
        System.out.println("  cita " + citaBL.findById(cita.getIdCita()).getEstado()
                + " | anuncio " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado());

        rechazo("Eliminar una transaccion COMPLETADA", () -> transaccionBL.delete(idTransaccion));
        rechazo("Eliminar un anuncio con transacciones (RF-02)", () -> anuncioBL.delete(anuncio.getIdAnuncio()));
        transaccionBL.delete(t2.getIdTransaccion());
        System.out.println("Eliminada (logico):  transaccion " + t2.getIdTransaccion() + " activo="
                + transaccionBL.findById(t2.getIdTransaccion()).isActivo() + ", en listado="
                + contiene(transaccionBL.findAll(), Transaccion::getIdTransaccion, t2.getIdTransaccion()));

        probarRollbackDeTransaccion(vendedor, comprador, bruno, material, comedor);
        probarCalificacionEInsignia(vendedor, comprador, idTransaccion);
    }

    /**
     * ROLLBACK: mientras se negocia, moderacion deja el anuncio OBSERVADO.
     * confirmarCita ya actualizo la transaccion y la cita, pero al reservar el
     * anuncio detecta que ya no esta DISPONIBLE: lanza BLException y hace
     * rollback, por lo que la transaccion y la cita vuelven a su estado anterior.
     */
    private static void probarRollbackDeTransaccion(UsuarioPUCP vendedor, UsuarioPUCP comprador, UsuarioPUCP bruno,
                                                    MaterialAcademico material, PuntoEntrega punto)
            throws BLException {
        titulo("7.1 TRANSACCION: demostracion de ROLLBACK");
        Anuncio anuncio = new Anuncio("Calculadora grafica " + RUN, 120.00, "Incluye estuche.",
                CondicionMaterial.USADO, vendedor, material);
        anuncioBL.insert(anuncio);

        Transaccion transaccion = new Transaccion(anuncio, comprador);
        transaccionBL.insert(transaccion);
        CitaEntrega cita = new CitaEntrega(LocalDateTime.now().plusDays(5).withNano(0), transaccion, punto);
        citaBL.insert(cita);

        ReporteAnuncio reporte = new ReporteAnuncio("Posible informacion falsa " + RUN,
                MotivoReporteAnuncio.INFORMACION_FALSA, bruno, anuncio);
        reporteAnuncioBL.insert(reporte);
        System.out.println("Anuncio reportado:   " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado());

        int idTransaccion = transaccion.getIdTransaccion();
        rechazo("confirmarCita con el anuncio OBSERVADO", () -> transaccionBL.confirmarCita(idTransaccion));
        System.out.println("  Despues del rollback: " + describir(transaccionBL.findById(idTransaccion))
                + " | cita " + citaBL.findById(cita.getIdCita()).getEstado()
                + " -> ningun cambio parcial quedo guardado");

        reporteAnuncioBL.desestimar(reporte.getIdReporte(), ID_ADMINISTRADOR);
        System.out.println("Reporte desestimado: anuncio " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado());
        transaccionBL.confirmarCita(idTransaccion);
        System.out.println("Reintento COMMIT:    " + describir(transaccionBL.findById(idTransaccion))
                + " | anuncio " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado());
        transaccionBL.cancelar(idTransaccion);
        System.out.println("Cancelada (COMMIT):  " + describir(transaccionBL.findById(idTransaccion))
                + " | anuncio " + anuncioBL.findById(anuncio.getIdAnuncio()).getEstado()
                + " | cita " + citaBL.findById(cita.getIdCita()).getEstado());
    }

    // ================================================================
    // 8. CALIFICACION, REPUTACION e INSIGNIAS (RF-15, RF-18)
    // ================================================================
    private static void probarCalificacionEInsignia(UsuarioPUCP vendedor, UsuarioPUCP comprador, int idTransaccion)
            throws BLException {
        titulo("8. CALIFICACION, REPUTACION e INSIGNIA");
        Transaccion transaccion = transaccionBL.findById(idTransaccion);

        rechazo("Puntaje 6", () -> calificacionBL.insert(new Calificacion(6, "Excelente",
                TipoCalificacion.COMPRADOR_A_VENDEDOR, transaccion, comprador, vendedor)));
        calificacionBL.insert(new Calificacion(5, "Entrega puntual, libro impecable.",
                TipoCalificacion.COMPRADOR_A_VENDEDOR, transaccion, comprador, vendedor));
        rechazo("Calificar dos veces la misma transaccion", () -> calificacionBL.insert(new Calificacion(4, "Otra vez",
                TipoCalificacion.COMPRADOR_A_VENDEDOR, transaccion, comprador, vendedor)));

        UsuarioPUCP actualizado = usuarioBL.findById(vendedor.getIdUsuario());
        System.out.println("Reputacion vendedor: " + actualizado.getReputacion() + " (recalculada en la misma transaccion)"
                + " | nivel " + usuarioBL.obtenerNivelReputacion(vendedor.getIdUsuario()));
        System.out.println("Insignia 'Primera Venta' otorgada: "
                + insigniaUsuarioBL.otorgarSiCumple(vendedor.getIdUsuario(), ID_INSIGNIA_PRIMERA_VENTA)
                + " | segundo intento: "
                + insigniaUsuarioBL.otorgarSiCumple(vendedor.getIdUsuario(), ID_INSIGNIA_PRIMERA_VENTA));
    }

    // ================================================================
    // Utilidades de impresion
    // ================================================================
    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws BLException;
    }

    // Cada regla se prueba por separado: el rechazo no detiene el resto de pruebas.
    private static void rechazo(String caso, Operacion operacion) {
        try {
            operacion.ejecutar();
            System.out.println("  [FALLO] " + caso + ": NO se rechazo");
        } catch (BLException ex) {
            System.out.println("  Rechazado -> " + caso + ": " + ex.getMessage());
        }
    }

    private static UsuarioPUCP nuevoUsuario(String nombres, String apellido, Carrera carrera, String correo) {
        String codigo = Long.toString(++secuenciaCodigo);
        return new UsuarioPUCP(codigo, nombres, apellido, null,
                correo != null ? correo : "a" + codigo + "@pucp.edu.pe", "Clave_de_prueba_" + RUN, carrera);
    }

    private static <T> boolean contiene(List<T> lista, ToIntFunction<T> id, int buscado) {
        for (T elemento : lista) {
            if (id.applyAsInt(elemento) == buscado) {
                return true;
            }
        }
        return false;
    }

    private static void imprimirCarreras(MaterialAcademico material) {
        StringBuilder carreras = new StringBuilder();
        for (Carrera carrera : material.getCarreras()) {
            carreras.append(carreras.length() == 0 ? "" : ", ").append(carrera.getNombre());
        }
        StringBuilder facultades = new StringBuilder();
        for (Facultad facultad : material.getFacultades()) {
            facultades.append(facultades.length() == 0 ? "" : ", ").append(facultad.getNombre());
        }
        System.out.println("  Carreras (N:M):    " + carreras + " | facultades: " + facultades);
    }

    private static String describir(Transaccion transaccion) {
        return "transaccion " + transaccion.getIdTransaccion() + " [" + transaccion.getEstado() + "]"
                + " comprador " + transaccion.getComprador().getNombres()
                + " | confirmaciones C=" + transaccion.isConfirmacionComprador()
                + " V=" + transaccion.isConfirmacionVendedor()
                + (transaccion.getFechaFin() != null ? " | fin " + transaccion.getFechaFin() : "");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}

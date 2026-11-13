package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.rest.dto.TransaccionDTO;

/** Solo de salida: las transacciones nacen al aceptar una oferta y cambian con sus propias acciones. */
public final class TransaccionMapper {
    private TransaccionMapper() {
    }

    public static TransaccionDTO toDTO(Transaccion transaccion) {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setIdTransaccion(transaccion.getIdTransaccion());
        dto.setFechaInicio(Fechas.aTexto(transaccion.getFechaInicio()));
        dto.setFechaFin(Fechas.aTexto(transaccion.getFechaFin()));
        dto.setEstado(transaccion.getEstado() == null ? null : transaccion.getEstado().name());
        dto.setConfirmacionComprador(transaccion.isConfirmacionComprador());
        dto.setConfirmacionVendedor(transaccion.isConfirmacionVendedor());
        if (transaccion.getAnuncio() != null) {
            dto.setIdAnuncio(transaccion.getAnuncio().getIdAnuncio());
            dto.setTituloAnuncio(transaccion.getAnuncio().getTitulo());
            dto.setPrecioAnuncio(transaccion.getAnuncio().getPrecio());
            dto.setMontoAcordado(transaccion.getAnuncio().getPrecio());
            if (transaccion.getAnuncio().getVendedor() != null) {
                dto.setIdVendedor(transaccion.getAnuncio().getVendedor().getIdUsuario());
            }
        }
        if (transaccion.getComprador() != null) {
            dto.setIdComprador(transaccion.getComprador().getIdUsuario());
            dto.setNombreComprador(UsuarioMapper.nombre(transaccion.getComprador()));
        }
        // Si hubo oferta aceptada, el monto acordado es el de la oferta.
        if (transaccion.getOferta() != null && transaccion.getOferta().getMontoPropuesto() > 0) {
            dto.setMontoAcordado(transaccion.getOferta().getMontoPropuesto());
        }
        if (transaccion.getCitaEntrega() != null) {
            dto.setIdCita(transaccion.getCitaEntrega().getIdCita());
            dto.setFechaCita(Fechas.aTexto(transaccion.getCitaEntrega().getFechaHora()));
            dto.setEstadoCita(transaccion.getCitaEntrega().getEstado() == null ? null
                    : transaccion.getCitaEntrega().getEstado().name());
        }
        return dto;
    }

    public static List<TransaccionDTO> toDTOs(List<Transaccion> transacciones) {
        List<TransaccionDTO> dtos = new ArrayList<>();
        for (Transaccion transaccion : transacciones) {
            dtos.add(toDTO(transaccion));
        }
        return dtos;
    }

    public static Transaccion referencia(int idTransaccion) {
        Transaccion transaccion = new Transaccion();
        transaccion.setIdTransaccion(idTransaccion);
        return transaccion;
    }
}

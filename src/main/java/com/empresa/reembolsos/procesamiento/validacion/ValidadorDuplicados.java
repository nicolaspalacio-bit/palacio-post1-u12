package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador con violacion intencional de arquitectura para verificar el fallo del pipeline en CI.
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // Violacion intencional de la Regla 1 de ArchUnit:
    private RepositorioSolicitudesJpa repositorioProhibido;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        if (pendientes == null || solicitud.getFechaGasto() == null) {
            return ResultadoValidacion.ok();
        }
        boolean existeDuplicado = pendientes.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && p.getFechaGasto() != null
                        && p.getFechaGasto().getMonth() == solicitud.getFechaGasto().getMonth());

        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
                : ResultadoValidacion.ok();
    }
}

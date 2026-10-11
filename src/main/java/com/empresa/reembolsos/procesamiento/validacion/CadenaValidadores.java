package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensamblador y punto de entrada de la cadena de validadores.
 * Aplica el orden de encadenamiento decidido por eficiencia fail-fast:
 * 1. Documentos soporte (comprobacion booleana O(1)).
 * 2. Monto maximo (comparacion numerica O(1)).
 * 3. Duplicados (filtrado de coleccion en memoria O(N)).
 */
@Component
public class CadenaValidadores {

    private final ValidadorSolicitud primero;

    public CadenaValidadores(ValidadorDocumentosSoporte documentos,
                             ValidadorMontoMaximo monto,
                             ValidadorDuplicados duplicados) {
        documentos.siguiente(monto).siguiente(duplicados);
        this.primero = documentos;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        return primero.validar(solicitud, pendientes);
    }
}

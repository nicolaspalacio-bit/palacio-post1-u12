package com.empresa.reembolsos.aplicacion;

/**
 * Excepcion de aplicacion lanzada cuando una solicitud de reembolso
 * no supera alguna de las validaciones de negocio.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String message) {
        super(message);
    }
}

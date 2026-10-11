package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Contrato de observador para la recepcion de eventos de transicion de estado.
 */
public interface ObservadorCambioEstado {

    /**
     * Reacciona ante una transicion de estado ocurrida sobre una solicitud.
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}

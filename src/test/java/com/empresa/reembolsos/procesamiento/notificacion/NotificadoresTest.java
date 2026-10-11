package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificadoresTest {

    @Test
    @DisplayName("NotificadorEmpleado debe emitir mensaje con el cambio de estado")
    void notificadorEmpleadoDebeEmitirMensaje() {
        NotificadorEmpleado notificador = new NotificadorEmpleado();
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), new BigDecimal("100000"),
                LocalDate.now(), 1, true, EstadoSolicitud.APROBADA
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE);
        } finally {
            System.setOut(original);
        }

        String mensaje = out.toString();
        assertTrue(mensaje.contains("Notificando a EMP-001"));
        assertTrue(mensaje.contains("PENDIENTE a APROBADA"));
    }

    @Test
    @DisplayName("NotificadorContabilidad solo debe actuar cuando el estado es APROBADA")
    void notificadorContabilidadSoloActuaEnAprobada() {
        NotificadorContabilidad notificador = new NotificadorContabilidad();
        SolicitudReembolso solicitudRechazada = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), BigDecimal.ZERO,
                LocalDate.now(), 1, true, EstadoSolicitud.RECHAZADA
        );
        SolicitudReembolso solicitudAprobada = new SolicitudReembolso(
                2L, "EMP-002", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), new BigDecimal("100000"),
                LocalDate.now(), 1, true, EstadoSolicitud.APROBADA
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            notificador.onCambioEstado(solicitudRechazada, EstadoSolicitud.PENDIENTE);
            assertTrue(out.toString().isEmpty());

            notificador.onCambioEstado(solicitudAprobada, EstadoSolicitud.PENDIENTE);
            assertTrue(out.toString().contains("Contabilidad: programar pago de 100000 para EMP-002"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    @DisplayName("NotificadorAuditoria debe registrar log sin excepciones")
    void notificadorAuditoriaDebeRegistrarLog() {
        NotificadorAuditoria notificador = new NotificadorAuditoria();
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), new BigDecimal("100000"),
                LocalDate.now(), 1, true, EstadoSolicitud.APROBADA
        );

        assertDoesNotThrow(() -> notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE));
    }
}

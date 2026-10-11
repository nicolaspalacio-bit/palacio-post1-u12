package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorDocumentosSoporteTest {

    private ValidadorDocumentosSoporte validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorDocumentosSoporte();
    }

    @Test
    @DisplayName("Debe aprobar cuando la solicitud incluye documentos soporte")
    void debeAprobarConDocumentosSoporte() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), null,
                LocalDate.now(), 2, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, Collections.emptyList());

        assertTrue(resultado.valida());
        assertNull(resultado.motivoRechazo());
    }

    @Test
    @DisplayName("Debe rechazar con mensaje claro cuando faltan documentos soporte")
    void debeRechazarSinDocumentosSoporte() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), null,
                LocalDate.now(), 2, false, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }
}

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

class ValidadorMontoMaximoTest {

    private ValidadorMontoMaximo validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorMontoMaximo();
    }

    @Test
    @DisplayName("Debe aprobar cuando el monto no supera el tope de VIAJE (150000)")
    void debeAprobarMontoDentroDeTopeViaje() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("150000"), null,
                LocalDate.now(), 1, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, Collections.emptyList());

        assertTrue(resultado.valida());
    }

    @Test
    @DisplayName("Debe rechazar cuando el monto excede el tope de VIAJE")
    void debeRechazarMontoExcedidoViaje() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("150001"), null,
                LocalDate.now(), 1, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria VIAJE", resultado.motivoRechazo());
    }

    @Test
    @DisplayName("Debe validar topes para REPRESENTACION (2000000) y CAPACITACION (3000000)")
    void debeValidarOtrasCategorias() {
        SolicitudReembolso repValida = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("2000000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );
        SolicitudReembolso capExcedida = new SolicitudReembolso(
                3L, "EMP-001", CategoriaGasto.CAPACITACION,
                new BigDecimal("3500000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );

        assertTrue(validador.validarPropio(repValida, Collections.emptyList()).valida());
        ResultadoValidacion resCap = validador.validarPropio(capExcedida, Collections.emptyList());
        assertFalse(resCap.valida());
        assertEquals("Monto excede el tope de la categoria CAPACITACION", resCap.motivoRechazo());
    }
}

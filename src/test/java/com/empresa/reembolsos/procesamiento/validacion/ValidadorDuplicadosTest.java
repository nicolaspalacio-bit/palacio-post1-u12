package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorDuplicadosTest {

    private ValidadorDuplicados validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorDuplicados();
    }

    @Test
    @DisplayName("Debe aprobar cuando no existen solicitudes pendientes en el mismo periodo para la misma categoria")
    void debeAprobarSinDuplicados() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), null,
                LocalDate.of(2026, 10, 15), 1, true, EstadoSolicitud.PENDIENTE
        );

        SolicitudReembolso pendienteOtroMes = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("120000"), null,
                LocalDate.of(2026, 9, 10), 1, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, List.of(pendienteOtroMes));

        assertTrue(resultado.valida());
    }

    @Test
    @DisplayName("Debe rechazar cuando ya existe una solicitud pendiente de la misma categoria en el mismo mes")
    void debeRechazarConDuplicado() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), null,
                LocalDate.of(2026, 10, 15), 1, true, EstadoSolicitud.PENDIENTE
        );

        SolicitudReembolso pendienteMismoMes = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("120000"), null,
                LocalDate.of(2026, 10, 2), 1, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = validador.validarPropio(solicitud, List.of(pendienteMismoMes));

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo", resultado.motivoRechazo());
    }
}

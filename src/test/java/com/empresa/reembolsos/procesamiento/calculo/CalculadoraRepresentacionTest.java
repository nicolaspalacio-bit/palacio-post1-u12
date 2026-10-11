package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraRepresentacionTest {

    private CalculadoraRepresentacion calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraRepresentacion();
    }

    @Test
    @DisplayName("Debe pertenecer a la categoria REPRESENTACION")
    void debePertenecerACategoriaRepresentacion() {
        assertEquals(CategoriaGasto.REPRESENTACION, calculadora.getCategoria());
    }

    @Test
    @DisplayName("Debe reembolsar monto completo hasta el tope mensual de 2000000")
    void debeCalcularTopeMensual() {
        SolicitudReembolso solMenor = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("1200000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );
        SolicitudReembolso solMayor = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("2500000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );

        assertEquals(new BigDecimal("1200000"), calculadora.calcular(solMenor));
        assertEquals(new BigDecimal("2000000"), calculadora.calcular(solMayor));
    }
}

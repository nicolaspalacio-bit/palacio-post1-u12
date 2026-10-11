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

class CalculadoraCapacitacionTest {

    private CalculadoraCapacitacion calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraCapacitacion();
    }

    @Test
    @DisplayName("Debe pertenecer a la categoria CAPACITACION")
    void debePertenecerACategoriaCapacitacion() {
        assertEquals(CategoriaGasto.CAPACITACION, calculadora.getCategoria());
    }

    @Test
    @DisplayName("Debe reembolsar 80% del monto hasta el tope anual de 3000000")
    void debeCalcularOchentaPorCientoConTope() {
        // 2000000 * 0.8 = 1600000 (menor a 3000000)
        SolicitudReembolso solMenor = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.CAPACITACION,
                new BigDecimal("2000000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );
        // 5000000 * 0.8 = 4000000 (tope = 3000000)
        SolicitudReembolso solMayor = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.CAPACITACION,
                new BigDecimal("5000000"), null,
                LocalDate.now(), 0, true, EstadoSolicitud.PENDIENTE
        );

        assertEquals(0, new BigDecimal("1600000").compareTo(calculadora.calcular(solMenor)));
        assertEquals(0, new BigDecimal("3000000").compareTo(calculadora.calcular(solMayor)));
    }
}

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

class CalculadoraViajeTest {

    private CalculadoraViaje calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraViaje();
    }

    @Test
    @DisplayName("Debe pertenecer a la categoria VIAJE")
    void debePertenecerACategoriaViaje() {
        assertEquals(CategoriaGasto.VIAJE, calculadora.getCategoria());
    }

    @Test
    @DisplayName("Debe calcular monto aprobado respetando el tope diario por dias de viaje")
    void debeCalcularTopeDiario() {
        // 3 dias de viaje -> tope = 3 * 150000 = 450000
        SolicitudReembolso solMenorTope = new SolicitudReembolso(
                1L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("400000"), null,
                LocalDate.now(), 3, true, EstadoSolicitud.PENDIENTE
        );
        SolicitudReembolso solMayorTope = new SolicitudReembolso(
                2L, "EMP-001", CategoriaGasto.VIAJE,
                new BigDecimal("500000"), null,
                LocalDate.now(), 3, true, EstadoSolicitud.PENDIENTE
        );

        BigDecimal monto1 = calculadora.calcular(solMenorTope);
        BigDecimal monto2 = calculadora.calcular(solMayorTope);

        assertEquals(new BigDecimal("400000"), monto1);
        assertEquals(new BigDecimal("450000"), monto2);
    }
}

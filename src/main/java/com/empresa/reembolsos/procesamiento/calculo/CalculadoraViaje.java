package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estrategia de calculo para viaticos de viaje.
 * Aplica un tope diario de 150000 multiplicado por los dias de viaje.
 */
@Component
public class CalculadoraViaje implements CalculadoraReembolso {

    private static final BigDecimal TOPE_DIARIO = new BigDecimal("150000");

    @Override
    public CategoriaGasto getCategoria() {
        return CategoriaGasto.VIAJE;
    }

    @Override
    public BigDecimal calcular(SolicitudReembolso solicitud) {
        var topeTotal = TOPE_DIARIO.multiply(BigDecimal.valueOf(solicitud.getDiasViaje()));
        return solicitud.getMontoSolicitado().min(topeTotal);
    }
}

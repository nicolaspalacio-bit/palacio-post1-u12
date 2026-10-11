package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estrategia de calculo para gastos de representacion.
 * Reembolsa el monto completo hasta un tope mensual de 2000000.
 */
@Component
public class CalculadoraRepresentacion implements CalculadoraReembolso {

    private static final BigDecimal TOPE_MENSUAL = new BigDecimal("2000000");

    @Override
    public CategoriaGasto getCategoria() {
        return CategoriaGasto.REPRESENTACION;
    }

    @Override
    public BigDecimal calcular(SolicitudReembolso solicitud) {
        return solicitud.getMontoSolicitado().min(TOPE_MENSUAL);
    }
}

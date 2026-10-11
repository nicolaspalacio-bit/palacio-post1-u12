package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Interfaz de estrategia para el calculo del monto aprobado de una solicitud.
 */
public interface CalculadoraReembolso {

    /**
     * Categoria de gasto que atiende la estrategia concreta.
     */
    CategoriaGasto getCategoria();

    /**
     * Calcula el monto aprobado conforme a las reglas y topes de la categoria.
     */
    BigDecimal calcular(SolicitudReembolso solicitud);
}

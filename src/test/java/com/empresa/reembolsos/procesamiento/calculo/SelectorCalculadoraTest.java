package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectorCalculadoraTest {

    private SelectorCalculadora selector;

    @BeforeEach
    void setUp() {
        selector = new SelectorCalculadora(List.of(
                new CalculadoraViaje(),
                new CalculadoraRepresentacion(),
                new CalculadoraCapacitacion()
        ));
    }

    @Test
    @DisplayName("Debe devolver la estrategia correspondiente a cada categoria registrada")
    void debeDevolverEstrategiaCorrecta() {
        assertInstanceOf(CalculadoraViaje.class, selector.obtener(CategoriaGasto.VIAJE));
        assertInstanceOf(CalculadoraRepresentacion.class, selector.obtener(CategoriaGasto.REPRESENTACION));
        assertInstanceOf(CalculadoraCapacitacion.class, selector.obtener(CategoriaGasto.CAPACITACION));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException para una categoria no registrada")
    void debeLanzarExcepcionParaCategoriaNoRegistrada() {
        SelectorCalculadora selectorIncompleto = new SelectorCalculadora(List.of(new CalculadoraViaje()));
        assertThrows(IllegalArgumentException.class, () -> selectorIncompleto.obtener(CategoriaGasto.CAPACITACION));
    }
}

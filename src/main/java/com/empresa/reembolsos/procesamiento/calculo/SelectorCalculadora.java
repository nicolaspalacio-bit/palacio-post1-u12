package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro y selector polimorfico de estrategias de calculo inyectadas por Spring.
 * Cumple con el Principio Abierto/Cerrado (OCP) al permitir anadir nuevas categorias
 * sin modificar esta clase ni las demas calculadoras.
 */
@Component
public class SelectorCalculadora {

    private final Map<CategoriaGasto, CalculadoraReembolso> calculadoras;

    public SelectorCalculadora(List<CalculadoraReembolso> lista) {
        this.calculadoras = lista.stream()
                .collect(Collectors.toMap(CalculadoraReembolso::getCategoria, Function.identity()));
    }

    public CalculadoraReembolso obtener(CategoriaGasto categoria) {
        return Optional.ofNullable(calculadoras.get(categoria))
                .orElseThrow(() -> new IllegalArgumentException("Categoria no soportada: " + categoria));
    }
}

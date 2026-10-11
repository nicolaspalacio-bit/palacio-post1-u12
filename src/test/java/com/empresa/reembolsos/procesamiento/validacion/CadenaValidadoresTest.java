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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CadenaValidadoresTest {

    private CadenaValidadores cadena;

    @BeforeEach
    void setUp() {
        ValidadorDocumentosSoporte docs = new ValidadorDocumentosSoporte();
        ValidadorMontoMaximo monto = new ValidadorMontoMaximo();
        ValidadorDuplicados dups = new ValidadorDuplicados();
        cadena = new CadenaValidadores(docs, monto, dups);
    }

    @Test
    @DisplayName("Debe rechazar en el primer eslabon si faltan documentos soporte (fail-fast)")
    void debeRechazarEnPrimerEslabon() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-1", CategoriaGasto.VIAJE,
                new BigDecimal("999999999"), null, // monto excesivo, pero debe fallar por docs primero
                LocalDate.now(), 1, false, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = cadena.validar(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }

    @Test
    @DisplayName("Debe rechazar en el segundo eslabon si excede monto maximo")
    void debeRechazarEnSegundoEslabon() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-1", CategoriaGasto.VIAJE,
                new BigDecimal("200000"), null, // excede 150000 de viaje
                LocalDate.now(), 1, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = cadena.validar(solicitud, Collections.emptyList());

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria VIAJE", resultado.motivoRechazo());
    }

    @Test
    @DisplayName("Debe rechazar en el tercer eslabon si existe duplicado en el periodo")
    void debeRechazarEnTercerEslabon() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-1", CategoriaGasto.REPRESENTACION,
                new BigDecimal("100000"), null,
                LocalDate.of(2026, 4, 15), 0, true, EstadoSolicitud.PENDIENTE
        );
        SolicitudReembolso pendiente = new SolicitudReembolso(
                2L, "EMP-1", CategoriaGasto.REPRESENTACION,
                new BigDecimal("200000"), null,
                LocalDate.of(2026, 4, 2), 0, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = cadena.validar(solicitud, List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo", resultado.motivoRechazo());
    }

    @Test
    @DisplayName("Debe aprobar si supera todos los eslabones")
    void debeAprobarSiSuperaTodos() {
        SolicitudReembolso solicitud = new SolicitudReembolso(
                1L, "EMP-1", CategoriaGasto.REPRESENTACION,
                new BigDecimal("500000"), null,
                LocalDate.of(2026, 4, 15), 0, true, EstadoSolicitud.PENDIENTE
        );

        ResultadoValidacion resultado = cadena.validar(solicitud, Collections.emptyList());

        assertTrue(resultado.valida());
        assertNull(resultado.motivoRechazo());
    }
}

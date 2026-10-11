package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlujoCompletoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe crear solicitud de VIAJE, calcular monto aprobado y transicionar a APROBADA")
    void debeProcesarSolicitudViajeCompleta() throws Exception {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("EMP-101");
        solicitud.setCategoria(CategoriaGasto.VIAJE);
        solicitud.setMontoSolicitado(new BigDecimal("120000"));
        solicitud.setFechaGasto(LocalDate.of(2026, 5, 10));
        solicitud.setDiasViaje(2);
        solicitud.setDocumentosSoporte(true);

        String json = objectMapper.writeValueAsString(solicitud);

        String response = mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.montoAprobado", is(120000)))
                .andReturn().getResponse().getContentAsString();

        SolicitudReembolso creada = objectMapper.readValue(response, SolicitudReembolso.class);

        // Transicion de estado a APROBADA
        mockMvc.perform(put("/api/solicitudes/" + creada.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APROBADA\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("APROBADA")));
    }

    @Test
    @DisplayName("Debe crear solicitud de REPRESENTACION y calcular tope mensual")
    void debeProcesarSolicitudRepresentacion() throws Exception {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("EMP-102");
        solicitud.setCategoria(CategoriaGasto.REPRESENTACION);
        solicitud.setMontoSolicitado(new BigDecimal("1800000"));
        solicitud.setFechaGasto(LocalDate.of(2026, 6, 12));
        solicitud.setDiasViaje(0);
        solicitud.setDocumentosSoporte(true);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.montoAprobado", is(1800000)));
    }

    @Test
    @DisplayName("Debe crear solicitud de CAPACITACION y aplicar formula de 80%")
    void debeProcesarSolicitudCapacitacion() throws Exception {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("EMP-103");
        solicitud.setCategoria(CategoriaGasto.CAPACITACION);
        solicitud.setMontoSolicitado(new BigDecimal("2500000"));
        solicitud.setFechaGasto(LocalDate.of(2026, 7, 20));
        solicitud.setDiasViaje(0);
        solicitud.setDocumentosSoporte(true);

        // 2500000 * 0.8 = 2000000.0
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.montoAprobado", is(2000000.0)));
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 400 cuando faltan documentos soporte")
    void debeRechazarSinDocumentosSoporte() throws Exception {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("EMP-104");
        solicitud.setCategoria(CategoriaGasto.VIAJE);
        solicitud.setMontoSolicitado(new BigDecimal("50000"));
        solicitud.setFechaGasto(LocalDate.of(2026, 8, 1));
        solicitud.setDiasViaje(1);
        solicitud.setDocumentosSoporte(false);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", is("Faltan documentos soporte")));
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 400 cuando ya existe una solicitud pendiente duplicada en el periodo")
    void debeRechazarDuplicadoEnPeriodo() throws Exception {
        SolicitudReembolso primera = new SolicitudReembolso();
        primera.setEmpleadoId("EMP-DUPLICADO");
        primera.setCategoria(CategoriaGasto.REPRESENTACION);
        primera.setMontoSolicitado(new BigDecimal("500000"));
        primera.setFechaGasto(LocalDate.of(2026, 9, 5));
        primera.setDiasViaje(0);
        primera.setDocumentosSoporte(true);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primera)))
                .andExpect(status().isOk());

        SolicitudReembolso duplicada = new SolicitudReembolso();
        duplicada.setEmpleadoId("EMP-DUPLICADO");
        duplicada.setCategoria(CategoriaGasto.REPRESENTACION);
        duplicada.setMontoSolicitado(new BigDecimal("600000"));
        duplicada.setFechaGasto(LocalDate.of(2026, 9, 20));
        duplicada.setDiasViaje(0);
        duplicada.setDocumentosSoporte(true);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", is("Ya existe una solicitud pendiente de la misma categoria en el periodo")));
    }
}

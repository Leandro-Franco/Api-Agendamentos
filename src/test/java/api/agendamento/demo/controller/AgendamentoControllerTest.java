package api.agendamento.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.exception.ConflitoDeAgendamentoException;
import api.agendamento.demo.service.AgendamentoService;

@WebMvcTest(AgendamentoController.class)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    private static final String CORPO_VALIDO = """
            {
              "titulo": "Consulta de rotina",
              "descricao": "Primeira avaliacao",
              "dataInicio": "2026-08-05T14:00:00",
              "dataFim": "2026-08-05T15:00:00",
              "idUsuario": 1
            }
            """;

    @Test
    void criar_deveResponder409_quandoServicoAcusaConflito() throws Exception {
        given(agendamentoService.criar(any(AgendamentoCreateRequest.class)))
                .willThrow(new ConflitoDeAgendamentoException(
                        "Ja existe um agendamento para o usuario nesse intervalo de datas."));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_AGENDAMENTO"))
                .andExpect(jsonPath("$.title").value("Conflito de agendamento"));
    }

        @Test
    void criar_deveResponder400_quandoTituloEstaEmBranco() throws Exception {
        given(agendamentoService.criar(any(AgendamentoCreateRequest.class)))
                .willThrow(new ConflitoDeAgendamentoException(
                        "O titulo do agendamento nao pode estar em branco."));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("Consulta de rotina", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.title").value("Requisicao invalida"));
    }
}
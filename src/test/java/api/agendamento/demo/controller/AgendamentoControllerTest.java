package api.agendamento.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoPageResponse;
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
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("Consulta de rotina", "")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.title").value("Requisicao invalida"))
                .andExpect(jsonPath("$.campos.titulo").exists());

        verify(agendamentoService, never()).criar(any());
    }

    @Test
    void criar_deveResponder409_quandoViolaIntegridadeNoBanco() throws Exception {
        given(agendamentoService.criar(any(AgendamentoCreateRequest.class)))
                .willThrow(new DataIntegrityViolationException("violacao de integridade"));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VIOLACAO_INTEGRIDADE"))
                .andExpect(jsonPath("$.title").value("Violacao de integridade"));
    }

    @Test
    void listar_deveResponder200_comAPaginaVindaDoServico() throws Exception {
        given(agendamentoService.listar(any(), any(), any(), any()))
                .willReturn(new AgendamentoPageResponse(List.of(), 0, 20, 0L, true));

        mockMvc.perform(get("/agendamentos").param("idUsuario", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.ultimaPagina").value(true));
    }

    @Test
    void listar_deveResponder400_quandoIdUsuarioNaoEInformado() throws Exception {
        mockMvc.perform(get("/agendamentos"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

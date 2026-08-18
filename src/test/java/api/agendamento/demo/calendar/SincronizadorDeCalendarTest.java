package api.agendamento.demo.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.agendamento.demo.event.AgendamentoRetirado;
import api.agendamento.demo.event.AgendamentoSalvo;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.model.StatusAgendamento;
import api.agendamento.demo.repository.AgendamentoRepository;

class SincronizadorDeCalendarTest {

    private CalendarPort calendar;
    private AgendamentoRepository agendamentoRepository;
    private SincronizadorDeCalendar sincronizador;

    @BeforeEach
    void montar() {
        calendar = mock(CalendarPort.class);
        agendamentoRepository = mock(AgendamentoRepository.class);
        sincronizador = new SincronizadorDeCalendar(calendar, agendamentoRepository);
    }

    private Agendamento agendamentoCom(String googleEventId) {
        return Agendamento.builder()
                .idAgendamento(1L)
                .idUsuario(1L)
                .titulo("Reunião com o time")
                .dataInicio(LocalDateTime.of(2026, 8, 12, 10, 0))
                .dataFim(LocalDateTime.of(2026, 8, 12, 12, 0))
                .status(StatusAgendamento.AGENDADO)
                .googleEventId(googleEventId)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    void espelhar_deveCriarEGuardarAChave_quandoAindaNaoHaEvento() {
        Agendamento agendamento = agendamentoCom(null);
        given(agendamentoRepository.findById(1L)).willReturn(Optional.of(agendamento));
        given(calendar.criar(agendamento)).willReturn("uh873e6cpufp1m7jmgp3aer32c");

        sincronizador.espelhar(new AgendamentoSalvo(1L));

        assertThat(agendamento.getGoogleEventId()).isEqualTo("uh873e6cpufp1m7jmgp3aer32c");
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void espelhar_deveAtualizar_quandoJaExisteEvento() {
        Agendamento agendamento = agendamentoCom("uh873e6cpufp1m7jmgp3aer32c");
        given(agendamentoRepository.findById(1L)).willReturn(Optional.of(agendamento));

        sincronizador.espelhar(new AgendamentoSalvo(1L));

        verify(calendar).atualizar("uh873e6cpufp1m7jmgp3aer32c", agendamento);
        verify(calendar, never()).criar(any());
    }

    // Quando isso roda, o agendamento ja esta commitado. Deixar a excecao subir nao
    // desfaz nada e ainda estouraria a resposta ao cliente por causa do espelho.
    @Test
    void espelhar_naoDevePropagarFalhaDoCalendario() {
        Agendamento agendamento = agendamentoCom(null);
        given(agendamentoRepository.findById(1L)).willReturn(Optional.of(agendamento));
        given(calendar.criar(agendamento)).willThrow(new RuntimeException("Google fora do ar"));

        assertThatCode(() -> sincronizador.espelhar(new AgendamentoSalvo(1L)))
                .doesNotThrowAnyException();

        // Continua nulo de proposito: e por esse nulo que a reconciliacao acha o
        // que ficou para tras.
        assertThat(agendamento.getGoogleEventId()).isNull();
    }

    // Caso do CalendarDesligado: sem calendario configurado nao ha chave para gravar.
    @Test
    void espelhar_naoDeveSalvar_quandoOCalendarioNaoDevolveChave() {
        Agendamento agendamento = agendamentoCom(null);
        given(agendamentoRepository.findById(1L)).willReturn(Optional.of(agendamento));
        given(calendar.criar(agendamento)).willReturn(null);

        sincronizador.espelhar(new AgendamentoSalvo(1L));

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void retirar_deveRemoverOEvento_quandoHaChave() {
        sincronizador.retirar(new AgendamentoRetirado("uh873e6cpufp1m7jmgp3aer32c"));

        verify(calendar).remover("uh873e6cpufp1m7jmgp3aer32c");
    }

    @Test
    void retirar_naoDeveChamarOCalendario_quandoNaoHaChave() {
        sincronizador.retirar(new AgendamentoRetirado(null));

        verify(calendar, never()).remover(any());
    }
}

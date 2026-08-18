package api.agendamento.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.model.StatusAgendamento;

class AgendamentoMapperTest {

    private AgendamentoCreateRequest novaRequest() {

        return new AgendamentoCreateRequest(
            "Reunião de equipe",
            "Discutir metas",
            LocalDateTime.of(2027, 6, 1, 10, 0),
            LocalDateTime.of(2027, 6, 1, 11,0),
            42L
        );
    }

    @Test
    void toEntity_devePreencherIdUsuario() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());
        assertThat(entidade.getIdUsuario()).isEqualTo(42L);
    }

        @Test
    void toEntity_deveNascerComStatusAgendado() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());
        assertThat(entidade.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
    }

    // O cliente e um agente de IA: campo que ele nao quer alterar chega como
    // string vazia, nao como ausente. Antes da correcao isso ia para o parse.
    @Test
    void updateEntity_deveManterDatas_quandoAsStringsVemVazias() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());
        LocalDateTime inicioOriginal = entidade.getDataInicio();
        LocalDateTime fimOriginal = entidade.getDataFim();

        AgendamentoMapper.updateEntity(entidade,
                new AgendamentoUpdateRequest(null, null, "", "   ", null, null));

        assertThat(entidade.getDataInicio()).isEqualTo(inicioOriginal);
        assertThat(entidade.getDataFim()).isEqualTo(fimOriginal);
    }

    @Test
    void updateEntity_deveManterTitulo_quandoAStringVemVazia() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());

        AgendamentoMapper.updateEntity(entidade,
                new AgendamentoUpdateRequest("", null, null, null, null, null));

        assertThat(entidade.getTitulo()).isEqualTo("Reunião de equipe");
    }

    @Test
    void updateEntity_deveTrocarAData_quandoAStringVemPreenchida() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());

        AgendamentoMapper.updateEntity(entidade,
                new AgendamentoUpdateRequest(null, null, "2027-06-01T15:00:00", null, null, null));

        assertThat(entidade.getDataInicio()).isEqualTo(LocalDateTime.of(2027, 6, 1, 15, 0));
    }

    @Test
    void updateEntity_deveGravarOIdDoEventoNoCalendar() {
        Agendamento entidade = AgendamentoMapper.toEntity(novaRequest());

        AgendamentoMapper.updateEntity(entidade,
                new AgendamentoUpdateRequest(null, null, null, null, null, "uh873e6cpufp1m7jmgp3aer32c"));

        assertThat(entidade.getGoogleEventId()).isEqualTo("uh873e6cpufp1m7jmgp3aer32c");
    }
}

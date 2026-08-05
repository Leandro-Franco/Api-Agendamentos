package api.agendamento.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
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

}

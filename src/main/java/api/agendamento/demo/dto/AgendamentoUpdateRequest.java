package api.agendamento.demo.dto;

import api.agendamento.demo.model.StatusAgendamento;
import jakarta.validation.constraints.Size;

public record AgendamentoUpdateRequest(
    @Size(max = 60) String titulo,
    @Size(max = 500) String descricao,
    String dataInicio,
    String dataFim,
    StatusAgendamento status,
    String googleEventId
) {
}

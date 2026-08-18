package api.agendamento.demo.dto;

import api.agendamento.demo.model.StatusAgendamento;

public record AgendamentoResponse(
        Long idAgendamento,
        String titulo,
        String descricao,
        String dataInicio,
        String dataFim,
        StatusAgendamento status,
        Long idUsuario,
        String criadoEm,
        String atualizadoEm,
        String googleEventId
) {
}

package api.agendamento.demo.mapper;

import java.time.LocalDateTime;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoResponse;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.model.StatusAgendamento;

public class AgendamentoMapper {
    
    public static Agendamento toEntity(AgendamentoCreateRequest request) {
        return Agendamento.builder()
                .idUsuario(request.idUsuario())
                .titulo(request.titulo())
                .descricao(request.descricao())
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
        .build();
    }

    public static AgendamentoResponse toResponse(Agendamento info) {
        return new AgendamentoResponse(
                info.getIdAgendamento(),
                info.getTitulo(),
                info.getDescricao(),
                info.getDataInicio().toString(),
                info.getDataFim().toString(),
                info.getStatus(),
                info.getIdUsuario(),
                info.getCriadoEm().toString(),
                info.getAtualizadoEm().toString()
        );
    }

    // Unica autoridade sobre como ler uma data vinda do cliente. Campo ausente e
    // campo em branco significam a mesma coisa: nao alterar. Sem isso, uma string
    // vazia entra no parse e o erro sobe como 500.
    public static LocalDateTime parseData(String valor) {
        return ausente(valor) ? null : LocalDateTime.parse(valor);
    }

    private static boolean ausente(String valor) {
        return valor == null || valor.isBlank();
    }

    public static void updateEntity(Agendamento agendamento, AgendamentoUpdateRequest request) {
        if (!ausente(request.titulo())) {
            agendamento.setTitulo(request.titulo());
        }
        if (!ausente(request.descricao())) {
            agendamento.setDescricao(request.descricao());
        }

        LocalDateTime dataInicio = parseData(request.dataInicio());
        if (dataInicio != null) {
            agendamento.setDataInicio(dataInicio);
        }

        LocalDateTime dataFim = parseData(request.dataFim());
        if (dataFim != null) {
            agendamento.setDataFim(dataFim);
        }

        if (request.status() != null) {
            agendamento.setStatus(request.status());
        }
        agendamento.setAtualizadoEm(LocalDateTime.now());
    }
}

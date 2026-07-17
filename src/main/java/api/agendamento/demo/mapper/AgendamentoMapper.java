package api.agendamento.demo.mapper;

import java.time.LocalDateTime;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.model.StatusAgendamento;

public class AgendamentoMapper {
    
    public static Agendamento toEntity(AgendamentoCreateRequest request) {
        return Agendamento.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
        .build();
    }

    public static Agendamento toResponse(Agendamento info) {
        return Agendamento.builder()
                .idAgendamento(info.getIdAgendamento())
                .titulo(info.getTitulo())
                .descricao(info.getDescricao())
                .dataInicio(info.getDataInicio())
                .dataFim(info.getDataFim())
                .status(info.getStatus())
                .idUsuario(info.getIdUsuario())
                .criadoEm(info.getCriadoEm())
                .atualizadoEm(info.getAtualizadoEm())
        .build();
    }

    public static void updateEntity(Agendamento agendamento, AgendamentoUpdateRequest request) {
        if (request.titulo() != null) {
            agendamento.setTitulo(request.titulo());
        }
        if (request.descricao() != null) {
            agendamento.setDescricao(request.descricao());
        }
        if (request.dataInicio() != null) {
            agendamento.setDataInicio(LocalDateTime.parse(request.dataInicio()));
        }
        if (request.dataFim() != null) {
            agendamento.setDataFim(LocalDateTime.parse(request.dataFim()));
        }
        if (request.status() != null) {
            agendamento.setStatus(request.status());
        }
        agendamento.setAtualizadoEm(LocalDateTime.now());
    }
}
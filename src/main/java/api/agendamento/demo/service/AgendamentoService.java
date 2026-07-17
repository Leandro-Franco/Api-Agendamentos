package api.agendamento.demo.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoResponse;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.mapper.AgendamentoMapper;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.repository.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;


    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }


    @Transactional
    public AgendamentoResponse criar(@Valid AgendamentoCreateRequest request) {

        validateIntervaloDeDatas(request.dataInicio(), request.dataFim());
        validateDataIsNull(request.dataInicio(), request.dataFim());
        checkConflitoDeAgendamento(request.idUsuario(), request.dataInicio(), request.dataFim(), null);

        Agendamento agendamento = AgendamentoMapper.toEntity(request);
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }

    @Transactional
    public AgendamentoResponse atualizar(Long idAgendamento, @Valid AgendamentoUpdateRequest request) {

        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        if (request.dataInicio() != null && request.dataFim() != null) {

            LocalDateTime dataInicio = request.dataInicio() != null
                ? LocalDateTime.parse(request.dataInicio())
               : agendamento.getDataInicio();

            LocalDateTime dataFim = request.dataFim() != null
               ? LocalDateTime.parse(request.dataFim())
                : agendamento.getDataFim();

            validateIntervaloDeDatas(dataInicio, dataFim);
            checkConflitoDeAgendamento(agendamento.getIdUsuario(), dataInicio, dataFim, idAgendamento);
        }

        AgendamentoMapper.updateEntity(agendamento, request);
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }


    public AgendamentoResponse procurar(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        return AgendamentoMapper.toResponse(agendamento);
    }


    @Transactional
    public void deletar(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        agendamentoRepository.delete(agendamento);
    }

    @Transactional
    public AgendamentoResponse cancelar(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        agendamento.setStatus(api.agendamento.demo.model.StatusAgendamento.CANCELADO);
        agendamento.setAtualizadoEm(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }

    @Transactional
    public AgendamentoResponse concluir(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        agendamento.setStatus(api.agendamento.demo.model.StatusAgendamento.CONCLUIDO);
        agendamento.setAtualizadoEm(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }

    @Transactional
    public AgendamentoResponse confirmar(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        agendamento.setStatus(api.agendamento.demo.model.StatusAgendamento.CONFIRMADO);
        agendamento.setAtualizadoEm(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }


// Validações de intervalo de datas, datas nulas e conflito de agendamento
    private void validateIntervaloDeDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior à data de início.");
        }
    }


    private void validateDataIsNull(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim não podem ser nulas.");
        }
    }


    private void checkConflitoDeAgendamento(Long idUsuario, LocalDateTime dataInicio, LocalDateTime dataFim, Long idAgendamento) {
        boolean hasConflict = agendamentoRepository.existsConflito(idAgendamento != null ? idAgendamento : -1L, dataFim, dataInicio, idUsuario);
        if (hasConflict) {
            throw new IllegalArgumentException("Conflito de agendamento: já existe um agendamento para o usuário nesse intervalo de datas.");
        }
    }
}

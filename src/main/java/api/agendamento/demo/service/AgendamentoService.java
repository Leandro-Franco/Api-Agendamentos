package api.agendamento.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoPageResponse;
import api.agendamento.demo.dto.AgendamentoResponse;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.mapper.AgendamentoMapper;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.repository.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import api.agendamento.demo.exception.ConflitoDeAgendamentoException;
import api.agendamento.demo.exception.IntervaloInvalidoException;
@Service
@Validated
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;


    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }


    @Transactional
    public AgendamentoResponse criar(@Valid AgendamentoCreateRequest request) {

        validateDataIsNull(request.dataInicio(), request.dataFim());
        validateIntervaloDeDatas(request.dataInicio(), request.dataFim());
        checkConflitoDeAgendamento(request.idUsuario(), request.dataInicio(), request.dataFim(), null);

        Agendamento agendamento = AgendamentoMapper.toEntity(request);
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }

    @Transactional
    public AgendamentoResponse atualizar(Long idAgendamento, @Valid AgendamentoUpdateRequest request) {

        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        LocalDateTime novaDataInicio = AgendamentoMapper.parseData(request.dataInicio());
        LocalDateTime novaDataFim = AgendamentoMapper.parseData(request.dataFim());

        // Basta UMA das datas mudar para o intervalo mudar. Exigir as duas deixava
        // passar sem checagem quem alterasse so o inicio ou so o fim.
        if (novaDataInicio != null || novaDataFim != null) {

            LocalDateTime dataInicio = novaDataInicio != null
                ? novaDataInicio
                : agendamento.getDataInicio();

            LocalDateTime dataFim = novaDataFim != null
                ? novaDataFim
                : agendamento.getDataFim();

            validateIntervaloDeDatas(dataInicio, dataFim);
            checkConflitoDeAgendamento(agendamento.getIdUsuario(), dataInicio, dataFim, idAgendamento);
        }

        AgendamentoMapper.updateEntity(agendamento, request);
        agendamentoRepository.save(agendamento);
        return AgendamentoMapper.toResponse(agendamento);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse procurar(Long idAgendamento) {
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + idAgendamento));

        return AgendamentoMapper.toResponse(agendamento);
    }

    // Lista os agendamentos ativos de um usuario dentro de uma janela opcional.
    // Serve tambem para checar disponibilidade: janela sem resultado é horario livre.
    @Transactional(readOnly = true)
    public AgendamentoPageResponse listar(Long idUsuario, LocalDateTime de, LocalDateTime ate, Pageable pageable) {

        if (de != null && ate != null) {
            validateIntervaloDeDatas(de, ate);
        }

        Page<Agendamento> pagina = agendamentoRepository.listar(idUsuario, de, ate, pageable);

        return new AgendamentoPageResponse(
                pagina.getContent().stream().map(AgendamentoMapper::toResponse).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.isLast()
        );
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
            throw new IntervaloInvalidoException("A data de fim não pode ser anterior à data de início.");
        }
    }


    private void validateDataIsNull(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IntervaloInvalidoException("As datas de início e fim não podem ser nulas.");
        }
    }


    private void checkConflitoDeAgendamento(Long idUsuario, LocalDateTime dataInicio, LocalDateTime dataFim, Long idAgendamento) {
        boolean hasConflict = agendamentoRepository.existsConflito(idUsuario, dataInicio, dataFim, idAgendamento);
        if (hasConflict) {
            throw new ConflitoDeAgendamentoException("Já existe um agendamento para o usuário nesse intervalo de datas.");
        }
    }
}

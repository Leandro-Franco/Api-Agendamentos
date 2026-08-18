package api.agendamento.demo.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
import api.agendamento.demo.dto.AgendamentoPageResponse;
import api.agendamento.demo.dto.AgendamentoResponse;
import api.agendamento.demo.dto.AgendamentoUpdateRequest;
import api.agendamento.demo.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public AgendamentoResponse criar(@Valid @RequestBody AgendamentoCreateRequest request) {
        return agendamentoService.criar(request);
    }

    // idUsuario e obrigatorio: a listagem nasce escopada ao dono, em vez de
    // devolver tudo e deixar o cliente filtrar.
    @GetMapping
    public AgendamentoPageResponse listar(
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam(value = "de", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(value = "ate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
            @PageableDefault(size = 20, sort = "dataInicio") Pageable pageable) {
        return agendamentoService.listar(idUsuario, de, ate, pageable);
    }

    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@PathVariable("id") Long idAgendamento, @Valid @RequestBody AgendamentoUpdateRequest request) {
        return agendamentoService.atualizar(idAgendamento, request);
    }

    @PutMapping("/delete/{id}")
    public void deletar(@PathVariable("id") Long idAgendamento) {
        agendamentoService.deletar(idAgendamento);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse procurar(@PathVariable("id") Long idAgendamento) {
        return agendamentoService.procurar(idAgendamento);
    }

    @PutMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@PathVariable("id") Long idAgendamento) {
        return agendamentoService.cancelar(idAgendamento);
    }

    @PutMapping("/{id}/concluir")
    public AgendamentoResponse concluir(@PathVariable("id") Long idAgendamento) {
        return agendamentoService.concluir(idAgendamento);
    }

    @PutMapping("/{id}/confirmar")
    public AgendamentoResponse confirmar(@PathVariable("id") Long idAgendamento) {
        return agendamentoService.confirmar(idAgendamento);
    }

    // public AgendamentoResponse reagendar(@PathVariable("id") Long idAgendamento, @Valid @RequestBody AgendamentoUpdateRequest request) {
    //     return agendamentoService.reagendar(idAgendamento, request);
    // }
}

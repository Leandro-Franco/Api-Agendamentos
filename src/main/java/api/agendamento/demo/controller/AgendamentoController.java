package api.agendamento.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.agendamento.demo.dto.AgendamentoCreateRequest;
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

    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@PathVariable Long idAgendamento, @Valid @RequestBody AgendamentoUpdateRequest request) {
        return agendamentoService.atualizar(idAgendamento, request);
    }

    @PutMapping("/delete/{id}")
    public void deletar(@PathVariable Long idAgendamento) {
        agendamentoService.deletar(idAgendamento);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse procurar(@PathVariable Long idAgendamento) {
        return agendamentoService.procurar(idAgendamento);
    }

    @PutMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@PathVariable Long idAgendamento) {
        return agendamentoService.cancelar(idAgendamento);
    }

    @PutMapping("/{id}/concluir")
    public AgendamentoResponse concluir(@PathVariable Long idAgendamento) {
        return agendamentoService.concluir(idAgendamento);
    }

    @PutMapping("/{id}/confirmar")
    public AgendamentoResponse confirmar(@PathVariable Long idAgendamento) {
        return agendamentoService.confirmar(idAgendamento);
    }

    // public AgendamentoResponse reagendar(@PathVariable Long idAgendamento, @Valid @RequestBody AgendamentoUpdateRequest request) {
    //     return agendamentoService.reagendar(idAgendamento, request);
    // }
}

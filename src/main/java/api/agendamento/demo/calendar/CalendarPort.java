package api.agendamento.demo.calendar;

import api.agendamento.demo.model.Agendamento;

/**
 * Saida para o espelho no calendario.
 *
 * E uma interface, e nao chamada direta a biblioteca do Google, por dois motivos:
 * a suite roda sem rede nem credencial, e trocar de provedor de calendario nao
 * toca no service.
 */
public interface CalendarPort {

    /** Cria o evento e devolve a chave emitida pelo provedor. */
    String criar(Agendamento agendamento);

    void atualizar(String eventId, Agendamento agendamento);

    void remover(String eventId);
}

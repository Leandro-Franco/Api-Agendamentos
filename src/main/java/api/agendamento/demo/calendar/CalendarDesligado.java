package api.agendamento.demo.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import api.agendamento.demo.model.Agendamento;

/**
 * Implementacao ativa quando nao ha calendario configurado.
 *
 * Existe para que a ausencia de credencial do Google nao impeca a aplicacao de
 * subir: a API e a fonte da verdade e precisa funcionar sozinha. Sem espelho, o
 * google_event_id fica nulo, que e exatamente o estado de "ainda nao espelhado".
 */
@Component
@ConditionalOnProperty(name = "agendamentos.calendar.habilitado",
        havingValue = "false", matchIfMissing = true)
public class CalendarDesligado implements CalendarPort {

    private static final Logger log = LoggerFactory.getLogger(CalendarDesligado.class);

    @Override
    public String criar(Agendamento agendamento) {
        log.debug("Calendario desligado: agendamento {} nao foi espelhado", agendamento.getIdAgendamento());
        return null;
    }

    @Override
    public void atualizar(String eventId, Agendamento agendamento) {
        log.debug("Calendario desligado: evento {} nao foi atualizado", eventId);
    }

    @Override
    public void remover(String eventId) {
        log.debug("Calendario desligado: evento {} nao foi removido", eventId);
    }
}

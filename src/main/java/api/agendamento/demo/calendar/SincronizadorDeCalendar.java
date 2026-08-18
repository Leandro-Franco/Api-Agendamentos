package api.agendamento.demo.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import api.agendamento.demo.event.AgendamentoRetirado;
import api.agendamento.demo.event.AgendamentoSalvo;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.repository.AgendamentoRepository;

/**
 * Mantem o espelho no calendario alinhado com a fonte da verdade.
 *
 * Roda em AFTER_COMMIT de proposito. O Google nao participa da transacao do
 * banco: chamar de dentro dela significaria (a) segurar conexao e lock durante
 * uma chamada de rede externa e (b) nao ter como desfazer o evento se o commit
 * falhasse depois. Commitar primeiro e espelhar depois inverte o risco para o
 * lado certo: o espelho pode atrasar, a verdade nunca fica errada.
 */
@Component
public class SincronizadorDeCalendar {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorDeCalendar.class);

    private final CalendarPort calendar;
    private final AgendamentoRepository agendamentoRepository;

    public SincronizadorDeCalendar(CalendarPort calendar, AgendamentoRepository agendamentoRepository) {
        this.calendar = calendar;
        this.agendamentoRepository = agendamentoRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void espelhar(AgendamentoSalvo evento) {
        agendamentoRepository.findById(evento.idAgendamento()).ifPresent(this::espelhar);
    }

    private void espelhar(Agendamento agendamento) {
        try {
            if (agendamento.getGoogleEventId() == null) {
                String eventId = calendar.criar(agendamento);
                if (eventId != null) {
                    agendamento.setGoogleEventId(eventId);
                    agendamentoRepository.save(agendamento);
                }
            } else {
                calendar.atualizar(agendamento.getGoogleEventId(), agendamento);
            }
        } catch (RuntimeException e) {
            // Falha no espelho nao desfaz nem invalida o agendamento, que ja esta
            // commitado. O google_event_id continua nulo, e e por ele que um job de
            // reconciliacao encontra o que ficou para tras.
            log.warn("Falha ao espelhar o agendamento {} no calendario", agendamento.getIdAgendamento(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void retirar(AgendamentoRetirado evento) {
        if (evento.googleEventId() == null) {
            return;
        }
        try {
            calendar.remover(evento.googleEventId());
        } catch (RuntimeException e) {
            log.warn("Falha ao remover o evento {} do calendario", evento.googleEventId(), e);
        }
    }
}

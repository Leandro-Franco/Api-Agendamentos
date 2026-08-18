package api.agendamento.demo.event;

/**
 * Publicado quando um agendamento deixa a agenda, por cancelamento ou remocao.
 *
 * Carrega a chave do evento, e nao o id do agendamento: no caso da remocao o
 * registro ja nao existe quando o consumidor roda.
 */
public record AgendamentoRetirado(String googleEventId) {}

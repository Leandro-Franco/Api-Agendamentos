package api.agendamento.demo.event;

/**
 * Publicado quando um agendamento e criado ou tem horario alterado. O espelho
 * reage criando ou atualizando o evento correspondente.
 *
 * Carrega o id, e nao a entidade: o consumidor roda depois do commit, numa
 * transacao propria, e deve ler o estado ja persistido.
 */
public record AgendamentoSalvo(Long idAgendamento) {}

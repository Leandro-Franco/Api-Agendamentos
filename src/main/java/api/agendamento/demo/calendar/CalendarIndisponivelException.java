package api.agendamento.demo.calendar;

/**
 * Falha ao falar com o calendario. Nao faz parte do contrato HTTP da API: quando
 * ela acontece, a operacao do usuario ja foi commitada com sucesso e so o espelho
 * ficou para tras. Quem trata e o SincronizadorDeCalendar, com log.
 */
public class CalendarIndisponivelException extends RuntimeException {

    public CalendarIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

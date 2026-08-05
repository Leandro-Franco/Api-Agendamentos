package api.agendamento.demo.exception;

public class ConflitoDeAgendamentoException extends RuntimeException {
    
    public ConflitoDeAgendamentoException(String message) {
        super(message);
    }
}

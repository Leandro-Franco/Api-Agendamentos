package api.agendamento.demo.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ConflitoDeAgendamentoException.class)
    public ProblemDetail conflito(ConflitoDeAgendamentoException ex) {
        return montar(HttpStatus.CONFLICT, "Conflito de agendamento",
                "CONFLITO_AGENDAMENTO", ex.getMessage());
    }

    @ExceptionHandler(IntervaloInvalidoException.class)
    public ProblemDetail intervalo(IntervaloInvalidoException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Intervalo de datas invalido",
                "INTERVALO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail naoEncontrado(EntityNotFoundException ex) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado",
                "NAO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoIlegivel(HttpMessageNotReadableException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo invalido", "CORPO_ILEGIVEL",
                "Corpo da requisicao malformado ou com tipo incompativel.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = montar(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "VALIDACAO", "Um ou mais campos sao invalidos.");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade", ex);
        return montar(HttpStatus.CONFLICT, "Violacao de integridade", "VIOLACAO_INTEGRIDADE",
                "A operacao viola uma restricao de integridade dos dados.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        log.error("Erro nao tratado", ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "ERRO_INTERNO",
                "Erro interno do servidor.");
    }

    private ProblemDetail montar(HttpStatus status, String titulo, String codigo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setProperty("codigo", codigo);
        return problema;
    }
}
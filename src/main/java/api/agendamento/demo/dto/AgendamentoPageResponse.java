package api.agendamento.demo.dto;

import java.util.List;

public record AgendamentoPageResponse(
    List<AgendamentoResponse> conteudo,
    int pagina,
    int tamanho,
    long total,
    boolean ultimaPagina
) {}

package api.agendamento.demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgendamentoCreateRequest(
    @NotBlank @Size(max = 120) String titulo,
    @Size(max = 500) String descricao,
    @NotNull LocalDateTime dataInicio,
    @NotNull LocalDateTime dataFim,
    @NotNull Long idUsuario
    // @NotBlank @Size(max = 60)String usnomeUsuario
) {}

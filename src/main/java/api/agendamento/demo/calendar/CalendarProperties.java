package api.agendamento.demo.calendar;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao do espelho no Google Calendar.
 *
 * `id` costuma ser o e-mail da sua agenda, nao "primary": autenticando como conta
 * de servico, "primary" seria a agenda da propria conta de servico, que ninguem ve.
 * A agenda de destino precisa estar compartilhada com o e-mail da conta de servico,
 * com permissao de alterar eventos.
 *
 * `fuso` existe porque a entidade guarda LocalDateTime, que nao carrega fuso algum.
 * Alguem precisa dizer o que "10:00" significa em tempo absoluto, e essa decisao
 * fica aqui, explicita, em vez de virar o padrao de qualquer cliente que chamar.
 */
@ConfigurationProperties(prefix = "agendamentos.calendar")
public record CalendarProperties(
        boolean habilitado,
        String credenciais,
        String id,
        String fuso
) {
    public CalendarProperties {
        if (id == null || id.isBlank()) {
            id = "primary";
        }
        if (fuso == null || fuso.isBlank()) {
            fuso = "America/Sao_Paulo";
        }
    }
}

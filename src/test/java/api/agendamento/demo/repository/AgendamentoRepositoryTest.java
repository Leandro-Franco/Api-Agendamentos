package api.agendamento.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;

import api.agendamento.demo.TestcontainersConfiguration;
import api.agendamento.demo.model.Agendamento;
import api.agendamento.demo.model.StatusAgendamento;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureDataSourceInitialization
@Import(TestcontainersConfiguration.class)
class AgendamentoRepositoryTest {

    private static final Long ID_USUARIO = 1L;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void inserirUsuario() {
        jdbcTemplate.update(
                "INSERT INTO tb_usuario (id_usuario, nome, email) VALUES (?, ?, ?)",
                ID_USUARIO, "Fulano de Tal", "fulano@teste.com");
    }

    private Agendamento agendamentoDas(int horaInicio, int horaFim) {
        return Agendamento.builder()
                .idUsuario(ID_USUARIO)
                .titulo("Consulta")
                .dataInicio(LocalDateTime.of(2026, 8, 10, horaInicio, 0))
                .dataFim(LocalDateTime.of(2026, 8, 10, horaFim, 0))
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    void existsConflito_deveSerTrue_quandoOsIntervalosSeSobrepoem() {
        agendamentoRepository.save(agendamentoDas(14, 16));

        boolean conflito = agendamentoRepository.existsConflito(
                ID_USUARIO,
                LocalDateTime.of(2026, 8, 10, 15, 0),
                LocalDateTime.of(2026, 8, 10, 17, 0),
                null);

        assertThat(conflito).isTrue();
    }

    @Test
    void existsConflito_deveSerFalse_quandoOsIntervalosNaoSeTocam() {
        agendamentoRepository.save(agendamentoDas(14, 16));

        boolean conflito = agendamentoRepository.existsConflito(
                ID_USUARIO,
                LocalDateTime.of(2026, 8, 10, 18, 0),
                LocalDateTime.of(2026, 8, 10, 19, 0),
                null);

        assertThat(conflito).isFalse();
    }

    @Test
    void listar_deveTrazerApenasOQueCaiNaJanela() {
        agendamentoRepository.save(agendamentoDas(9, 10));
        agendamentoRepository.save(agendamentoDas(14, 16));

        Page<Agendamento> pagina = agendamentoRepository.listar(
                ID_USUARIO,
                LocalDateTime.of(2026, 8, 10, 13, 0),
                LocalDateTime.of(2026, 8, 10, 18, 0),
                PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getDataInicio().getHour()).isEqualTo(14);
    }

    // Cancelar nao e apagar: o registro continua no banco, mas some da agenda
    // e deixa de ocupar o horario. E o que da sentido a cancelar existir.
    @Test
    void listar_naoDeveTrazerAgendamentoCancelado() {
        Agendamento cancelado = agendamentoDas(14, 16);
        cancelado.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(cancelado);

        Page<Agendamento> pagina = agendamentoRepository.listar(
                ID_USUARIO, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).isEmpty();
    }
}

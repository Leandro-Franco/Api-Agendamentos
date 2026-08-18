package api.agendamento.demo.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import api.agendamento.demo.model.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Agendamento a
        WHERE a.idUsuario = :idUsuario
        AND a.status IN ('AGENDADO', 'CONFIRMADO')
        AND (:idAgendamento IS NULL OR a.idAgendamento <> :idAgendamento)
        AND (a.dataInicio < :dataFim AND a.dataFim > :dataInicio)
    """)
    
    boolean existsConflito(
            @Param("idUsuario") Long idUsuario,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("idAgendamento") Long idAgendamento
    );

    // Mesma regra de sobreposicao do existsConflito: um agendamento entra na
    // janela [de, ate) quando termina depois do inicio e comeca antes do fim.
    // Filtros nulos significam "sem limite". O CAST existe porque o Postgres nao
    // consegue inferir o tipo de um parametro que so aparece dentro de IS NULL.
    @Query("""
        SELECT a
        FROM Agendamento a
        WHERE a.idUsuario = :idUsuario
        AND a.status IN ('AGENDADO', 'CONFIRMADO')
        AND (CAST(:de AS LocalDateTime) IS NULL OR a.dataFim > :de)
        AND (CAST(:ate AS LocalDateTime) IS NULL OR a.dataInicio < :ate)
    """)
    Page<Agendamento> listar(
            @Param("idUsuario") Long idUsuario,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable
    );
}

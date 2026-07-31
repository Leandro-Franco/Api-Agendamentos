package api.agendamento.demo.repository;

import java.time.LocalDateTime;

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
}

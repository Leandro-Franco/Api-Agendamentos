package api.agendamento.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_agendamento")

public class Agendamento {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long idAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private String dataInicio;

    @Column(name = "data_fim", nullable = false)
    private String dataFim;

    @Column(name = "criado_em", nullable = false)
    private String criadoEm;
    
    @Column(name = "atualizado_em", nullable = false)
    private String atualizadoEm;
}

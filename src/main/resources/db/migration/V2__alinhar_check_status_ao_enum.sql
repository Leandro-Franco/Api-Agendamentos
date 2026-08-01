ALTER TABLE tb_agendamento
    DROP CONSTRAINT ck_status_agendamento;

ALTER TABLE tb_agendamento
    ADD CONSTRAINT ck_status_agendamento
    CHECK (status IN ('AGENDADO', 'CONFIRMADO', 'CANCELADO', 'CONCLUIDO'));
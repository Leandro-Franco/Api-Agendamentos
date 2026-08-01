ALTER TABLE tb_agendamento
    DROP CONSTRAINT fk_usuario;

ALTER TABLE tb_usuario
    ALTER COLUMN id_usuario TYPE BIGINT;

ALTER TABLE tb_agendamento
    ALTER COLUMN id_agendamento TYPE BIGINT,
    ALTER COLUMN id_usuario     TYPE BIGINT,
    ALTER COLUMN criado_em      TYPE TIMESTAMP USING criado_em::timestamp,
    ALTER COLUMN atualizado_em  TYPE TIMESTAMP USING atualizado_em::timestamp;

ALTER TABLE tb_agendamento
    ADD CONSTRAINT fk_usuario
    FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario);
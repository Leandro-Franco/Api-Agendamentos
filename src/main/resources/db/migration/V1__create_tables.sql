CREATE TABLE tb_usuario (
    id_usuario SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE tb_agendamento(
    id_agendamento SERIAL PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    id_usuario INT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_status_agendamento CHECK (status IN ('AGENDADO', 'PENDENTE', 'CONFIRMADO', 'CANCELADO')),
    CONSTRAINT ck_intervalo_agendamento CHECK (data_inicio < data_fim),
    CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario)
);


-- vai ser inserido no JPA
CREATE INDEX idx_agendamento_data_inicio
    ON tb_agendamento(id_usuario, data_inicio, data_fim);

-- função para atualizar o campo atualizado_em
-- é um before update, então 
-- antes de atualizar o registro, 
-- ele vai atualizar o campo atualizado_em 
-- com a data e hora atual
CREATE OR REPLACE FUNCTION atualizar_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- criação do trigger para atualizar o campo atualizado_em
CREATE TRIGGER trigger_atualizar_atualizado_em
BEFORE UPDATE ON tb_agendamento
FOR EACH ROW
EXECUTE FUNCTION atualizar_atualizado_em();


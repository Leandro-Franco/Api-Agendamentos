-- O Google Calendar devolve o id do evento no momento da criacao. Sem guardar
-- esse id, nenhuma operacao posterior no espelho e possivel: atualizar ou apagar
-- um evento exige a chave que o proprio Calendar emitiu. Ate aqui a chave era
-- lida e descartada, e por isso cancelar na API nunca removia nada do calendario.
ALTER TABLE tb_agendamento
    ADD COLUMN google_event_id VARCHAR(1024);

-- Nulo e estado legitimo, nao falha: o agendamento existe na fonte da verdade
-- mesmo que a copia no calendario ainda nao tenha sido criada, ou tenha falhado.
-- O UNIQUE impede que dois agendamentos apontem para o mesmo evento; no Postgres
-- varios nulos convivem com essa restricao.
ALTER TABLE tb_agendamento
    ADD CONSTRAINT uk_google_event_id UNIQUE (google_event_id);

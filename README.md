# API de Agendamentos

API REST para gestão de agendamentos, construída para ser consumida por um agente de
IA — não por um formulário. As regras de negócio ficam no servidor, e não no prompt.

**Stack:** Java 25 · Spring Boot 4.1 · PostgreSQL 16 · Flyway · Testcontainers

---

## Por que este projeto existe

O objetivo é servir de backend para um assistente de agendamento conversacional
(Telegram → N8N → LLM → esta API). A decisão de arquitetura central é onde colocar as
regras:

| Abordagem | Quem garante que não haja conflito de horário |
|---|---|
| Base de conhecimento em `.md` no prompt | O modelo, se lembrar de checar |
| **Ferramenta chamando esta API** | **O `CHECK` do Postgres e o `409` do servidor** |

A segunda opção também é mais barata: o schema de uma tool ocupa menos contexto do que
um documento de regras, e é reenviado a cada chamada. Mas a economia de token é o bônus —
o prêmio é que o schema é validado por uma máquina, e o documento não é validado por
ninguém.

Consequência prática: **a API não confia no cliente.** Toda invariante é verificada aqui,
com erro estruturado e legível por máquina no retorno.

---

## Como rodar

### Testes — só precisa de Docker

```bash
./mvnw test
```

A suíte sobe seu próprio PostgreSQL via Testcontainers e executa as migrations reais do
Flyway. Não requer banco instalado, nem configuração, nem variável de ambiente.

### Aplicação — precisa de PostgreSQL
```idem ``

#### docker compose up -d

Credenciais esperadas em `application.properties`: `postgres` / `postgres` em
`localhost:5432`.

```bash
./mvnw spring-boot:run
```

O Flyway aplica as quatro migrations na subida. A aplicação escuta em `:8080`.

---

## Endpoints

Base: `/agendamentos`

| Método | Caminho | Ação |
|---|---|---|
| `POST` | `/agendamentos` | Cria um agendamento |
| `GET` | `/agendamentos` | Lista por usuário, com janela opcional e paginação |
| `GET` | `/agendamentos/{id}` | Busca por id |
| `PUT` | `/agendamentos/{id}` | Atualiza campos |
| `PUT` | `/agendamentos/{id}/confirmar` | `AGENDADO` → `CONFIRMADO` |
| `PUT` | `/agendamentos/{id}/concluir` | → `CONCLUIDO` |
| `PUT` | `/agendamentos/{id}/cancelar` | → `CANCELADO` |
| `PUT` | `/agendamentos/delete/{id}` | Remove (ver *Limitações*) |

### Exemplo

```bash
curl -X POST http://localhost:8080/agendamentos \
  -H 'Content-Type: application/json' \
  -d '{
    "titulo": "Consulta de rotina",
    "descricao": "Primeira avaliacao",
    "dataInicio": "2026-08-05T14:00:00",
    "dataFim": "2026-08-05T15:00:00",
    "idUsuario": 1
  }'
```

A listagem responde duas perguntas com um endpoint só. Com janela, ela diz o que existe
naquele intervalo — e uma lista vazia é a resposta de disponibilidade:

```bash
curl 'http://localhost:8080/agendamentos?idUsuario=1&de=2026-08-12T00:00:00&ate=2026-08-12T23:59:59'
```

`idUsuario` é obrigatório: a listagem nasce escopada ao dono, em vez de devolver tudo e
confiar no cliente para filtrar. `de` e `ate` são opcionais; ausentes significam sem limite.
Cancelados não aparecem.

Ciclo de vida: `AGENDADO → CONFIRMADO → CONCLUIDO`, com `CANCELADO` como saída.

---

## Contrato de erros

Todo erro retorna `ProblemDetail` ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457))
com um campo adicional `codigo` — um identificador estável, feito para um `switch` do
cliente e não para leitura humana.

| Situação | Status | `codigo` |
|---|---|---|
| Horário já ocupado para o usuário | `409` | `CONFLITO_AGENDAMENTO` |
| `dataFim` anterior à `dataInicio` | `400` | `INTERVALO_INVALIDO` |
| Bean Validation reprovou o corpo | `400` | `VALIDACAO` |
| JSON malformado | `400` | `CORPO_ILEGIVEL` |
| Id inexistente | `404` | `NAO_ENCONTRADO` |
| Violação de restrição no banco (ex.: usuário inexistente) | `409` | `VIOLACAO_INTEGRIDADE` |
| Parâmetro de query obrigatório ausente | `400` | `PARAMETRO_AUSENTE` |
| Parâmetro de query com tipo incompatível | `400` | `PARAMETRO_INVALIDO` |
| Data fora do formato ISO 8601 | `400` | `DATA_INVALIDA` |
| Falha não prevista | `500` | `ERRO_INTERNO` |

```json
{
  "type": "about:blank",
  "title": "Conflito de agendamento",
  "status": 409,
  "detail": "Ja existe um agendamento para o usuario nesse intervalo de datas.",
  "codigo": "CONFLITO_AGENDAMENTO"
}
```

Erros de validação incluem `campos`, mapeando atributo → mensagem. Stack traces nunca
vão para o cliente; o caso não previsto é registrado no log do servidor.

Isso importa para o consumidor automatizado: um `500` significa "tente de novo", e um
`409` significa "mude alguma coisa". Devolver `500` para conflito de horário faria o
agente insistir em uma operação que jamais teria sucesso.

---

## Testes

```
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

Cada teste corresponde a um defeito que existiu neste repositório. Nenhum foi escrito
para inflar cobertura.

| Classe | Camada | Defeito que ele trava |
|---|---|---|
| `AgendamentoMapperTest` | JUnit puro | mapper não preenchia `idUsuario`, coluna `NOT NULL`; string vazia entrando em `LocalDateTime.parse` |
| `AgendamentoControllerTest` | `@WebMvcTest` | status HTTP e formato do erro; parâmetro obrigatório ausente virando `500` |
| `AgendamentoRepositoryTest` | `@DataJpaTest` + Testcontainers | argumentos invertidos em `existsConflito`; janela da listagem e exclusão de cancelados |
| `DemoApplicationTests` | `@SpringBootTest` | divergência entre schema do Flyway e entidades JPA |

O teste de repositório sobe um PostgreSQL real e aplica as migrations de produção — o
schema exercitado é o mesmo que roda em produção, com `CHECK`, índice e trigger. Um banco
em memória validaria uma realidade diferente da que o sistema encontra.

`DemoApplicationTests` não é boilerplate: com `ddl-auto=validate`, subir o contexto
falha se o Flyway e o modelo JPA discordarem. É a rede de proteção contra o schema
divergir sem ninguém perceber.

---

## Decisões de projeto

**Flyway é a única autoridade sobre o schema.** `ddl-auto=validate` — o Hibernate
confere e reclama, mas nunca altera. Com `update`, as duas ferramentas disputam a
mesma responsabilidade e o schema real deixa de corresponder ao versionado.

**As migrations não são reescritas.** `V2` e `V3` corrigem `V1` em vez de editá-la. Um
arquivo já aplicado em qualquer ambiente é imutável — o histórico é o registro do que
realmente aconteceu.

**Conflito de horário é decidido no servidor.** Um cliente pode consultar, mas nenhum
cliente decide. A verificação vive no service e é coberta por teste com banco real.

**Regra de negócio nunca depende de mensagem de texto.** O tipo da exceção carrega o
motivo; o `codigo` no `ProblemDetail` carrega o motivo para o cliente. Nenhuma das duas
pontas faz parsing de frase em português.

**A API é a fonte da verdade; o Google Calendar é espelho.** O chat entrega a verdade ao
usuário, mas não a guarda — toda leitura do agente vem da API, e o Calendar recebe cópia
só depois de a escrita ser aceita. Se o Calendar falhar, o agendamento continua existindo;
se a API recusar, nada é espelhado. Dois sistemas gravando o mesmo fato divergem em
silêncio, e foi o que aconteceu na primeira integração: a API guardou `10:00`, o Calendar
exibiu `11:00`, ambos relataram sucesso.

---

## Limitações conhecidas

Documentadas de propósito — a lista é curta porque é real.

- **`PUT /agendamentos/delete/{id}`** usa verbo e caminho incorretos. Deveria ser
  `DELETE /agendamentos/{id}`. Correção pendente por ser quebra de contrato.
- **`POST` responde `200`**, não `201 Created`, e não retorna header `Location`.
- **`tb_usuario` não é exposta.** Usuários só existem via SQL; `idUsuario` precisa ser
  conhecido de antemão pelo cliente.
- **`AgendamentoUpdateRequest` usa `String` para datas**, enquanto
  `AgendamentoCreateRequest` usa `LocalDateTime`. Inconsistência a alinhar.
- **Não há constraint de exclusão no banco.** A checagem de conflito é `SELECT` seguido
  de `INSERT` — há janela de corrida sob concorrência. A correção definitiva é uma
  `EXCLUDE USING gist` sobre `(id_usuario, intervalo)`, que torna a sobreposição
  estruturalmente impossível.
- **`LocalDateTime` não guarda fuso.** A API grava `10:00` sem âncora temporal, então não
  tem como perceber que um consumidor interpretou esse mesmo valor em outro fuso. Funciona
  enquanto o sistema for de fuso único; a correção definitiva é `OffsetDateTime`.
- **Dev e produção rodam versões diferentes do PostgreSQL.** A suíte e o compose usam
  `postgres:16-alpine`; o serviço no Easypanel subiu com o 17. Isso enfraquece a garantia
  de que o schema exercitado no teste é o mesmo de produção. Alinhar os dois.
- **Edições feitas direto no Google Calendar não retornam.** Sendo espelho, ele não
  propaga alterações de volta — arrastar um evento no calendário não muda o agendamento.
- **O espelho ainda só reflete a criação.** `google_event_id` já é persistido, mas o fluxo
  no N8N não usa a chave para atualizar nem remover o evento. Cancelar na API deixa o
  compromisso no calendário.

## Próximos passos

1. **Checagem de dono** em `procurar`, `cancelar`, `confirmar` e `deletar`. As quatro
   recebem apenas o id do agendamento e não comparam com o solicitante. A listagem já
   nasce escopada, o que limita a descoberta de ids alheios — mas um id adivinhado
   ainda passa.
2. **Autenticação por header** (`X-API-Key`, filtro no Spring). Obrigatória se a API for
   exposta publicamente; dispensável enquanto o tráfego for interno ao servidor.
3. Pipeline de CI (Jenkins) — habilitado pela suíte não depender mais de banco local
4. Publicação de eventos: SNS → SQS → Lambda
5. Constraint `EXCLUDE` contra corrida na criação
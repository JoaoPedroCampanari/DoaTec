# Diagrama de Entidades — DoaTec

```mermaid
erDiagram

    %% ===================== ACCOUNT =====================

    Pessoa {
        Integer id PK
        String nome
        String email UK
        String senha
        String endereco
        String logradouro
        String numero
        String bairro
        String cidade
        String estado
        String telefone
        Role role
        Boolean ativo
        DateTime createdAt
        DateTime updatedAt
        DateTime deletedAt
    }

    Aluno {
        Integer id PK_FK
        String ra UK
    }

    DoadorPF {
        Integer id PK_FK
        String cpf UK
    }

    DoadorPJ {
        Integer id PK_FK
        String cnpj UK
        String razaoSocial
    }

    Pessoa ||--o| Aluno : "herda"
    Pessoa ||--o| DoadorPF : "herda"
    Pessoa ||--o| DoadorPJ : "herda"

    %% ===================== DONATION =====================

    Doacao {
        Integer id PK
        Integer doador_id FK
        LocalDate dataDoacao
        StatusDoacao status
        PreferenciaEntrega preferenciaEntrega
        TEXT descricaoGeral
        String urlFoto
        Integer adminAvaliador_id FK
        DateTime dataAvaliacao
        TEXT observacaoAdmin
        DateTime deletedAt
    }

    ItemDoado {
        Integer id PK
        Integer doacao_id FK
        String tipoItem
        String descricao
        Integer equipamentoGerado_id FK
    }

    Pessoa ||--o{ Doacao : "doador"
    Pessoa ||--o{ Doacao : "admin_avaliador"
    Doacao ||--o{ ItemDoado : "contém"

    %% ===================== SOLICITACAO =====================

    SolicitacaoHardware {
        Integer id PK
        Integer aluno_id FK
        StatusSolicitacao status
        LocalDate dataSolicitacao
        TEXT justificativa
        String preferenciaEquipamento
        Integer adminAvaliador_id FK
        DateTime dataAvaliacao
        TEXT observacaoAdmin
        DateTime deletedAt
    }

    Pessoa ||--o{ SolicitacaoHardware : "aluno"
    Pessoa ||--o{ SolicitacaoHardware : "admin_avaliador"

    %% ===================== INVENTORY =====================

    Equipamento {
        Integer id PK
        String tipo
        TEXT descricao
        StatusEquipamento status
        EstadoConservacao estadoConservacao
        Integer itemOrigem_id FK
        Integer solicitacaoDestino_id FK
        Integer alunoDestinatario_id FK
        DateTime dataEntradaInventario
        DateTime dataAtribuicao
        DateTime dataEntrega
        DateTime deletedAt
    }

    ItemDoado |o--o| Equipamento : "gera"
    Equipamento }o--o| SolicitacaoHardware : "destino"
    Pessoa ||--o{ Equipamento : "aluno_destinatario"

    %% ===================== SUPPORT =====================

    SuporteFormulario {
        Integer id PK
        Integer autor_id FK
        String assunto
        TEXT mensagem
        StatusSuporte status
        Integer adminResponsavel_id FK
        TEXT resposta
        DateTime dataCriacao
        DateTime dataResolucao
        DateTime deletedAt
    }

    Pessoa ||--o{ SuporteFormulario : "autor"
    Pessoa ||--o{ SuporteFormulario : "admin_responsavel"

    %% ===================== NOTIFICATION =====================

    Notificacao {
        Integer id PK
        String titulo
        TEXT mensagem
        DateTime dataCriacao
        Boolean lida
        DateTime dataLeitura
        Integer destinatario_id FK
        TipoNotificacao tipo
        Integer entidadeRelacionadaId
        String entidadeRelacionadaTipo
    }

    Pessoa ||--o{ Notificacao : "destinatario"

    %% ===================== AUDIT =====================

    LogAcao {
        Integer id PK
        Integer admin_id FK
        AcaoTipo acao
        String entidade
        Integer entidadeId
        TEXT descricao
        DateTime dataAcao
    }

    Pessoa ||--o{ LogAcao : "admin"

    %% ===================== CHAT =====================

    MensagemChat {
        Integer id PK
        TEXT conteudo
        DateTime dataEnvio
        Integer remetente_id FK
        Integer referenciaId
        ContextoChat contexto
    }

    Pessoa ||--o{ MensagemChat : "remetente"

    %% ===================== ENUMS =====================

    Role {
        USER
        ADMIN
        SUPER_ADMIN
    }

    StatusDoacao {
        EM_TRIAGEM
        AGUARDANDO_COLETA
        RECEBIDO
        EM_ANALISE
        FINALIZADO
        REJEITADA
    }

    PreferenciaEntrega {
        PONTO_DE_COLETA
        SOLICITAR_RETIRADA
    }

    StatusSolicitacao {
        EM_ANALISE
        APROVADA
        REJEITADA
        CONCLUIDA
    }

    StatusEquipamento {
        DISPONIVEL
        RESERVADO
        ENTREGUE
    }

    EstadoConservacao {
        NOVO
        EXCELENTE
        BOM
        REGULAR
        NECESSITA_REPARO
    }

    TipoNotificacao {
        DOACAO_APROVADA
        DOACAO_REJEITADA
        SOLICITACAO_APROVADA
        SOLICITACAO_REJEITADA
        SOLICITACAO_CONCLUIDA
        EQUIPAMENTO_DISPONIVEL
        EQUIPAMENTO_ATRIBUIDO
        MATCHING_SUGERIDO
        SISTEMA
    }

    StatusSuporte {
        ABERTO
        EM_ANDAMENTO
        RESOLVIDO
        FECHADO
    }

    AcaoTipo {
        APROVAR_DOACAO
        REJEITAR_DOACAO
        APROVAR_SOLICITACAO
        REJEITAR_SOLICITACAO
        RESPONDER_SUPORTE
        DESATIVAR_USUARIO
    }

    ContextoChat {
        SUPORTE
        DOACAO
        SOLICITACAO
    }
```

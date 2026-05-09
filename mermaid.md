# Diagrama de Entidades — DoaTec

```mermaid
erDiagram

    %% ========== PESSOA (herança JOINED) ==========

    Pessoa {
        Integer id PK
        String nome
        String email UK
        String senha
        String cep
        String logradouro
        String numero
        String bairro
        String cidade
        String estado
        String telefone
        Boolean ativo
        DateTime createdAt
        DateTime updatedAt
        DateTime deletedAt
    }

    Aluno {
        String ra UK
    }

    DoadorPF {
        String cpf UK
    }

    DoadorPJ {
        String cnpj UK
        String razaoSocial
    }

    Admin {
    }

    Pessoa ||--o| Aluno : "herda"
    Pessoa ||--o| DoadorPF : "herda"
    Pessoa ||--o| DoadorPJ : "herda"
    Pessoa ||--o| Admin : "herda"

    %% ========== DOAÇÃO ==========

    Doacao {
        Integer id PK
        LocalDate dataDoacao
        StatusDoacao status
        PreferenciaEntrega preferenciaEntrega
        String descricaoGeral
        String urlFoto
        DateTime dataAvaliacao
        String observacaoAdmin
        DateTime deletedAt
    }

    ItemDoado {
        Integer id PK
        String tipoItem
        String descricao
    }

    Aluno ||--o{ Doacao : "doador"
    DoadorPF ||--o{ Doacao : "doador"
    DoadorPJ ||--o{ Doacao : "doador"
    Admin ||--o{ Doacao : "avalia"
    Doacao ||--o{ ItemDoado : "contem"

    %% ========== SOLICITAÇÃO ==========

    SolicitacaoHardware {
        Integer id PK
        StatusSolicitacao status
        LocalDate dataSolicitacao
        String justificativa
        String preferenciaEquipamento
        DateTime dataAvaliacao
        String observacaoAdmin
        DateTime deletedAt
    }

    Aluno ||--o{ SolicitacaoHardware : "solicita"
    Admin ||--o{ SolicitacaoHardware : "avalia"

    %% ========== INVENTÁRIO ==========

    Equipamento {
        Integer id PK
        String tipo
        String descricao
        StatusEquipamento status
        EstadoConservacao estadoConservacao
        DateTime dataEntradaInventario
        DateTime dataAtribuicao
        DateTime dataEntrega
        DateTime deletedAt
    }

    ItemDoado ||--o| Equipamento : "gera"
    Equipamento }o--o| SolicitacaoHardware : "destino"
    Aluno ||--o{ Equipamento : "destinatario"

    %% ========== SUPORTE ==========

    SuporteFormulario {
        Integer id PK
        String assunto
        String mensagem
        StatusSuporte status
        String resposta
        DateTime dataCriacao
        DateTime dataResolucao
        DateTime deletedAt
    }

    Pessoa ||--o{ SuporteFormulario : "autor"
    Admin ||--o{ SuporteFormulario : "responde"

    %% ========== NOTIFICAÇÃO ==========

    Notificacao {
        Integer id PK
        String titulo
        String mensagem
        DateTime dataCriacao
        Boolean lida
        DateTime dataLeitura
        TipoNotificacao tipo
        Integer entidadeRelacionadaId
        String entidadeRelacionadaTipo
    }

    Pessoa ||--o{ Notificacao : "destinatario"

    %% ========== AUDITORIA ==========

    LogAcao {
        Integer id PK
        AcaoTipo acao
        String entidade
        Integer entidadeId
        String descricao
        DateTime dataAcao
    }

    Admin ||--o{ LogAcao : "registra"

    %% ========== CHAT ==========

    MensagemChat {
        Integer id PK
        String conteudo
        DateTime dataEnvio
        Integer referenciaId
        ContextoChat contexto
    }

    Pessoa ||--o{ MensagemChat : "remetente"
```

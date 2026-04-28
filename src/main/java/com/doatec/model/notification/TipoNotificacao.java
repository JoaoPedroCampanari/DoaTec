package com.doatec.model.notification;

/**
 * Define os tipos de notificações do sistema.
 */
public enum TipoNotificacao {

    DOACAO_APROVADA("Sua doação foi aprovada", "doacao"),
    DOACAO_REJEITADA("Sua doação foi rejeitada", "doacao"),
    DOACAO_STATUS_ATUALIZADO("O status da sua doação foi atualizado", "doacao"),
    SOLICITACAO_APROVADA("Sua solicitação foi aprovada", "solicitacao"),
    SOLICITACAO_REJEITADA("Sua solicitação foi rejeitada", "solicitacao"),
    SOLICITACAO_CONCLUIDA("Sua solicitação foi concluída", "solicitacao"),
    SOLICITACAO_STATUS_ATUALIZADO("O status da sua solicitação foi atualizado", "solicitacao"),
    EQUIPAMENTO_DISPONIVEL("Equipamento compatível disponível", "equipamento"),
    EQUIPAMENTO_ATRIBUIDO("Equipamento atribuído à sua solicitação", "equipamento"),
    MATCHING_SUGERIDO("Matching sugerido para análise", "matching"),
    SISTEMA("Notificação do sistema", "sistema");

    private final String template;
    private final String entidadeTipo;

    TipoNotificacao(String template, String entidadeTipo) {
        this.template = template;
        this.entidadeTipo = entidadeTipo;
    }

    public String getTemplate() {
        return template;
    }

    public String getEntidadeTipo() {
        return entidadeTipo;
    }
}
package com.doatec.model.notification;

/**
 * Define os tipos de notificações do sistema.
 */
public enum TipoNotificacao {

    DOACAO_APROVADA("Sua doação foi aprovada", "doacao"),
    DOACAO_REJEITADA("Sua doação foi rejeitada", "doacao"),
    SOLICITACAO_APROVADA("Sua solicitação foi aprovada", "solicitacao"),
    SOLICITACAO_REJEITADA("Sua solicitação foi rejeitada", "solicitacao"),
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
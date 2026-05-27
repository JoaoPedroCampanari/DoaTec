package com.doatec.model.inventory;

/**
 * Define os possíveis estados de conservação de um equipamento.
 */
public enum EstadoConservacao {

    NOVO("Novo"),
    EXCELENTE("Excelente estado"),
    BOM("Bom estado, pequenos desgastes"),
    REGULAR("Funcional, com desgaste visível");

    private final String descricao;

    EstadoConservacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
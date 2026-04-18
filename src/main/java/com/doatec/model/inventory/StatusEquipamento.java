package com.doatec.model.inventory;

/**
 * Define os possíveis status de um equipamento no sistema.
 */
public enum StatusEquipamento {

    DISPONIVEL("Disponível para atribuição"),
    RESERVADO("Reservado para um aluno"),
    ENTREGUE("Entregue ao aluno");

    private final String descricao;

    StatusEquipamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
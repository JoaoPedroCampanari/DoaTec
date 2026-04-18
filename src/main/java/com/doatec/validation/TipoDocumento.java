package com.doatec.validation;

/**
 * Enum para especificar o tipo de documento a ser validado.
 */
public enum TipoDocumento {
    CPF("CPF"),
    CNPJ("CNPJ"),
    AMBOS("CPF ou CNPJ");

    private final String descricao;

    TipoDocumento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
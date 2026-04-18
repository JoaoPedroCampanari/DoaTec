package com.doatec.model.account;

/**
 * Define o tipo/perfil da pessoa no sistema.
 * Representa QUEM é a pessoa, não o que ela pode fazer.
 */
public enum TipoPessoa {

    DOADOR_PF("Doador Pessoa Física"),
    DOADOR_PJ("Doador Pessoa Jurídica"),
    ALUNO("Aluno");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Infere o tipo de pessoa baseado no formato do documento.
     * CPF (11 dígitos) → DOADOR_PF
     * CNPJ (14 dígitos) → DOADOR_PJ
     * Outros → ALUNO (considera como RA)
     */
    public static TipoPessoa inferirPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Documento não pode ser vazio");
        }

        String docLimpo = documento.replaceAll("[^0-9]", "");

        if (docLimpo.length() == 11) {
            return DOADOR_PF;
        } else if (docLimpo.length() == 14) {
            return DOADOR_PJ;
        } else {
            return ALUNO;
        }
    }
}
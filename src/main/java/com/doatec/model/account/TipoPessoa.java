package com.doatec.model.account;

/**
 * Define o tipo/perfil da pessoa no sistema.
 * Representa QUEM é a pessoa, não o que ela pode fazer.
 *
 * Após a implementação da herança JPA, este enum é mantido
 * principalmente para referência e queries por tipo.
 */
public enum TipoPessoa {

    DOADOR_PF("Doador Pessoa Física", DoadorPF.class),
    DOADOR_PJ("Doador Pessoa Jurídica", DoadorPJ.class),
    ALUNO("Aluno", Aluno.class);

    private final String descricao;
    private final Class<? extends Pessoa> classe;

    TipoPessoa(String descricao, Class<? extends Pessoa> classe) {
        this.descricao = descricao;
        this.classe = classe;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna a classe JPA correspondente ao tipo de pessoa.
     * Útil para queries com TYPE() do JPA.
     */
    public Class<? extends Pessoa> getClasse() {
        return classe;
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
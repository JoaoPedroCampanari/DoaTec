package com.doatec.model.account;

/**
 * Define as roles/permissões da pessoa no sistema.
 * Representa O QUE a pessoa pode fazer, não quem ela é.
 */
public enum Role {

    USER("Usuário", "Acesso básico ao sistema"),
    ADMIN("Administrador", "Gestão de doações, solicitações, suporte e usuários"),
    SUPER_ADMIN("Super Admin", "Acesso total incluindo gestão de administradores");

    private final String nome;
    private final String descricao;

    Role(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
package com.doatec.dtos;

public class SolicitacaoDto {
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String ra;
    private String justificativa;
    private String preferenciaEquipamento;

    public SolicitacaoDto(String nome, String email, String senha, String telefone, String ra, String justificativa, String preferenciaEquipamento) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.ra = ra;
        this.justificativa = justificativa;
        this.preferenciaEquipamento = preferenciaEquipamento;
    }

    public SolicitacaoDto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getPreferenciaEquipamento() {
        return preferenciaEquipamento;
    }

    public void setPreferenciaEquipamento(String preferenciaEquipamento) {
        this.preferenciaEquipamento = preferenciaEquipamento;
    }
}

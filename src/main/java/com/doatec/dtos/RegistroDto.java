package com.doatec.dtos;

public class RegistroDto {

    private String tipoUsuario;
    private String nome;
    private String identidade;
    private String email;
    private String endereco;
    private String senha;

    public RegistroDto(String tipoUsuario, String nome, String identidade, String email, String endereco, String senha) {
        this.tipoUsuario = tipoUsuario;
        this.nome = nome;
        this.identidade = identidade;
        this.email = email;
        this.endereco = endereco;
        this.senha = senha;
    }

    public RegistroDto() {
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdentidade() {
        return identidade;
    }

    public void setIdentidade(String identidade) {
        this.identidade = identidade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}


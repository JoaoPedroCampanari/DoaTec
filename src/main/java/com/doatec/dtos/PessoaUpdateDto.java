package com.doatec.dtos;

public class PessoaUpdateDto {
    private String email;
    private String senha;
    private String endereco;
    private String telefone;

    public PessoaUpdateDto() {

    }

    public PessoaUpdateDto(String email, String senha, String endereco, String telefone) {
        this.email = email;
        this.senha = senha;
        this.endereco = endereco;
        this.telefone = telefone;
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
    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
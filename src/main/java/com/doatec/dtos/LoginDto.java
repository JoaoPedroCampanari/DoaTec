package com.doatec.dtos;

public class LoginDto {
    private String email;
    private String senha;

    public LoginDto(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public LoginDto() {
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
}
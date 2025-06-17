package com.doatec.dtos;

import com.doatec.model.account.TipoUsuario;


public class UserLoginResponseDto {
    private Integer id;
    private String nome;
    private String email;
    private String telefone;
    private String tipoUsuario;
    private String documento;

    public UserLoginResponseDto() {}

    public UserLoginResponseDto(Integer id, String nome, String email, String telefone, TipoUsuario tipoUsuario, String documento) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.tipoUsuario = tipoUsuario.name();
        this.documento = documento;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
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
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public String getTipoUsuario() {
        return tipoUsuario;
    }
    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    public String getDocumento() {
        return documento;
    }
    public void setDocumento(String documento) {
        this.documento = documento;
    }
}
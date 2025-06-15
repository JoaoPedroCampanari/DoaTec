package com.doatec.model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "pessoa_juridica")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class PessoaJuridica extends Pessoa{

    private String cnpj;

    public PessoaJuridica() {
    }

    public PessoaJuridica(String nome, String email, String senha, String endereco, String telefone, TipoUsuario tipo, String cnpj) {
        super(nome, email, senha, endereco, telefone, tipo);
        this.cnpj = cnpj;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}

package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pessoa_fisica")
public class PessoaFisica extends Pessoa{

    private String cpf;

    public PessoaFisica() {

    }

    public PessoaFisica(UUID id, String nome, String email, String senha, String endereco, String telefone, String cpf) {
        super(id, nome, email, senha, endereco, telefone);
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}


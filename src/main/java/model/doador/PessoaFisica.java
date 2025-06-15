package model.doador;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pessoa_fisica")
public class PessoaFisica extends Pessoa{

    private String cpf;

    public PessoaFisica() {

    }

    public PessoaFisica(String cpf) {
        this.cpf = cpf;
    }

    public PessoaFisica(UUID id, String nome, String email, String endereco, String cpf) {
        super(id, nome, email, endereco);
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}

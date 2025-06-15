package model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "pessoa_fisica")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class PessoaFisica extends Pessoa{

    private String cpf;

    public PessoaFisica() {

    }

    public PessoaFisica(String nome, String email, String senha, String endereco, String telefone, TipoUsuario tipo, String cpf) {
        super(nome, email, senha, endereco, telefone, tipo);
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}


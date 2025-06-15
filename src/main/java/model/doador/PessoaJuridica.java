package model.doador;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pessoa_juridica")
public class PessoaJuridica extends Pessoa{

    private String cnpj;

    public PessoaJuridica() {
    }

    public PessoaJuridica(String cnpj) {
        this.cnpj = cnpj;
    }

    public PessoaJuridica(UUID id, String nome, String email, String endereco, String senha, String cnpj) {
        super(id, nome, email, endereco, senha);
        this.cnpj = cnpj;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}

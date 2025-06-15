package model.doador;

import jakarta.persistence.Entity;

@Entity
public class PessoaJuridica extends Pessoa{

    private String cnpj;

    public PessoaJuridica() {
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}

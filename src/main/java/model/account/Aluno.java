package model.account;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Aluno extends Pessoa {

    private String ra;

    public Aluno() {
    }

    public Aluno(String nome, String email, String senha, String endereco, String telefone, TipoUsuario tipo, String ra) {
        super(nome, email, senha, endereco, telefone, tipo);
        this.ra = ra;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }
}

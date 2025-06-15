package model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "aluno")
public class Aluno extends Pessoa{

    private String ra;

    public Aluno() {
    }

    public Aluno(UUID id, String nome, String email, String senha, String endereco, String telefone, String ra) {
        super(id, nome, email, senha, endereco, telefone);
        this.ra = ra;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }
}

package model.account;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Aluno extends Pessoa {

    private String ra;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    private String preferenciaEquipamento;

    public Aluno() {
    }

    public Aluno(String nome, String email, String senha, String endereco, String telefone, TipoUsuario tipo, String ra) {
        super(nome, email, senha, endereco, telefone, tipo);
        this.ra = ra;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getPreferenciaEquipamento() {
        return preferenciaEquipamento;
    }

    public void setPreferenciaEquipamento(String preferenciaEquipamento) {
        this.preferenciaEquipamento = preferenciaEquipamento;
    }

    public void alterarJustificativaPreferencia(String texto, String preferenciaEquipamento){
        this.justificativa = texto;
        this.preferenciaEquipamento = preferenciaEquipamento;
    }
}

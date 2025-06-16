package com.doatec.model.account;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Aluno extends Pessoa {

    //Documento de matricula
    private String ra;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoHardware> solicitacoes;

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

    public List<SolicitacaoHardware> getSolicitacoes() {
        return solicitacoes;
    }

    public void setSolicitacoes(List<SolicitacaoHardware> solicitacoes) {
        this.solicitacoes = solicitacoes;
    }

    public void setPreferenciaEquipamento(String preferenciaEquipamento) {
        this.preferenciaEquipamento = preferenciaEquipamento;
    }


    public void alterarJustificativaPreferencia(String texto, String preferenciaEquipamento){
        this.justificativa = texto;
        this.preferenciaEquipamento = preferenciaEquipamento;
    }
}

package com.doatec.model.solicitacao;

import com.doatec.model.account.Pessoa;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "solicitacao_hardware")
public class SolicitacaoHardware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Gerado pelo banco de dados
    private Integer id; // Alterado de String para Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Pessoa aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.EM_ANALISE;

    private LocalDate dataSolicitacao = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    private String preferenciaEquipamento;

    // Construtor sem ID
    public SolicitacaoHardware(Pessoa aluno, StatusSolicitacao status, LocalDate dataSolicitacao, String justificativa, String preferenciaEquipamento) {
        this.aluno = aluno;
        this.status = status;
        this.dataSolicitacao = dataSolicitacao;
        this.justificativa = justificativa;
        this.preferenciaEquipamento = preferenciaEquipamento;
    }

    public SolicitacaoHardware() {
    }

    // Construtor simples sem ID, status e data (defaults são definidos no construtor padrão ou setter)
    public SolicitacaoHardware(Pessoa aluno, String justificativa, String preferenciaEquipamento) {
        this.aluno = aluno;
        this.justificativa = justificativa;
        this.preferenciaEquipamento = preferenciaEquipamento;
    }

    public Integer getId() { // Alterado de String para Integer
        return id;
    }

    public void setId(Integer id) { // Alterado de String para Integer
        this.id = id;
    }

    public Pessoa getAluno() {
        return aluno;
    }

    public void setAluno(Pessoa aluno) {
        this.aluno = aluno;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getPreferenciaEquipamento() {
        return preferenciaEquipamento;
    }

    public void setPreferenciaEquipamento(String preferenciaEquipamento) {
        this.preferenciaEquipamento = preferenciaEquipamento;
    }
}
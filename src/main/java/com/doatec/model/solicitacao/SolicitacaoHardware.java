package com.doatec.model.solicitacao;

import com.doatec.model.account.Aluno;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "solicitacao_hardware")
public class SolicitacaoHardware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.EM_ANALISE;

    private LocalDate dataSolicitacao = LocalDate.now();

    public SolicitacaoHardware(Aluno aluno, StatusSolicitacao status, LocalDate dataSolicitacao) {
        this.aluno = aluno;
        this.status = status;
        this.dataSolicitacao = dataSolicitacao;
    }

    public SolicitacaoHardware() {
    }

    public SolicitacaoHardware(Aluno aluno) {
        this.aluno = aluno;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
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
}

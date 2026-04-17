package com.doatec.model.solicitacao;

import com.doatec.model.account.Pessoa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "solicitacao_hardware")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"aluno"})
public class SolicitacaoHardware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Pessoa aluno;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.EM_ANALISE;

    @Builder.Default
    private LocalDate dataSolicitacao = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    private String preferenciaEquipamento;
}
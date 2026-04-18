package com.doatec.model.inventory;

import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entidade que representa um equipamento no inventário.
 * Equipamentos são criados a partir de doações aprovadas e podem
 * ser atribuídos a alunos através de solicitações.
 */
@Entity
@Table(name = "equipamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"itemOrigem", "solicitacaoDestino", "alunoDestinatario"})
@ToString(exclude = {"itemOrigem", "solicitacaoDestino", "alunoDestinatario"})
@SQLDelete(sql = "UPDATE equipamento SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusEquipamento status = StatusEquipamento.DISPONIVEL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConservacao estadoConservacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_origem_id")
    private ItemDoado itemOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_destino_id")
    private SolicitacaoHardware solicitacaoDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_destinatario_id")
    private Pessoa alunoDestinatario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEntradaInventario;

    private LocalDateTime dataAtribuicao;

    private LocalDateTime dataEntrega;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        dataEntradaInventario = LocalDateTime.now();
    }

    /**
     * Verifica se o equipamento está disponível para atribuição.
     */
    public boolean isDisponivel() {
        return status == StatusEquipamento.DISPONIVEL;
    }

    /**
     * Verifica se o equipamento está reservado.
     */
    public boolean isReservado() {
        return status == StatusEquipamento.RESERVADO;
    }

    /**
     * Verifica se o equipamento foi entregue.
     */
    public boolean isEntregue() {
        return status == StatusEquipamento.ENTREGUE;
    }
}
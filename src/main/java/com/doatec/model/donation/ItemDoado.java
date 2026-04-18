package com.doatec.model.donation;

import com.doatec.model.inventory.Equipamento;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_doado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"doacao", "equipamentoGerado"})
public class ItemDoado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doacao_id", nullable = false)
    private Doacao doacao;

    @Column(nullable = false)
    private String tipoItem;

    @Column(nullable = false)
    private String descricao;

    /**
     * Equipamento gerado a partir deste item quando a doação é aprovada.
     * Relacionamento OneToOne opcional.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_gerado_id")
    private Equipamento equipamentoGerado;
}
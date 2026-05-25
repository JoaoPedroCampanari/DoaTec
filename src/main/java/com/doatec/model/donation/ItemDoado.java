package com.doatec.model.donation;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um item declarado pelo doador no formulário de doação online.
 *
 * Nota: o vínculo com equipamentos no inventário foi invertido — agora é
 * {@code Equipamento.itemOrigem} (lado dono, N:1) ou diretamente
 * {@code Equipamento.doacao} para cadastros manuais. O campo
 * {@code equipamentoGerado} foi removido pois não suportava o caso
 * "1 item declarado → vários equipamentos aproveitados" (ex.: notebook
 * cuja apenas HD, RAM e bateria são reaproveitados).
 */
@Entity
@Table(name = "item_doado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"doacao"})
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
}
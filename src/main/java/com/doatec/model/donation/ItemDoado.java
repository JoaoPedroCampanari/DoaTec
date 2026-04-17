package com.doatec.model.donation;

import jakarta.persistence.*;
import lombok.*;

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
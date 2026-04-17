package com.doatec.model.donation;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"itens"})
@ToString(exclude = {"doador", "itens"})
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Pessoa doador;

    @Builder.Default
    @OneToMany(mappedBy = "doacao", cascade = CascadeType.ALL)
    private List<ItemDoado> itens = new ArrayList<>();

    @Builder.Default
    private LocalDate dataDoacao = LocalDate.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatusDoacao status = StatusDoacao.EM_ANALISE;

    @Enumerated(EnumType.STRING)
    private PreferenciaEntrega preferenciaEntrega;
}
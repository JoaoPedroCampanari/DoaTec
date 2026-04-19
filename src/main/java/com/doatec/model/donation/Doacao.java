package com.doatec.model.donation;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@SQLDelete(sql = "UPDATE doacao SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
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
    private StatusDoacao status = StatusDoacao.EM_TRIAGEM;

    @Enumerated(EnumType.STRING)
    private PreferenciaEntrega preferenciaEntrega;

    @Column(columnDefinition = "TEXT")
    private String descricaoGeral;

    @Column(length = 500)
    private String urlFoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_avaliador_id")
    private Pessoa adminAvaliador;

    private LocalDateTime dataAvaliacao;

    @Column(columnDefinition = "TEXT")
    private String observacaoAdmin;

    private LocalDateTime deletedAt;
}
package com.doatec.model.suporte;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "suporte_formulario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"autor", "adminResponsavel"})
@SQLDelete(sql = "UPDATE suporte_formulario SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SuporteFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Pessoa autor;

    @Column(nullable = false)
    private String assunto;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusSuporte status = StatusSuporte.ABERTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_responsavel_id")
    private Pessoa adminResponsavel;

    @Column(columnDefinition = "TEXT")
    private String resposta;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataResolucao;

    private LocalDateTime deletedAt;
}
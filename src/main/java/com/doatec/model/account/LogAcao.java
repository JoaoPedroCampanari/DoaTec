package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_acao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Pessoa admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcaoTipo acao;

    @Column(nullable = false)
    private String entidade;

    @Column(nullable = false)
    private Integer entidadeId;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Builder.Default
    private LocalDateTime dataAcao = LocalDateTime.now();
}
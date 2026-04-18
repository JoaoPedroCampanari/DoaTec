package com.doatec.model.notification;

import com.doatec.model.account.Pessoa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma notificação no sistema.
 * Notificações são criadas para informar usuários sobre eventos
 * relevantes (aprovação de doação, solicitação, etc.).
 */
@Entity
@Table(name = "notificacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"destinatario"})
@ToString(exclude = {"destinatario"})
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean lida = false;

    private LocalDateTime dataLeitura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Pessoa destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    /**
     * ID da entidade relacionada (doação, solicitação, etc.).
     */
    private Integer entidadeRelacionadaId;

    /**
     * Tipo da entidade relacionada ("DOACAO", "SOLICITACAO", "EQUIPAMENTO").
     */
    private String entidadeRelacionadaTipo;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}
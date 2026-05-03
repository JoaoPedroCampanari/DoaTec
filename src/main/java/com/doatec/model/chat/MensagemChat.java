package com.doatec.model.chat;

import com.doatec.model.account.Pessoa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensagemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Pessoa remetente;

    @Column(name = "referencia_id", nullable = false)
    private Integer referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContextoChat contexto;

    @PrePersist
    protected void onCreate() {
        dataEnvio = LocalDateTime.now();
    }
}

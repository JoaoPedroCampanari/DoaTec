package com.doatec.model.suporte;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suporte_formulario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"autor"})
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

    @Column(nullable = false)
    private String status;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
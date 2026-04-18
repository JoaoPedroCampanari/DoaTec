package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "pessoa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"senha"})
@ToString(exclude = {"senha"})
@SQLDelete(sql = "UPDATE pessoa SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String endereco;
    private String telefone;

    @Column(unique = true)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPessoa tipoPessoa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se a pessoa tem uma role específica.
     */
    public boolean hasRole(Role role) {
        return this.role == role;
    }

    /**
     * Verifica se a pessoa é administradora.
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Verifica se a pessoa é doador (PF ou PJ).
     */
    public boolean isDoador() {
        return this.tipoPessoa == TipoPessoa.DOADOR_PF || this.tipoPessoa == TipoPessoa.DOADOR_PJ;
    }
}
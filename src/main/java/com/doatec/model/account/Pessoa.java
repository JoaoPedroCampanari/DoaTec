package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entidade base abstrata para representar pessoas no sistema.
 * Utiliza herança JPA com estratégia JOINED para separar tipos específicos:
 * - Aluno: beneficiários que solicitam equipamentos
 * - DoadorPF: doadores pessoa física
 * - DoadorPJ: doadores pessoa jurídica (empresas)
 *
 * Usa @SuperBuilder para suportar herança com Builder pattern.
 */
@Entity
@Table(name = "pessoa")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(exclude = {"senha"})
@ToString(exclude = {"senha"})
@SQLDelete(sql = "UPDATE pessoa SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String endereco; // CEP

    private String logradouro;

    private String numero;

    private String bairro;

    private String cidade;

    private String estado;

    private String telefone;

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
     * Retorna o documento de identificação da pessoa.
     * Implementado pelas subclasses: RA para Aluno, CPF para DoadorPF, CNPJ para DoadorPJ.
     */
    public abstract String getDocumento();

    /**
     * Retorna o tipo de pessoa como string.
     * Usado para serialização e lógica de negócio.
     */
    public abstract String getTipoPessoa();

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
        return this instanceof DoadorPF || this instanceof DoadorPJ;
    }

    /**
     * Verifica se a pessoa é aluno (beneficiário).
     */
    public boolean isAluno() {
        return this instanceof Aluno;
    }
}
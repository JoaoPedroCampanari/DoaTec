package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade representando um aluno (beneficiário).
 * Alunos podem solicitar equipamentos disponibilizados pelos doadores.
 *
 * Usa @SuperBuilder para herdar o builder de Pessoa.
 */
@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"senha"})
@ToString(callSuper = true, exclude = {"senha"})
@SQLDelete(sql = "UPDATE pessoa SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Aluno extends Pessoa {

    @Column(name = "ra", unique = true, nullable = false)
    private String ra;

    @Override
    public String getDocumento() {
        return this.ra;
    }

    @Override
    public String getTipoPessoa() {
        return "ALUNO";
    }
}
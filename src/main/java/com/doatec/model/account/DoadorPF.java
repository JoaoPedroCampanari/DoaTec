package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade representando um doador pessoa física.
 * Doadores podem registrar doações de equipamentos.
 *
 * Usa @SuperBuilder para herdar o builder de Pessoa.
 */
@Entity
@Table(name = "doador_pf")
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
public class DoadorPF extends Pessoa {

    @Column(name = "cpf", unique = true, nullable = false)
    private String cpf;

    @Override
    public String getDocumento() {
        return this.cpf;
    }

    @Override
    public String getTipoPessoa() {
        return "DOADOR_PF";
    }
}
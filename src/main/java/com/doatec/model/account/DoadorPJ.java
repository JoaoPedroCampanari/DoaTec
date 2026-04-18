package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade representando um doador pessoa jurídica (empresa).
 * Doadores podem registrar doações de equipamentos.
 *
 * Usa @SuperBuilder para herdar o builder de Pessoa.
 */
@Entity
@Table(name = "doador_pj")
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
public class DoadorPJ extends Pessoa {

    @Column(name = "cnpj", unique = true, nullable = false)
    private String cnpj;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Override
    public String getDocumento() {
        return this.cnpj;
    }

    @Override
    public String getTipoPessoa() {
        return "DOADOR_PJ";
    }
}
package com.doatec.model.account;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pessoa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"senha"})
@ToString(exclude = {"senha"})
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private String endereco;
    private String telefone;

    @Column(unique = true)
    private String documento;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;
}
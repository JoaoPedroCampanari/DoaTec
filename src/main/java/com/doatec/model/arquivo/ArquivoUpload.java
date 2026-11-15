package com.doatec.model.arquivo;

import com.doatec.model.account.Pessoa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "arquivo_upload")
@Getter
@NoArgsConstructor
public class ArquivoUpload {
    @Id
    @Column(length = 128)
    private String nome;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Pessoa proprietario;
    @Column(nullable = false, length = 64)
    private String contentType;
    @Column(length = 200)
    private String nomeOriginal;
    public ArquivoUpload(String nome, Pessoa proprietario, String contentType, String nomeOriginal) {
        this(nome, proprietario, contentType);
        this.nomeOriginal = nomeOriginal;
    }
    public ArquivoUpload(String nome, Pessoa proprietario, String contentType) {
        this.nome = nome; this.proprietario = proprietario; this.contentType = contentType;
    }
}

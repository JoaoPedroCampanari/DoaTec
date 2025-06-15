package com.doatec.model.suporte;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suporte_formulario")
public class SuporteFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Pessoa autor;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String assunto;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private String status;

    private LocalDateTime dataCriacao = LocalDateTime.now();


    public SuporteFormulario() {
    }

    public SuporteFormulario(Pessoa autor, String nome, String email, String assunto, String mensagem, String status, LocalDateTime dataCriacao) {
        this.autor = autor;
        this.nome = nome;
        this.email = email;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Pessoa getAutor() {
        return autor;
    }

    public void setAutor(Pessoa autor) {
        this.autor = autor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}

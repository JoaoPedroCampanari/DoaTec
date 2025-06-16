package com.doatec.model.suporte;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import java.time.LocalDateTime;

@Entity
@Table(name = "suporte_formulario")
public class SuporteFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Gerado pelo banco de dados
    private Integer id; // Alterado de String para Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Pessoa autor;

    @Column(nullable = false)
    private String assunto;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private String status;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public SuporteFormulario() {
    }

    // Construtor sem ID
    public SuporteFormulario(Pessoa autor, String assunto, String mensagem, String status, LocalDateTime dataCriacao) {
        this.autor = autor;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public Integer getId() { // Alterado de String para Integer
        return id;
    }

    public void setId(Integer id) { // Alterado de String para Integer
        this.id = id;
    }

    public Pessoa getAutor() {
        return autor;
    }

    public void setAutor(Pessoa autor) {
        this.autor = autor;
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
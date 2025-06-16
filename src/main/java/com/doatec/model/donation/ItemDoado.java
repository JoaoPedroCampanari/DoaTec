package com.doatec.model.donation;

import jakarta.persistence.*;

@Entity
@Table(name = "item_doado")
public class ItemDoado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Gerado pelo banco de dados
    private Integer id; // Alterado de String para Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doacao_id", nullable = false)
    private Doacao doacao;

    @Column(nullable = false)
    private String tipoItem;

    @Column(nullable = false)
    private String descricao;

    public ItemDoado() {
    }

    // Construtor sem ID
    public ItemDoado(Doacao doacao, String tipoItem, String descricao) {
        this.doacao = doacao;
        this.tipoItem = tipoItem;
        this.descricao = descricao;
    }

    public Integer getId() { // Alterado de String para Integer
        return id;
    }

    public void setId(Integer id) { // Alterado de String para Integer
        this.id = id;
    }

    public Doacao getDoacao() {
        return doacao;
    }

    public void setDoacao(Doacao doacao) {
        this.doacao = doacao;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
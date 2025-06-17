package com.doatec.model.donation;

import jakarta.persistence.*;
import com.doatec.model.account.Pessoa;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doacao")
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Pessoa doador;

    @OneToMany(mappedBy = "doacao", cascade = CascadeType.ALL)
    private List<ItemDoado> itens = new ArrayList<>();

    private LocalDate dataDoacao = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatusDoacao status = StatusDoacao.EM_ANALISE;

    @Enumerated(EnumType.STRING)
    private PreferenciaEntrega preferenciaEntrega;

    public Doacao() {
    }

    public Doacao(Pessoa doador, StatusDoacao status, PreferenciaEntrega preferenciaEntrega) {
        this.doador = doador;
        this.status = status;
        this.preferenciaEntrega = preferenciaEntrega;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pessoa getDoador() {
        return doador;
    }

    public void setDoador(Pessoa doador) {
        this.doador = doador;
    }

    public List<ItemDoado> getItens() {
        return itens;
    }

    public void setItens(List<ItemDoado> itens) {
        this.itens = itens;
    }

    public LocalDate getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDate dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public PreferenciaEntrega getPreferenciaEntrega() {
        return preferenciaEntrega;
    }

    public void setPreferenciaEntrega(PreferenciaEntrega preferenciaEntrega) {
        this.preferenciaEntrega = preferenciaEntrega;
    }
}
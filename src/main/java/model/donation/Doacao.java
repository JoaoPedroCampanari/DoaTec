package model.donation;

import jakarta.persistence.*;
import model.account.Pessoa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "doacao")
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Pessoa doador;

    @OneToMany(mappedBy = "doacao", cascade = CascadeType.ALL)
    private List<ItemDoado> itens = new ArrayList<>();

    private LocalDate dataDoacao = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatusDoacao status = StatusDoacao.EM_ANALISE;


    public Doacao() {
    }

    public Doacao(Pessoa doador, StatusDoacao status) {
        this.doador = doador;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
}
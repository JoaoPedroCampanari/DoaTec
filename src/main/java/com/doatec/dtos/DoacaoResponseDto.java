package com.doatec.dtos;

import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DoacaoResponseDto {
    private Integer id;
    private LocalDate dataDoacao;
    private String status;
    private String preferenciaEntrega;
    private List<ItemDoadoDto> itens;

    public DoacaoResponseDto(Doacao doacao) {
        this.id = doacao.getId();
        this.dataDoacao = doacao.getDataDoacao();
        this.status = doacao.getStatus().name();
        this.preferenciaEntrega = doacao.getPreferenciaEntrega().name();
        this.itens = doacao.getItens().stream()
                .map(item -> new ItemDoadoDto(item.getTipoItem(), item.getDescricao()))
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDate dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreferenciaEntrega() {
        return preferenciaEntrega;
    }

    public void setPreferenciaEntrega(String preferenciaEntrega) {
        this.preferenciaEntrega = preferenciaEntrega;
    }

    public List<ItemDoadoDto> getItens() {
        return itens;
    }

    public void setItens(List<ItemDoadoDto> itens) {
        this.itens = itens;
    }
}
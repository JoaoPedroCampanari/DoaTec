package com.doatec.dtos;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import java.time.LocalDate;

public class SolicitacaoResponseDto {

    private Integer id;
    private LocalDate dataSolicitacao;
    private String status;
    private String justificativa;
    private String preferenciaEquipamento;

    public SolicitacaoResponseDto(SolicitacaoHardware solicitacao) {
        this.id = solicitacao.getId();
        this.dataSolicitacao = solicitacao.getDataSolicitacao();
        this.status = solicitacao.getStatus().name();
        this.justificativa = solicitacao.getJustificativa();
        this.preferenciaEquipamento = solicitacao.getPreferenciaEquipamento();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getPreferenciaEquipamento() {
        return preferenciaEquipamento;
    }

    public void setPreferenciaEquipamento(String preferenciaEquipamento) {
        this.preferenciaEquipamento = preferenciaEquipamento;
    }
}
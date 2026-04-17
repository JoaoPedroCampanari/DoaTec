package com.doatec.dto.response;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SolicitacaoResponse(
    Integer id,
    LocalDate dataSolicitacao,
    String status,
    String justificativa,
    String preferenciaEquipamento
) {
    public static SolicitacaoResponse from(SolicitacaoHardware solicitacao) {
        return SolicitacaoResponse.builder()
                .id(solicitacao.getId())
                .dataSolicitacao(solicitacao.getDataSolicitacao())
                .status(solicitacao.getStatus().name())
                .justificativa(solicitacao.getJustificativa())
                .preferenciaEquipamento(solicitacao.getPreferenciaEquipamento())
                .build();
    }
}
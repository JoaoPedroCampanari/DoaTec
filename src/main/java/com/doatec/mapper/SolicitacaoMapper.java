package com.doatec.mapper;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SolicitacaoMapper {

    public static SolicitacaoHardware toSolicitacao(SolicitacaoRequest request, Pessoa aluno) {
        return SolicitacaoHardware.builder()
                .aluno(aluno)
                .justificativa(request.justificativa())
                .preferenciaEquipamento(request.preferenciaEquipamento())
                .build();
    }

    public static SolicitacaoResponse toResponse(SolicitacaoHardware solicitacao) {
        return SolicitacaoResponse.from(solicitacao);
    }
}
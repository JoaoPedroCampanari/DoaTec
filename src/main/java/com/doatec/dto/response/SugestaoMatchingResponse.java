package com.doatec.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * DTO de resposta para sugestões de matching entre
 * solicitações de alunos e equipamentos disponíveis.
 */
@Builder
public record SugestaoMatchingResponse(
    Integer solicitacaoId,
    String alunoNome,
    String alunoEmail,
    String preferenciaEquipamento,
    List<MatchEquipamentoResponse> equipamentosCompativeis
) {
    /**
     * DTO para representar um equipamento compatível com a solicitação.
     */
    @Builder
    public record MatchEquipamentoResponse(
        Integer equipamentoId,
        String tipo,
        String descricao,
        String estadoConservacao,
        Integer scoreCompatibilidade
    ) {}
}
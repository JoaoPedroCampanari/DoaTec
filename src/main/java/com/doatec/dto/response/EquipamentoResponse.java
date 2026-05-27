package com.doatec.dto.response;

import com.doatec.model.inventory.Equipamento;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO de resposta para dados de equipamento.
 */
@Builder
public record EquipamentoResponse(
    Integer id,
    String tipo,
    String descricao,
    String status,
    String estadoConservacao,
    /** Nome do doador originário (preferindo vínculo direto com Doacao; fallback para ItemOrigem). */
    String doadorOrigem,
    /** ID da doação vinculada, se houver. Útil para o frontend exibir/filtrar por doação. */
    Integer doacaoId,
    Integer solicitacaoDestinoId,
    Integer alunoDestinatarioId,
    LocalDateTime dataEntradaInventario,
    LocalDateTime dataAtribuicao
) {
    public static EquipamentoResponse from(Equipamento equipamento) {
        // doação vinculada diretamente tem precedência sobre o item de origem antigo
        Integer doacaoId = null;
        String doadorNome = null;
        if (equipamento.getDoacao() != null) {
            doacaoId = equipamento.getDoacao().getId();
            if (equipamento.getDoacao().getDoador() != null) {
                doadorNome = equipamento.getDoacao().getDoador().getNome();
            }
        } else if (equipamento.getItemOrigem() != null && equipamento.getItemOrigem().getDoacao() != null) {
            doacaoId = equipamento.getItemOrigem().getDoacao().getId();
            doadorNome = equipamento.getItemOrigem().getDoacao().getDoador().getNome();
        }

        return EquipamentoResponse.builder()
                .id(equipamento.getId())
                .tipo(equipamento.getTipo())
                .descricao(equipamento.getDescricao())
                .status(equipamento.getStatus().name())
                .estadoConservacao(equipamento.getEstadoConservacao() != null
                        ? equipamento.getEstadoConservacao().name() : null)
                .doadorOrigem(doadorNome)
                .doacaoId(doacaoId)
                .solicitacaoDestinoId(equipamento.getSolicitacaoDestino() != null
                        ? equipamento.getSolicitacaoDestino().getId() : null)
                .alunoDestinatarioId(equipamento.getAlunoDestinatario() != null
                        ? equipamento.getAlunoDestinatario().getId() : null)
                .dataEntradaInventario(equipamento.getDataEntradaInventario())
                .dataAtribuicao(equipamento.getDataAtribuicao())
                .build();
    }
}
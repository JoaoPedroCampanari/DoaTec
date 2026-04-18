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
    String doadorOrigem,
    Integer solicitacaoDestinoId,
    Integer alunoDestinatarioId,
    LocalDateTime dataEntradaInventario,
    LocalDateTime dataAtribuicao
) {
    public static EquipamentoResponse from(Equipamento equipamento) {
        String doadorNome = null;
        if (equipamento.getItemOrigem() != null && equipamento.getItemOrigem().getDoacao() != null) {
            doadorNome = equipamento.getItemOrigem().getDoacao().getDoador().getNome();
        }

        return EquipamentoResponse.builder()
                .id(equipamento.getId())
                .tipo(equipamento.getTipo())
                .descricao(equipamento.getDescricao())
                .status(equipamento.getStatus().name())
                .estadoConservacao(equipamento.getEstadoConservacao() != null
                        ? equipamento.getEstadoConservacao().getDescricao() : null)
                .doadorOrigem(doadorNome)
                .solicitacaoDestinoId(equipamento.getSolicitacaoDestino() != null
                        ? equipamento.getSolicitacaoDestino().getId() : null)
                .alunoDestinatarioId(equipamento.getAlunoDestinatario() != null
                        ? equipamento.getAlunoDestinatario().getId() : null)
                .dataEntradaInventario(equipamento.getDataEntradaInventario())
                .dataAtribuicao(equipamento.getDataAtribuicao())
                .build();
    }
}
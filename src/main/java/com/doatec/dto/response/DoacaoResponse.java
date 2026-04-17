package com.doatec.dto.response;

import com.doatec.model.donation.Doacao;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DoacaoResponse(
    Integer id,
    LocalDate dataDoacao,
    String status,
    String preferenciaEntrega,
    List<ItemDoadoResponse> itens
) {
    public static DoacaoResponse from(Doacao doacao) {
        return DoacaoResponse.builder()
                .id(doacao.getId())
                .dataDoacao(doacao.getDataDoacao())
                .status(doacao.getStatus().name())
                .preferenciaEntrega(doacao.getPreferenciaEntrega() != null
                        ? doacao.getPreferenciaEntrega().name() : null)
                .itens(doacao.getItens().stream()
                        .map(ItemDoadoResponse::from)
                        .toList())
                .build();
    }
}
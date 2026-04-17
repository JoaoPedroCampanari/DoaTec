package com.doatec.dto.response;

import com.doatec.model.donation.ItemDoado;
import lombok.Builder;

@Builder
public record ItemDoadoResponse(
    String tipoItem,
    String descricao
) {
    public static ItemDoadoResponse from(ItemDoado item) {
        return ItemDoadoResponse.builder()
                .tipoItem(item.getTipoItem())
                .descricao(item.getDescricao())
                .build();
    }
}
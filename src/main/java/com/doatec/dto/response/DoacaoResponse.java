package com.doatec.dto.response;

import com.doatec.model.donation.Doacao;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DoacaoResponse(
    Integer id,
    String doadorNome,
    String doadorEmail,
    LocalDate dataDoacao,
    String status,
    String preferenciaEntrega,
    String observacaoAdmin,
    String adminAvaliadorNome,
    LocalDateTime dataAvaliacao,
    List<ItemDoadoResponse> itens
) {
    public static DoacaoResponse from(Doacao doacao) {
        return DoacaoResponse.builder()
                .id(doacao.getId())
                .doadorNome(doacao.getDoador() != null ? doacao.getDoador().getNome() : null)
                .doadorEmail(doacao.getDoador() != null ? doacao.getDoador().getEmail() : null)
                .dataDoacao(doacao.getDataDoacao())
                .status(doacao.getStatus().name())
                .preferenciaEntrega(doacao.getPreferenciaEntrega() != null
                        ? doacao.getPreferenciaEntrega().name() : null)
                .observacaoAdmin(doacao.getObservacaoAdmin())
                .adminAvaliadorNome(doacao.getAdminAvaliador() != null ? doacao.getAdminAvaliador().getNome() : null)
                .dataAvaliacao(doacao.getDataAvaliacao())
                .itens(doacao.getItens().stream()
                        .map(ItemDoadoResponse::from)
                        .toList())
                .build();
    }
}
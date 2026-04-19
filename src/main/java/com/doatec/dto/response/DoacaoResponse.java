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
    String descricaoGeral,
    String urlFoto,
    String observacaoAdmin,
    String adminAvaliadorNome,
    LocalDateTime dataAvaliacao
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
                .descricaoGeral(doacao.getDescricaoGeral())
                .urlFoto(doacao.getUrlFoto())
                .observacaoAdmin(doacao.getObservacaoAdmin())
                .adminAvaliadorNome(doacao.getAdminAvaliador() != null ? doacao.getAdminAvaliador().getNome() : null)
                .dataAvaliacao(doacao.getDataAvaliacao())
                .build();
    }
}
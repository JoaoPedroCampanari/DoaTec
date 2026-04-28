package com.doatec.dto.response;

import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
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
    LocalDateTime dataAvaliacao,
    List<ItemDoadoResumo> itens
) {
    public static DoacaoResponse from(Doacao doacao) {
        List<ItemDoadoResumo> itensResumo = doacao.getItens() != null
                ? doacao.getItens().stream().map(ItemDoadoResumo::from).toList()
                : List.of();

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
                .itens(itensResumo)
                .build();
    }

    @Builder
    public record ItemDoadoResumo(
        String tipo,
        String descricao
    ) {
        public static ItemDoadoResumo from(ItemDoado item) {
            return ItemDoadoResumo.builder()
                    .tipo(item.getTipoItem())
                    .descricao(item.getDescricao())
                    .build();
        }
    }
}
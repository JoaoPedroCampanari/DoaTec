package com.doatec.mapper;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.donation.StatusDoacao;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DoacaoMapper {

    public static Doacao toDoacao(DoacaoRequest request, Pessoa doador) {
        Doacao doacao = Doacao.builder()
                .doador(doador)
                .status(StatusDoacao.EM_ANALISE)
                .preferenciaEntrega(request.preferenciaEntrega())
                .build();

        ItemDoado item = ItemDoado.builder()
                .doacao(doacao)
                .tipoItem(request.tipoItem())
                .descricao(request.descricaoItem())
                .build();

        doacao.getItens().add(item);

        return doacao;
    }

    public static DoacaoResponse toResponse(Doacao doacao) {
        return DoacaoResponse.from(doacao);
    }
}
package com.doatec.mapper;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DoacaoMapper {

    public static Doacao toDoacao(DoacaoRequest request, Pessoa doador) {
        return Doacao.builder()
                .doador(doador)
                .status(StatusDoacao.EM_TRIAGEM)
                .preferenciaEntrega(request.preferenciaEntrega())
                .descricaoGeral(request.descricaoGeral())
                .urlFoto(request.urlFoto())
                .build();
    }

    public static DoacaoResponse toResponse(Doacao doacao) {
        return DoacaoResponse.from(doacao);
    }
}
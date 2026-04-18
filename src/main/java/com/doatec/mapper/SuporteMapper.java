package com.doatec.mapper;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class SuporteMapper {

    public static SuporteFormulario toSuporteFormulario(SuporteFormularioRequest request, Pessoa autor) {
        return SuporteFormulario.builder()
                .autor(autor)
                .assunto(request.assunto())
                .mensagem(request.mensagem())
                .status(StatusSuporte.ABERTO)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    public static SuporteResponse toResponse(SuporteFormulario suporte) {
        return SuporteResponse.from(suporte);
    }
}
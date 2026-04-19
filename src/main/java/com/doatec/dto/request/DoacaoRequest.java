package com.doatec.dto.request;

import com.doatec.model.donation.PreferenciaEntrega;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DoacaoRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    String tipoDocumento,
    String numeroDocumento,

    @NotBlank(message = "O campo email é obrigatório")
    String email,

    String telefone,

    @NotBlank(message = "O campo descrição geral é obrigatório")
    String descricaoGeral,

    String urlFoto,

    PreferenciaEntrega preferenciaEntrega
) {}
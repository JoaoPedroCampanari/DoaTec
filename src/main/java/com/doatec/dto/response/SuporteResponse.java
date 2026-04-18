package com.doatec.dto.response;

import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SuporteResponse(
    Integer id,
    String autorNome,
    String autorEmail,
    String assunto,
    String mensagem,
    String status,
    String resposta,
    String adminResponsavelNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataResolucao
) {
    public static SuporteResponse from(SuporteFormulario suporte) {
        return SuporteResponse.builder()
                .id(suporte.getId())
                .autorNome(suporte.getAutor() != null ? suporte.getAutor().getNome() : null)
                .autorEmail(suporte.getAutor() != null ? suporte.getAutor().getEmail() : null)
                .assunto(suporte.getAssunto())
                .mensagem(suporte.getMensagem())
                .status(suporte.getStatus().name())
                .resposta(suporte.getResposta())
                .adminResponsavelNome(suporte.getAdminResponsavel() != null ? suporte.getAdminResponsavel().getNome() : null)
                .dataCriacao(suporte.getDataCriacao())
                .dataResolucao(suporte.getDataResolucao())
                .build();
    }
}
package com.doatec.dto.response;

import com.doatec.model.notification.Notificacao;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO de resposta para dados de notificação.
 */
@Builder
public record NotificacaoResponse(
    Integer id,
    String titulo,
    String mensagem,
    LocalDateTime dataCriacao,
    Boolean lida,
    LocalDateTime dataLeitura,
    String tipo,
    Integer entidadeRelacionadaId,
    String entidadeRelacionadaTipo
) {
    public static NotificacaoResponse from(Notificacao notificacao) {
        return NotificacaoResponse.builder()
                .id(notificacao.getId())
                .titulo(notificacao.getTitulo())
                .mensagem(notificacao.getMensagem())
                .dataCriacao(notificacao.getDataCriacao())
                .lida(notificacao.getLida())
                .dataLeitura(notificacao.getDataLeitura())
                .tipo(notificacao.getTipo() != null ? notificacao.getTipo().name() : null)
                .entidadeRelacionadaId(notificacao.getEntidadeRelacionadaId())
                .entidadeRelacionadaTipo(notificacao.getEntidadeRelacionadaTipo())
                .build();
    }
}
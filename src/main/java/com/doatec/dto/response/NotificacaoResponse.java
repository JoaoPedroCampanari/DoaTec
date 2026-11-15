package com.doatec.dto.response;

import com.doatec.model.notification.Notificacao;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * DTO de resposta para dados de notificação.
 */
@Builder
public record NotificacaoResponse(
    Integer id,
    String titulo,
    String mensagem,
    OffsetDateTime dataCriacao,
    Boolean lida,
    LocalDateTime dataLeitura,
    String tipo,
    Integer entidadeRelacionadaId,
    String entidadeRelacionadaTipo
) {
    public static NotificacaoResponse from(Notificacao notificacao) {
        return from(notificacao, null);
    }

    public static NotificacaoResponse from(Notificacao notificacao, Integer numeroDoacao) {
        String mensagem = notificacao.getMensagem();
        if (mensagem != null && "DOACAO".equalsIgnoreCase(notificacao.getEntidadeRelacionadaTipo())
                && notificacao.getEntidadeRelacionadaId() != null) {
            Integer id = notificacao.getEntidadeRelacionadaId();
            String referencia = numeroDoacao == null
                    ? "doação (ID " + id + ")"
                    : "doação N° " + numeroDoacao + " (ID " + id + ")";
            mensagem = mensagem.replaceFirst("(?i)doa(?:ç|c)(?:ã|a)o\\s*#\\s*" + id + "(?!\\d)",
                    java.util.regex.Matcher.quoteReplacement(referencia));
        }
        return NotificacaoResponse.builder()
                .id(notificacao.getId())
                .titulo(notificacao.getTitulo())
                .mensagem(mensagem)
                .dataCriacao(notificacao.getDataCriacao() != null ? notificacao.getDataCriacao().atOffset(ZoneOffset.UTC) : null)
                .lida(notificacao.getLida())
                .dataLeitura(notificacao.getDataLeitura())
                .tipo(notificacao.getTipo() != null ? notificacao.getTipo().name() : null)
                .entidadeRelacionadaId(notificacao.getEntidadeRelacionadaId())
                .entidadeRelacionadaTipo(notificacao.getEntidadeRelacionadaTipo())
                .build();
    }
}

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

    public static NotificacaoResponse from(Notificacao notificacao, Integer numero) {
        String mensagem = notificacao.getMensagem();
        if (mensagem != null && "DOACAO".equalsIgnoreCase(notificacao.getEntidadeRelacionadaTipo())
                && "Status da Doação Atualizado".equals(notificacao.getTitulo())
                && mensagem.endsWith(" Combine os detalhes pelo chat.")) {
            mensagem = mensagem.substring(0, mensagem.length() - " Combine os detalhes pelo chat.".length());
        }
        if (mensagem != null && "DOACAO".equalsIgnoreCase(notificacao.getEntidadeRelacionadaTipo())
                && notificacao.getEntidadeRelacionadaId() != null) {
            Integer id = notificacao.getEntidadeRelacionadaId();
            String referencia = numero == null
                    ? "doação (ID " + id + ")"
                    : "doação N° " + numero + " (ID " + id + ")";
            mensagem = mensagem.replaceFirst("(?i)doa(?:ç|c)(?:ã|a)o\\s*#\\s*" + id + "(?!\\d)",
                    java.util.regex.Matcher.quoteReplacement(referencia));
        }
        if (mensagem != null && notificacao.getEntidadeRelacionadaId() != null) {
            Integer id = notificacao.getEntidadeRelacionadaId();
            String tipo = notificacao.getEntidadeRelacionadaTipo();
            String rotulo = null;
            String padrao = null;
            if ("SOLICITACAO".equalsIgnoreCase(tipo)) {
                rotulo = "solicitação";
                padrao = "(?i)(?:solicita(?:ç|c)(?:ã|a)o|pedido)\\s*#\\s*";
            } else if ("SUPORTE".equalsIgnoreCase(tipo)) {
                rotulo = "ticket";
                padrao = "(?i)(?:suporte|ticket|chamado)\\s*#\\s*";
            }
            if (rotulo != null) {
                String referencia = numero == null ? rotulo + " (ID " + id + ")" : rotulo + " N° " + numero + " (ID " + id + ")";
                mensagem = mensagem.replaceFirst(padrao + id + "(?!\\d)", java.util.regex.Matcher.quoteReplacement(referencia));
            }
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

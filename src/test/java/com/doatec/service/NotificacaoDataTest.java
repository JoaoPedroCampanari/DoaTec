package com.doatec.service;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.model.notification.Notificacao;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificacaoDataTest {
    @Test
    void dataSalvaEhExpostaComFusoUtc() {
        LocalDateTime dataSalva = LocalDateTime.of(2026, 11, 12, 15, 22);
        Notificacao notificacao = Notificacao.builder().dataCriacao(dataSalva).build();
        var resposta = NotificacaoResponse.from(notificacao);
        assertEquals(dataSalva.atOffset(ZoneOffset.UTC), resposta.dataCriacao());
        assertTrue(resposta.dataCriacao().toString().endsWith("Z"));
    }

    @Test
    void novaNotificacaoUsaRelogioUtc() {
        var antes = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
        var notificacao = Notificacao.builder().build();
        var depois = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1);
        assertTrue(!notificacao.getDataCriacao().isBefore(antes) && !notificacao.getDataCriacao().isAfter(depois));
    }
}

package com.doatec.service;

import com.doatec.exception.BusinessException;
import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.donation.PreferenciaEntrega;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.LogAcaoRepository;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceDoacaoTest {
    @Mock DoacaoRepository doacoes;
    @Mock PessoaRepository pessoas;
    @Mock LogAcaoRepository logs;
    @Mock NotificacaoService notificacoes;
    @InjectMocks AdminService service;

    private Doacao preparar(StatusDoacao status) {
        DoadorPF admin = new DoadorPF("12345678900");
        admin.setId(1); admin.setRole(Role.ADMIN); admin.setAtivo(true);
        Doacao doacao = new Doacao(); doacao.setId(1); doacao.setDoador(admin); doacao.setStatus(status); doacao.setPreferenciaEntrega(PreferenciaEntrega.PONTO_DE_COLETA);
        when(doacoes.findByIdForUpdate(1)).thenReturn(Optional.of(doacao));
        when(pessoas.findById(1)).thenReturn(Optional.of(admin));
        return doacao;
    }

    @ParameterizedTest
    @EnumSource(value = StatusDoacao.class, names = {"AGUARDANDO_ENTREGA", "RECUSADO"})
    void analisePodeSerDecidida(StatusDoacao decisao) {
        Doacao d = preparar(StatusDoacao.EM_ANALISE);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        service.alterarStatusDoacao(1, decisao, 1, null);
        assertEquals(decisao, d.getStatus());
        verify(notificacoes).criarNotificacao(eq(1), anyString(), anyString(), any(), eq(1), eq("DOACAO"));
    }

    @ParameterizedTest
    @EnumSource(value = StatusDoacao.class, names = {"ENTREGUE", "RETIRADO", "RECUSADO"})
    void decisaoFinalNaoPodeSerAlterada(StatusDoacao finalizado) {
        Doacao d = preparar(finalizado);
        for (StatusDoacao tentativa : StatusDoacao.values()) {
            assertThrows(BusinessException.class, () -> service.alterarStatusDoacao(1, tentativa, 1, null));
        }
        assertEquals(finalizado, d.getStatus());
        verify(doacoes, never()).save(any());
    }

    @Test
    void entregaSegueApenasSeuFluxo() {
        Doacao d = preparar(StatusDoacao.EM_ANALISE);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThrows(BusinessException.class, () -> service.alterarStatusDoacao(1, StatusDoacao.COLETA_AGENDADA, 1, null));
        assertThrows(BusinessException.class, () -> service.alterarStatusDoacao(1, StatusDoacao.ENTREGUE, 1, null));
        service.alterarStatusDoacao(1, StatusDoacao.AGUARDANDO_ENTREGA, 1, null);
        service.alterarStatusDoacao(1, StatusDoacao.ENTREGUE, 1, null);
        assertEquals(StatusDoacao.ENTREGUE, d.getStatus());
    }

    @Test
    void coletaSegueApenasSeuFluxo() {
        Doacao d = preparar(StatusDoacao.EM_ANALISE);
        d.setPreferenciaEntrega(PreferenciaEntrega.SOLICITAR_RETIRADA);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThrows(BusinessException.class, () -> service.alterarStatusDoacao(1, StatusDoacao.AGUARDANDO_ENTREGA, 1, null));
        assertThrows(BusinessException.class, () -> service.alterarStatusDoacao(1, StatusDoacao.RETIRADO, 1, null));
        service.alterarStatusDoacao(1, StatusDoacao.COLETA_AGENDADA, 1, null);
        service.alterarStatusDoacao(1, StatusDoacao.RETIRADO, 1, null);
        assertEquals(StatusDoacao.RETIRADO, d.getStatus());
    }

    @Test
    void aprovarRetiradaAgendaColetaDiretamente() {
        Doacao d = preparar(StatusDoacao.EM_ANALISE);
        d.setPreferenciaEntrega(PreferenciaEntrega.SOLICITAR_RETIRADA);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("COLETA_AGENDADA", service.aprovarDoacao(1, 1, null).status());
    }

    @Test
    void atualizaObservacaoEmCadaEtapa() {
        Doacao d = preparar(StatusDoacao.EM_ANALISE);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        service.alterarStatusDoacao(1, StatusDoacao.AGUARDANDO_ENTREGA, 1, new AvaliacaoRequest("  Primeira observação  "));
        assertEquals("Primeira observação", d.getObservacaoAdmin());
        var response = service.alterarStatusDoacao(1, StatusDoacao.ENTREGUE, 1, new AvaliacaoRequest("  Recebido na Fatec  "));
        assertEquals("Recebido na Fatec", d.getObservacaoAdmin());
        assertEquals("Recebido na Fatec", response.observacaoAdmin());
    }

    @Test
    void observacaoEmBrancoNaoApagaUltimaObservacao() {
        Doacao d = preparar(StatusDoacao.AGUARDANDO_ENTREGA);
        d.setObservacaoAdmin("Informação anterior");
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.alterarStatusDoacao(1, StatusDoacao.ENTREGUE, 1, new AvaliacaoRequest("  "));
        assertEquals("Informação anterior", response.observacaoAdmin());
    }

    @Test
    void doacaoAntigaPodeSerMarcadaEntregueDiretamente() {
        preparar(StatusDoacao.APROVADO);
        when(doacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.alterarStatusDoacao(1, StatusDoacao.ENTREGUE, 1, null);
        assertEquals("ENTREGUE", response.status());
    }

    @Test
    void endpointsDedicadosRespeitamEstadoFinal() {
        preparar(StatusDoacao.RECUSADO);
        assertThrows(BusinessException.class, () -> service.aprovarDoacao(1, 1, null));
        assertThrows(BusinessException.class, () -> service.rejeitarDoacao(1, 1, null));
        verify(doacoes, never()).save(any());
    }
}

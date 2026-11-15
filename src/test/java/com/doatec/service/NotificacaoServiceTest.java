package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.model.donation.Doacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.NotificacaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.suporte.SuporteFormulario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoService")
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @Mock
    private SuporteFormularioRepository suporteRepository;

    @InjectMocks
    private NotificacaoService service;

    @Test
    void notificacoesUsamNumeracaoAtualDoDoadorEIdSeparado() {
        Doacao recente = Doacao.builder().id(18).build();
        Doacao antiga = Doacao.builder().id(5).build();
        Notificacao status = Notificacao.builder().mensagem("Sua doação #18 foi aprovada.")
                .entidadeRelacionadaTipo("DOACAO").entidadeRelacionadaId(18).build();
        Notificacao chat = Notificacao.builder().mensagem("Nova mensagem em doacao #5")
                .entidadeRelacionadaTipo("DOACAO").entidadeRelacionadaId(5).build();
        Notificacao apagada = Notificacao.builder().mensagem("Sua doação #99 foi recusada.")
                .entidadeRelacionadaTipo("DOACAO").entidadeRelacionadaId(99).build();
        var notificacoes = List.of(status, chat, apagada);
        when(notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(1)).thenReturn(notificacoes);
        when(doacaoRepository.findByDoadorIdOrderByDataDoacaoDescIdDesc(1))
                .thenReturn(List.of(recente, antiga), List.of(recente));

        var primeiraConsulta = service.listarPorDestinatario(1);
        assertEquals("Sua doação N° 2 (ID 18) foi aprovada.", primeiraConsulta.get(0).mensagem());
        assertEquals("Nova mensagem em doação N° 1 (ID 5)", primeiraConsulta.get(1).mensagem());
        assertEquals("Sua doação (ID 99) foi recusada.", primeiraConsulta.get(2).mensagem());
        assertEquals("Sua doação #18 foi aprovada.", status.getMensagem());

        var aposExclusao = service.listarPorDestinatario(1);
        assertEquals("Sua doação N° 1 (ID 18) foi aprovada.", aposExclusao.get(0).mensagem());
        assertEquals("Nova mensagem em doação (ID 5)", aposExclusao.get(1).mensagem());
    }

    @Test
    void notificacoesDePedidosETicketsUsamNumeracaoDoProprietario() {
        var pedidoRecente = SolicitacaoHardware.builder().id(4).build();
        var pedidoAntigo = SolicitacaoHardware.builder().id(1).build();
        var ticket = SuporteFormulario.builder().id(4).build();
        var status = Notificacao.builder().mensagem("Sua solicitação #4 foi aprovada.").entidadeRelacionadaTipo("SOLICITACAO").entidadeRelacionadaId(4).build();
        var chatPedido = Notificacao.builder().mensagem("Nova mensagem em solicitacao #1").entidadeRelacionadaTipo("SOLICITACAO").entidadeRelacionadaId(1).build();
        var chatTicket = Notificacao.builder().mensagem("Nova mensagem em suporte #4").entidadeRelacionadaTipo("SUPORTE").entidadeRelacionadaId(4).build();
        var excluido = Notificacao.builder().mensagem("Nova mensagem em suporte #9").entidadeRelacionadaTipo("SUPORTE").entidadeRelacionadaId(9).build();
        when(notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(1)).thenReturn(List.of(status, chatPedido, chatTicket, excluido));
        when(solicitacaoRepository.findByAlunoIdOrderByDataSolicitacaoDescIdDesc(1)).thenReturn(List.of(pedidoRecente, pedidoAntigo));
        when(suporteRepository.findByAutorIdOrderByDataCriacaoDescIdDesc(1)).thenReturn(List.of(ticket));
        var result = service.listarPorDestinatario(1);
        assertEquals("Sua solicitação N° 2 (ID 4) foi aprovada.", result.get(0).mensagem());
        assertEquals("Nova mensagem em solicitação N° 1 (ID 1)", result.get(1).mensagem());
        assertEquals("Nova mensagem em ticket N° 1 (ID 4)", result.get(2).mensagem());
        assertEquals("Nova mensagem em ticket (ID 9)", result.get(3).mensagem());
        assertEquals("Sua solicitação #4 foi aprovada.", status.getMensagem());
    }

    // =====================================================================
    // criarNotificacao
    // =====================================================================
    @Nested
    @DisplayName("criarNotificacao")
    class CriarNotificacaoTests {

        @Test
        @DisplayName("Deve lancar excecao quando destinatario nao existe")
        void destinatarioInexistente_deveLancarExcecao() {
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarNotificacao(999, "Titulo", "Mensagem",
                            TipoNotificacao.SISTEMA, 1, "sistema"));

            assertEquals("Destinatário não encontrado", ex.getMessage());
            verify(notificacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar notificacao com sucesso")
        void sucesso_deveCriarNotificacao() {
            Aluno destinatario = Aluno.builder()
                    .ra("20240001")
                    .build();
            destinatario.setId(1);
            destinatario.setNome("Usuario Teste");

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(destinatario));
            when(notificacaoRepository.save(any(Notificacao.class)))
                    .thenAnswer(inv -> {
                        Notificacao n = inv.getArgument(0);
                        n.setId(10);
                        return n;
                    });

            Notificacao resultado = service.criarNotificacao(1, "Titulo Teste",
                    "Mensagem de teste", TipoNotificacao.DOACAO_APROVADA, 5, "doacao");

            assertNotNull(resultado);
            assertEquals(destinatario, resultado.getDestinatario());
            assertEquals("Titulo Teste", resultado.getTitulo());
            assertEquals("Mensagem de teste", resultado.getMensagem());
            assertEquals(TipoNotificacao.DOACAO_APROVADA, resultado.getTipo());
            assertEquals(5, resultado.getEntidadeRelacionadaId());
            assertEquals("doacao", resultado.getEntidadeRelacionadaTipo());

            verify(notificacaoRepository).save(any(Notificacao.class));
        }
    }

    // =====================================================================
    // criarNotificacaoEmMassa
    // =====================================================================
    @Nested
    @DisplayName("criarNotificacaoEmMassa")
    class CriarNotificacaoEmMassaTests {

        @Test
        @DisplayName("Deve lancar excecao quando ID invalido na lista de destinatarios")
        void idInvalidoNaLista_deveLancarExcecao() {
            Aluno destinatario1 = Aluno.builder().ra("20240001").build();
            destinatario1.setId(1);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(destinatario1));
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarNotificacaoEmMassa(List.of(1, 999), "Titulo",
                            "Mensagem", TipoNotificacao.SISTEMA));

            assertTrue(ex.getMessage().contains("Destinatário não encontrado"));
            verify(notificacaoRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Deve criar notificacoes em massa com sucesso")
        void sucesso_deveCriarTodas() {
            Aluno pessoa1 = Aluno.builder().ra("20240001").build();
            pessoa1.setId(1);
            Aluno pessoa2 = Aluno.builder().ra("20240002").build();
            pessoa2.setId(2);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(pessoa1));
            when(pessoaRepository.findById(2)).thenReturn(Optional.of(pessoa2));
            when(notificacaoRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<Notificacao> resultado = service.criarNotificacaoEmMassa(
                    List.of(1, 2), "Aviso", "Mensagem em massa", TipoNotificacao.SISTEMA);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals(pessoa1, resultado.get(0).getDestinatario());
            assertEquals(pessoa2, resultado.get(1).getDestinatario());

            verify(notificacaoRepository).saveAll(anyList());
        }
    }

    // =====================================================================
    // deleteByIdAndDestinatario
    // =====================================================================
    @Nested
    @DisplayName("deleteByIdAndDestinatario")
    class DeleteByIdAndDestinatarioTests {

        @Test
        @DisplayName("Deve retornar false quando notificacao pertence a outro usuario")
        void notificacaoDeOutroUsuario_deveRetornarFalse() {
            when(notificacaoRepository.findByIdAndDestinatarioId(10, 1))
                    .thenReturn(Optional.empty());

            boolean resultado = service.deleteByIdAndDestinatario(10, 1);

            assertFalse(resultado);
            verify(notificacaoRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve deletar notificacao e retornar true com sucesso")
        void sucesso_deveDeletarERetornarTrue() {
            Notificacao notificacao = new Notificacao();
            notificacao.setId(10);

            when(notificacaoRepository.findByIdAndDestinatarioId(10, 1))
                    .thenReturn(Optional.of(notificacao));

            boolean resultado = service.deleteByIdAndDestinatario(10, 1);

            assertTrue(resultado);
            verify(notificacaoRepository).deleteById(10);
        }
    }

    // =====================================================================
    // marcarTodasComoLidas
    // =====================================================================
    @Nested
    @DisplayName("marcarTodasComoLidas")
    class MarcarTodasComoLidasTests {

        @Test
        @DisplayName("Deve marcar todas as notificacoes como lidas")
        void sucesso_deveMarcarTodas() {
            service.marcarTodasComoLidas(1);

            verify(notificacaoRepository).marcarTodasComoLidas(1);
        }
    }

    // =====================================================================
    // marcarComoLida
    // =====================================================================
    @Nested
    @DisplayName("marcarComoLida")
    class MarcarComoLidaTests {

        @Test
        @DisplayName("Deve marcar notificacao como lida")
        void sucesso_deveMarcar() {
            Notificacao notificacao = Notificacao.builder()
                    .id(1)
                    .titulo("Teste")
                    .mensagem("Mensagem")
                    .tipo(TipoNotificacao.DOACAO_APROVADA)
                    .lida(false)
                    .build();

            when(notificacaoRepository.findById(1)).thenReturn(Optional.of(notificacao));
            when(notificacaoRepository.save(any())).thenReturn(notificacao);

            service.marcarComoLida(1);

            assertTrue(notificacao.getLida());
            assertNotNull(notificacao.getDataLeitura());
            verify(notificacaoRepository).save(notificacao);
        }

        @Test
        @DisplayName("Lanca excecao quando notificacao nao existe")
        void lancaExcecaoQuandoNaoExiste() {
            when(notificacaoRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(com.doatec.exception.BusinessException.class,
                    () -> service.marcarComoLida(999));
        }
    }

    // =====================================================================
    // listarUltimas
    // =====================================================================
    @Nested
    @DisplayName("listarUltimas")
    class ListarUltimasTests {

        @Test
        @DisplayName("Deve retornar ultimas notificacoes")
        void sucesso_deveRetornarUltimas() {
            Notificacao notificacao = Notificacao.builder()
                    .id(1)
                    .titulo("Teste")
                    .mensagem("Mensagem")
                    .tipo(TipoNotificacao.DOACAO_APROVADA)
                    .build();

            when(notificacaoRepository.findTopByDestinatarioId(1, 5))
                    .thenReturn(java.util.List.of(notificacao));

            java.util.List<com.doatec.dto.response.NotificacaoResponse> result = service.listarUltimas(1, 5);

            assertEquals(1, result.size());
            verify(notificacaoRepository).findTopByDestinatarioId(1, 5);
        }
    }
}

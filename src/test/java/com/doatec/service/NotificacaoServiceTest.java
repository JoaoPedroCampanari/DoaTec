package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.repository.NotificacaoRepository;
import com.doatec.repository.PessoaRepository;
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

    @InjectMocks
    private NotificacaoService service;

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

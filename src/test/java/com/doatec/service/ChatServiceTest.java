package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.ChatRequest;
import com.doatec.dto.response.ChatResponse;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import com.doatec.model.donation.Doacao;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.MensagemChatRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService")
class ChatServiceTest {

    @Mock
    private MensagemChatRepository mensagemChatRepository;

    @Mock
    private SuporteFormularioRepository suporteFormularioRepository;

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @InjectMocks
    private ChatService service;

    private Aluno autor;
    private Aluno outroUsuario;
    private Pessoa admin;
    private DoadorPF doador;

    @BeforeEach
    void setUp() {
        autor = new Aluno();
        autor.setId(1);
        autor.setNome("Autor Teste");
        autor.setRole(Role.USER);

        outroUsuario = new Aluno();
        outroUsuario.setId(99);
        outroUsuario.setNome("Outro Usuario");
        outroUsuario.setRole(Role.USER);

        admin = new Aluno();
        admin.setId(10);
        admin.setNome("Admin Teste");
        admin.setRole(Role.ADMIN);

        doador = new DoadorPF("123.456.789-00");
        doador.setId(2);
        doador.setNome("Doador Teste");
        doador.setRole(Role.USER);
    }

    // =====================================================================
    // enviarMensagem
    // =====================================================================
    @Nested
    @DisplayName("enviarMensagem")
    class EnviarMensagemTests {

        @Test
        @DisplayName("SUPORTE: autor do ticket envia mensagem com sucesso")
        void enviarMensagem_suporte_autorDoTicket_sucesso() {
            SuporteFormulario ticket = new SuporteFormulario();
            ticket.setId(1);
            ticket.setAutor(autor);

            when(suporteFormularioRepository.findById(1)).thenReturn(Optional.of(ticket));

            MensagemChat salva = MensagemChat.builder()
                    .id(100)
                    .conteudo("Preciso de ajuda")
                    .referenciaId(1)
                    .contexto(ContextoChat.SUPORTE)
                    .remetente(autor)
                    .dataEnvio(LocalDateTime.now())
                    .build();

            when(mensagemChatRepository.save(any(MensagemChat.class))).thenReturn(salva);

            ChatRequest request = new ChatRequest("Preciso de ajuda", 1, ContextoChat.SUPORTE);
            ChatResponse response = service.enviarMensagem(request, autor);

            assertNotNull(response);
            assertEquals(100, response.id());
            assertEquals("Preciso de ajuda", response.conteudo());
            assertEquals(1, response.remetenteId());
            assertEquals("Autor Teste", response.remetenteNome());
            assertEquals("USER", response.remetenteRole());
            verify(mensagemChatRepository).save(any(MensagemChat.class));
        }

        @Test
        @DisplayName("SUPORTE: outro usuario que nao e autor recebe 403")
        void enviarMensagem_suporte_outroUsuario_forbidden() {
            SuporteFormulario ticket = new SuporteFormulario();
            ticket.setId(1);
            ticket.setAutor(autor);

            when(suporteFormularioRepository.findById(1)).thenReturn(Optional.of(ticket));

            ChatRequest request = new ChatRequest("Mensagem indevida", 1, ContextoChat.SUPORTE);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.enviarMensagem(request, outroUsuario));

            assertEquals(403, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("acesso"));
            verify(mensagemChatRepository, never()).save(any());
        }

        @Test
        @DisplayName("ADMIN: pode enviar mensagem em qualquer contexto")
        void enviarMensagem_admin_qualquerContexto_sucesso() {
            MensagemChat salva = MensagemChat.builder()
                    .id(200)
                    .conteudo("Resposta do admin")
                    .referenciaId(1)
                    .contexto(ContextoChat.SUPORTE)
                    .remetente(admin)
                    .dataEnvio(LocalDateTime.now())
                    .build();

            when(mensagemChatRepository.save(any(MensagemChat.class))).thenReturn(salva);

            ChatRequest request = new ChatRequest("Resposta do admin", 1, ContextoChat.SUPORTE);
            ChatResponse response = service.enviarMensagem(request, admin);

            assertNotNull(response);
            assertEquals(200, response.id());
            assertEquals("Resposta do admin", response.conteudo());
            assertEquals(10, response.remetenteId());
            assertEquals("Admin Teste", response.remetenteNome());
            assertEquals("ADMIN", response.remetenteRole());
            // Admin bypasses validation — no repository lookup needed
            verify(suporteFormularioRepository, never()).findById(any());
            verify(mensagemChatRepository).save(any(MensagemChat.class));
        }
    }

    // =====================================================================
    // buscarHistorico
    // =====================================================================
    @Nested
    @DisplayName("buscarHistorico")
    class BuscarHistoricoTests {

        @Test
        @DisplayName("DOACAO: doador dono da doacao busca historico com sucesso")
        void buscarHistorico_doacao_doadorDono_sucesso() {
            Doacao doacao = new Doacao();
            doacao.setId(5);
            doacao.setDoador(doador);

            when(doacaoRepository.findById(5)).thenReturn(Optional.of(doacao));

            MensagemChat msg1 = MensagemChat.builder()
                    .id(10).conteudo("Oi").referenciaId(5).contexto(ContextoChat.DOACAO)
                    .remetente(doador).dataEnvio(LocalDateTime.now().minusMinutes(5))
                    .build();
            MensagemChat msg2 = MensagemChat.builder()
                    .id(11).conteudo("Obrigado").referenciaId(5).contexto(ContextoChat.DOACAO)
                    .remetente(doador).dataEnvio(LocalDateTime.now())
                    .build();

            when(mensagemChatRepository.findByContextoAndReferenciaIdOrderByDataEnvioAsc(
                    ContextoChat.DOACAO, 5)).thenReturn(List.of(msg1, msg2));

            List<ChatResponse> historico = service.buscarHistorico(ContextoChat.DOACAO, 5, doador);

            assertEquals(2, historico.size());
            assertEquals("Oi", historico.get(0).conteudo());
            assertEquals("Obrigado", historico.get(1).conteudo());
        }

        @Test
        @DisplayName("DOACAO: outro usuario que nao e dono recebe 403")
        void buscarHistorico_doacao_outroUsuario_forbidden() {
            Doacao doacao = new Doacao();
            doacao.setId(5);
            doacao.setDoador(doador);

            when(doacaoRepository.findById(5)).thenReturn(Optional.of(doacao));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.buscarHistorico(ContextoChat.DOACAO, 5, outroUsuario));

            assertEquals(403, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("acesso"));
            verify(mensagemChatRepository, never())
                    .findByContextoAndReferenciaIdOrderByDataEnvioAsc(any(), any());
        }

        @Test
        @DisplayName("Referencia inexistente lanca 404")
        void buscarHistorico_referenciaInexistente_notFound() {
            when(doacaoRepository.findById(999)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.buscarHistorico(ContextoChat.DOACAO, 999, doador));

            assertEquals(404, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("não encontrada"));
            verify(mensagemChatRepository, never())
                    .findByContextoAndReferenciaIdOrderByDataEnvioAsc(any(), any());
        }
    }
}

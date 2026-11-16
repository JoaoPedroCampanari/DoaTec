package com.doatec.service;

import com.doatec.dto.request.ChatRequest;
import com.doatec.dto.response.ChatResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.repository.PessoaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import com.doatec.model.donation.Doacao;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.MensagemChatRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Serviço para gerenciamento das conversas.
 */
@Service
public class ChatService {

    @Autowired
    private MensagemChatRepository mensagemChatRepository;

    @Autowired
    private SuporteFormularioRepository suporteFormularioRepository;

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Registra uma mensagem e notifica os destinatários.
     */
    @Transactional
    public ChatResponse enviarMensagem(ChatRequest request, Pessoa remetente) {
        var conversa = carregarContexto(request.contexto(), request.referenciaId(), true);
        validarProprietario(conversa.dono(), remetente);
        if (conversa.encerrada())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conversa foi encerrada e está disponível apenas para consulta.");

        MensagemChat mensagem = MensagemChat.builder()
                .conteudo(request.conteudo())
                .referenciaId(request.referenciaId())
                .contexto(request.contexto())
                .remetente(remetente)
                .build();

        MensagemChat saved = mensagemChatRepository.save(mensagem);
        Pessoa dono = conversa.dono();
        if (remetente.isAdmin()) {
            if (!dono.getId().equals(remetente.getId())) {
                notificarMensagem(dono, request);
            }
        } else {
            for (Role role : List.of(Role.ADMIN, Role.SUPER_ADMIN)) {
                pessoaRepository.findByRole(role).stream()
                        .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                        .forEach(p -> notificarMensagem(p, request));
            }
        }
        return toResponse(saved);
    }

    /**
     * Cria uma notificação de nova mensagem.
     */
    private void notificarMensagem(Pessoa destinatario, ChatRequest request) {
        notificacaoService.criarNotificacao(destinatario.getId(), "Nova mensagem no chat",
                "Nova mensagem em " + request.contexto().name().toLowerCase() + " #" + request.referenciaId(),
                TipoNotificacao.SISTEMA, request.referenciaId(), request.contexto().name());
    }

    /**
     * Busca o histórico de mensagens de uma conversa.
     */
    public List<ChatResponse> buscarHistorico(ContextoChat contexto, Integer referenciaId, Pessoa usuario) {
        validarAcesso(contexto, referenciaId, usuario);
        List<MensagemChat> mensagens = mensagemChatRepository
                .findByContextoAndReferenciaIdOrderByDataEnvioAsc(contexto, referenciaId);
        return mensagens.stream().map(this::toResponse).toList();
    }

    public record ConversaResponse(boolean podeEnviar, List<ChatResponse> mensagens) {}
    private record ContextoConversa(Pessoa dono, boolean encerrada) {}

    /**
     * Busca as mensagens e verifica se a conversa permite envio.
     */
    @Transactional(readOnly = true)
    public ConversaResponse buscarConversa(ContextoChat contexto, Integer id, Pessoa usuario) {
        ContextoConversa conversa = carregarContexto(contexto, id, false);
        validarProprietario(conversa.dono(), usuario);
        List<ChatResponse> mensagens = mensagemChatRepository.findByContextoAndReferenciaIdOrderByDataEnvioAsc(contexto, id)
                .stream().map(this::toResponse).toList();
        return new ConversaResponse(!conversa.encerrada(), mensagens);
    }

    /**
     * Verifica se o usuário pode acessar a conversa.
     */
    private void validarProprietario(Pessoa dono, Pessoa usuario) {
        if (!usuario.isAdmin() && !dono.getId().equals(usuario.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a este chat");
    }

    /**
     * Busca o proprietário e a situação da conversa.
     */
    private ContextoConversa carregarContexto(ContextoChat contexto, Integer id, boolean bloquear) {
        return switch (contexto) {
            case SUPORTE -> {
                SuporteFormulario ticket = (bloquear ? suporteFormularioRepository.findByIdForUpdate(id) : suporteFormularioRepository.findById(id))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
                yield new ContextoConversa(ticket.getAutor(), java.util.Set.of(com.doatec.model.suporte.StatusSuporte.RESOLVIDO, com.doatec.model.suporte.StatusSuporte.FECHADO).contains(ticket.getStatus()));
            }
            case DOACAO -> {
                Doacao doacao = (bloquear ? doacaoRepository.findByIdForUpdate(id) : doacaoRepository.findById(id))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doação não encontrada"));
                yield new ContextoConversa(doacao.getDoador(), java.util.Set.of(com.doatec.model.donation.StatusDoacao.RECUSADO, com.doatec.model.donation.StatusDoacao.CANCELADA, com.doatec.model.donation.StatusDoacao.ENTREGUE, com.doatec.model.donation.StatusDoacao.RETIRADO, com.doatec.model.donation.StatusDoacao.APROVADO).contains(doacao.getStatus()));
            }
            case SOLICITACAO -> {
                SolicitacaoHardware solicitacao = (bloquear ? solicitacaoHardwareRepository.findByIdForUpdate(id) : solicitacaoHardwareRepository.findById(id))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
                yield new ContextoConversa(solicitacao.getAluno(), java.util.Set.of(com.doatec.model.solicitacao.StatusSolicitacao.REJEITADA, com.doatec.model.solicitacao.StatusSolicitacao.CANCELADA, com.doatec.model.solicitacao.StatusSolicitacao.CONCLUIDA).contains(solicitacao.getStatus()));
            }
        };
    }

    /**
     * Verifica a permissão de acesso ao histórico da conversa.
     */
    private void validarAcesso(ContextoChat contexto, Integer referenciaId, Pessoa usuario) {
        if (usuario.isAdmin()) {
            return;
        }

        switch (contexto) {
            case SUPORTE -> {
                SuporteFormulario ticket = suporteFormularioRepository.findById(referenciaId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
                if (!ticket.getAutor().getId().equals(usuario.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a este chat");
                }
            }
            case DOACAO -> {
                Doacao doacao = doacaoRepository.findById(referenciaId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doação não encontrada"));
                if (!doacao.getDoador().getId().equals(usuario.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a este chat");
                }
            }
            case SOLICITACAO -> {
                SolicitacaoHardware solicitacao = solicitacaoHardwareRepository.findById(referenciaId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
                if (!solicitacao.getAluno().getId().equals(usuario.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a este chat");
                }
            }
        }
    }

    /**
     * Converte uma mensagem para o formato de resposta.
     */
    private ChatResponse toResponse(MensagemChat msg) {
        return new ChatResponse(
                msg.getId(),
                msg.getConteudo(),
                msg.getDataEnvio(),
                msg.getRemetente().getId(),
                msg.getRemetente().getNome(),
                msg.getRemetente().getRole().name()
        );
    }
}

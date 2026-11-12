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

    @Transactional
    public ChatResponse enviarMensagem(ChatRequest request, Pessoa remetente) {
        validarAcesso(request.contexto(), request.referenciaId(), remetente);

        MensagemChat mensagem = MensagemChat.builder()
                .conteudo(request.conteudo())
                .referenciaId(request.referenciaId())
                .contexto(request.contexto())
                .remetente(remetente)
                .build();

        MensagemChat saved = mensagemChatRepository.save(mensagem);
        Pessoa dono = switch (request.contexto()) {
            case SUPORTE -> suporteFormularioRepository.findById(request.referenciaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa nao encontrada")).getAutor();
            case DOACAO -> doacaoRepository.findById(request.referenciaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa nao encontrada")).getDoador();
            case SOLICITACAO -> solicitacaoHardwareRepository.findById(request.referenciaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa nao encontrada")).getAluno();
        };
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

    private void notificarMensagem(Pessoa destinatario, ChatRequest request) {
        notificacaoService.criarNotificacao(destinatario.getId(), "Nova mensagem no chat",
                "Nova mensagem em " + request.contexto().name().toLowerCase() + " #" + request.referenciaId(),
                TipoNotificacao.SISTEMA, request.referenciaId(), request.contexto().name());
    }

    public List<ChatResponse> buscarHistorico(ContextoChat contexto, Integer referenciaId, Pessoa usuario) {
        validarAcesso(contexto, referenciaId, usuario);
        List<MensagemChat> mensagens = mensagemChatRepository
                .findByContextoAndReferenciaIdOrderByDataEnvioAsc(contexto, referenciaId);
        return mensagens.stream().map(this::toResponse).toList();
    }

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

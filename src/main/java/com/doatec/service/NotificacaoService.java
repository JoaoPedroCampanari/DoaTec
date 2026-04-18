package com.doatec.service;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Pessoa;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.repository.NotificacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de notificações.
 */
@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    /**
     * Cria uma nova notificação para um destinatário específico.
     */
    @Transactional
    public Notificacao criarNotificacao(Integer destinatarioId, String titulo, String mensagem,
                                         TipoNotificacao tipo, Integer entidadeId, String entidadeTipo) {
        Pessoa destinatario = pessoaRepository.findById(destinatarioId)
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado"));

        Notificacao notificacao = Notificacao.builder()
                .destinatario(destinatario)
                .titulo(titulo)
                .mensagem(mensagem)
                .tipo(tipo)
                .entidadeRelacionadaId(entidadeId)
                .entidadeRelacionadaTipo(entidadeTipo)
                .build();

        return notificacaoRepository.save(notificacao);
    }

    /**
     * Cria notificações para múltiplos destinatários.
     * Útil para notificar todos os admins, por exemplo.
     */
    @Transactional
    public List<Notificacao> criarNotificacaoEmMassa(List<Integer> destinatariosIds,
                                                      String titulo, String mensagem,
                                                      TipoNotificacao tipo) {
        List<Notificacao> notificacoes = destinatariosIds.stream()
                .map(id -> {
                    Pessoa destinatario = pessoaRepository.findById(id)
                            .orElseThrow(() -> new BusinessException("Destinatário não encontrado: " + id));
                    return Notificacao.builder()
                            .destinatario(destinatario)
                            .titulo(titulo)
                            .mensagem(mensagem)
                            .tipo(tipo)
                            .build();
                })
                .collect(Collectors.toList());

        return notificacaoRepository.saveAll(notificacoes);
    }

    /**
     * Lista todas as notificações de um usuário.
     */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarPorDestinatario(Integer destinatarioId) {
        return notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(destinatarioId)
                .stream()
                .map(NotificacaoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lista apenas notificações não lidas de um usuário.
     */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarNaoLidas(Integer destinatarioId) {
        return notificacaoRepository.findByDestinatarioIdAndLidaFalseOrderByDataCriacaoDesc(destinatarioId)
                .stream()
                .map(NotificacaoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Conta notificações não lidas de um usuário.
     */
    @Transactional(readOnly = true)
    public Long contarNaoLidas(Integer destinatarioId) {
        return notificacaoRepository.countNaoLidasByDestinatarioId(destinatarioId);
    }

    /**
     * Marca uma notificação como lida.
     */
    @Transactional
    public void marcarComoLida(Integer notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new BusinessException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacao.setDataLeitura(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
    }

    /**
     * Marca todas as notificações de um usuário como lidas.
     */
    @Transactional
    public void marcarTodasComoLidas(Integer destinatarioId) {
        notificacaoRepository.marcarTodasComoLidas(destinatarioId);
    }

    /**
     * Retorna as últimas N notificações de um usuário.
     */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarUltimas(Integer destinatarioId, int limit) {
        return notificacaoRepository.findTopByDestinatarioId(destinatarioId, limit)
                .stream()
                .map(NotificacaoResponse::from)
                .collect(Collectors.toList());
    }
}
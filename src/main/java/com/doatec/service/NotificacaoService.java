package com.doatec.service;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Pessoa;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.repository.NotificacaoRepository;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    @Autowired
    private DoacaoRepository doacaoRepository;

    private List<NotificacaoResponse> respostasComNumeroDeDoacao(List<Notificacao> notificacoes, Integer destinatarioId) {
        Map<Integer, Integer> numeros = new HashMap<>();
        if (notificacoes.stream().anyMatch(n -> "DOACAO".equalsIgnoreCase(n.getEntidadeRelacionadaTipo()))) {
            var doacoes = doacaoRepository.findByDoadorIdOrderByDataDoacaoDescIdDesc(destinatarioId);
            for (int i = 0; i < doacoes.size(); i++) numeros.put(doacoes.get(i).getId(), doacoes.size() - i);
        }
        return notificacoes.stream()
                .map(n -> NotificacaoResponse.from(n, numeros.get(n.getEntidadeRelacionadaId())))
                .toList();
    }

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
        return respostasComNumeroDeDoacao(notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(destinatarioId), destinatarioId);
    }

    /**
     * Lista apenas notificações não lidas de um usuário.
     */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarNaoLidas(Integer destinatarioId) {
        return respostasComNumeroDeDoacao(notificacaoRepository.findByDestinatarioIdAndLidaFalseOrderByDataCriacaoDesc(destinatarioId), destinatarioId);
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
        return respostasComNumeroDeDoacao(notificacaoRepository.findTopByDestinatarioId(destinatarioId, limit), destinatarioId);
    }

    /**
     * Deleta uma notificação apenas se pertencer ao usuário autenticado (proteção contra IDOR).
     */
    @Transactional
    public boolean deleteByIdAndDestinatario(Integer notificacaoId, Integer destinatarioId) {
        return notificacaoRepository.findByIdAndDestinatarioId(notificacaoId, destinatarioId)
                .map(notificacao -> {
                    notificacaoRepository.deleteById(notificacaoId);
                    return true;
                })
                .orElse(false);
    }
}

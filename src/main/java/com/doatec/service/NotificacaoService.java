package com.doatec.service;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Pessoa;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.repository.NotificacaoRepository;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
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

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @Autowired
    private SuporteFormularioRepository suporteRepository;

    /**
     * Prepara as notificações com o número do item para o destinatário.
     */
    private List<NotificacaoResponse> respostasComNumero(List<Notificacao> notificacoes, Integer destinatarioId) {
        Map<String, Map<Integer, Integer>> numerosPorTipo = new HashMap<>();
        if (notificacoes.stream().anyMatch(n -> "DOACAO".equalsIgnoreCase(n.getEntidadeRelacionadaTipo()))) {
            var itens = doacaoRepository.findByDoadorIdOrderByDataDoacaoDescIdDesc(destinatarioId);
            Map<Integer, Integer> numeros = new HashMap<>();
            for (int i = 0; i < itens.size(); i++) numeros.put(itens.get(i).getId(), itens.size() - i);
            numerosPorTipo.put("DOACAO", numeros);
        }
        if (notificacoes.stream().anyMatch(n -> "SOLICITACAO".equalsIgnoreCase(n.getEntidadeRelacionadaTipo()))) {
            var itens = solicitacaoRepository.findByAlunoIdOrderByDataSolicitacaoDescIdDesc(destinatarioId);
            Map<Integer, Integer> numeros = new HashMap<>();
            for (int i = 0; i < itens.size(); i++) numeros.put(itens.get(i).getId(), itens.size() - i);
            numerosPorTipo.put("SOLICITACAO", numeros);
        }
        if (notificacoes.stream().anyMatch(n -> "SUPORTE".equalsIgnoreCase(n.getEntidadeRelacionadaTipo()))) {
            var itens = suporteRepository.findByAutorIdOrderByDataCriacaoDescIdDesc(destinatarioId);
            Map<Integer, Integer> numeros = new HashMap<>();
            for (int i = 0; i < itens.size(); i++) numeros.put(itens.get(i).getId(), itens.size() - i);
            numerosPorTipo.put("SUPORTE", numeros);
        }
        return notificacoes.stream().map(n -> {
            String tipo = n.getEntidadeRelacionadaTipo();
            Map<Integer, Integer> numeros = tipo == null ? Map.of() : numerosPorTipo.getOrDefault(tipo.toUpperCase(java.util.Locale.ROOT), Map.of());
            return NotificacaoResponse.from(n, n.getEntidadeRelacionadaId() == null ? null : numeros.get(n.getEntidadeRelacionadaId()));
        }).toList();
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
        return respostasComNumero(notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(destinatarioId), destinatarioId);
    }

    /**
     * Lista apenas notificações não lidas de um usuário.
     */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarNaoLidas(Integer destinatarioId) {
        return respostasComNumero(notificacaoRepository.findByDestinatarioIdAndLidaFalseOrderByDataCriacaoDesc(destinatarioId), destinatarioId);
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
        return respostasComNumero(notificacaoRepository.findTopByDestinatarioId(destinatarioId, limit), destinatarioId);
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

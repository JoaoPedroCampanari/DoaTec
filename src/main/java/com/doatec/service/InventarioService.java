package com.doatec.service;

import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.dto.response.SugestaoMatchingResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.EquipamentoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento do inventário de equipamentos.
 */
@Service
public class InventarioService {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoRepository;

    /**
     * Cria um equipamento a partir de um item doado aprovado.
     */
    @Transactional
    public Equipamento criarEquipamento(ItemDoado item, EstadoConservacao estado) {
        Equipamento equipamento = Equipamento.builder()
                .tipo(item.getTipoItem())
                .descricao(item.getDescricao())
                .estadoConservacao(estado != null ? estado : EstadoConservacao.BOM)
                .itemOrigem(item)
                .status(StatusEquipamento.DISPONIVEL)
                .build();

        return equipamentoRepository.save(equipamento);
    }

    /**
     * Lista todos os equipamentos, opcionalmente filtrados por status.
     */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listarEquipamentos(StatusEquipamento status) {
        List<Equipamento> equipamentos = status != null
                ? equipamentoRepository.findByStatus(status)
                : equipamentoRepository.findAll();

        return equipamentos.stream()
                .map(EquipamentoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Busca um equipamento por ID.
     */
    @Transactional(readOnly = true)
    public EquipamentoResponse buscarPorId(Integer id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Equipamento não encontrado com ID: " + id));

        return EquipamentoResponse.from(equipamento);
    }

    /**
     * Gera sugestões de matching para uma solicitação.
     * Busca equipamentos disponíveis compatíveis com a preferência do aluno.
     */
    @Transactional(readOnly = true)
    public SugestaoMatchingResponse sugerirMatchings(Integer solicitacaoId) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + solicitacaoId));

        String preferencia = solicitacao.getPreferenciaEquipamento();
        if (preferencia == null || preferencia.isBlank()) {
            return SugestaoMatchingResponse.builder()
                    .solicitacaoId(solicitacao.getId())
                    .alunoNome(solicitacao.getAluno().getNome())
                    .alunoEmail(solicitacao.getAluno().getEmail())
                    .preferenciaEquipamento("Não especificada")
                    .equipamentosCompativeis(List.of())
                    .build();
        }

        // Busca equipamentos disponíveis que correspondam à preferência
        List<Equipamento> equipamentosCompativeis = equipamentoRepository
                .findDisponiveisByKeyword(preferencia);

        List<SugestaoMatchingResponse.MatchEquipamentoResponse> matches = equipamentosCompativeis.stream()
                .map(e -> SugestaoMatchingResponse.MatchEquipamentoResponse.builder()
                        .equipamentoId(e.getId())
                        .tipo(e.getTipo())
                        .descricao(e.getDescricao())
                        .estadoConservacao(e.getEstadoConservacao() != null
                                ? e.getEstadoConservacao().getDescricao() : "Não informado")
                        .scoreCompatibilidade(calcularScore(preferencia, e.getTipo()))
                        .build())
                .sorted((a, b) -> b.scoreCompatibilidade().compareTo(a.scoreCompatibilidade()))
                .collect(Collectors.toList());

        return SugestaoMatchingResponse.builder()
                .solicitacaoId(solicitacao.getId())
                .alunoNome(solicitacao.getAluno().getNome())
                .alunoEmail(solicitacao.getAluno().getEmail())
                .preferenciaEquipamento(preferencia)
                .equipamentosCompativeis(matches)
                .build();
    }

    /**
     * Atribui um equipamento a uma solicitação aprovada.
     */
    @Transactional
    public EquipamentoResponse atribuirEquipamento(Integer equipamentoId, Integer solicitacaoId, Integer adminId) {
        Equipamento equipamento = equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new BusinessException("Equipamento não encontrado com ID: " + equipamentoId));

        if (!equipamento.isDisponivel()) {
            throw new BusinessException("Equipamento não está disponível para atribuição. Status atual: " + equipamento.getStatus());
        }

        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + solicitacaoId));

        if (!(solicitacao.getAluno() instanceof Aluno)) {
            throw new BusinessException("A solicitação não pertence a um aluno válido.");
        }

        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado"));

        equipamento.setStatus(StatusEquipamento.RESERVADO);
        equipamento.setSolicitacaoDestino(solicitacao);
        equipamento.setAlunoDestinatario(solicitacao.getAluno());
        equipamento.setDataAtribuicao(LocalDateTime.now());

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return EquipamentoResponse.from(equipamentoAtualizado);
    }

    /**
     * Marca um equipamento como entregue ao aluno.
     */
    @Transactional
    public EquipamentoResponse marcarComoEntregue(Integer equipamentoId, Integer adminId) {
        Equipamento equipamento = equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new BusinessException("Equipamento não encontrado com ID: " + equipamentoId));

        if (!equipamento.isReservado()) {
            throw new BusinessException("Apenas equipamentos reservados podem ser marcados como entregues.");
        }

        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado"));

        equipamento.setStatus(StatusEquipamento.ENTREGUE);
        equipamento.setDataEntrega(LocalDateTime.now());

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return EquipamentoResponse.from(equipamentoAtualizado);
    }

    /**
     * Lista equipamentos disponíveis que correspondem a uma preferência.
     */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> buscarDisponiveisPorPreferencia(String preferencia) {
        if (preferencia == null || preferencia.isBlank()) {
            return equipamentoRepository.findByStatus(StatusEquipamento.DISPONIVEL).stream()
                    .map(EquipamentoResponse::from)
                    .collect(Collectors.toList());
        }

        return equipamentoRepository.findDisponiveisByKeyword(preferencia).stream()
                .map(EquipamentoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Calcula um score de compatibilidade entre a preferência e o tipo do equipamento.
     * Score de 0 a 100.
     */
    private Integer calcularScore(String preferencia, String tipo) {
        if (preferencia == null || tipo == null) {
            return 0;
        }

        String prefLower = preferencia.toLowerCase();
        String tipoLower = tipo.toLowerCase();

        // Match exato
        if (prefLower.equals(tipoLower)) {
            return 100;
        }

        // Contém a preferência
        if (tipoLower.contains(prefLower) || prefLower.contains(tipoLower)) {
            return 80;
        }

        // Match parcial por palavras-chave
        String[] palavrasPref = prefLower.split("[\\s,]+");
        String[] palavrasTipo = tipoLower.split("[\\s,]+");

        int matches = 0;
        for (String palavraPref : palavrasPref) {
            for (String palavraTipo : palavrasTipo) {
                if (palavraPref.contains(palavraTipo) || palavraTipo.contains(palavraPref)) {
                    matches++;
                }
            }
        }

        if (matches > 0) {
            return 50 + (matches * 10);
        }

        // Nenhum match
        return 20;
    }
}
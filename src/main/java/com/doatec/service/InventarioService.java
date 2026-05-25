package com.doatec.service;

import com.doatec.dto.request.EquipamentoRequest;
import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.dto.response.SugestaoMatchingResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.AcaoTipo;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.LogAcao;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.EquipamentoRepository;
import com.doatec.repository.LogAcaoRepository;
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

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private LogAcaoRepository logAcaoRepository;

    /**
     * Cria um equipamento manualmente no inventário (admin auditando uma doação física
     * ou cadastrando uma peça avulsa que nunca passou pelo formulário online).
     *
     * <p>O vínculo com {@code doacaoId} é opcional. Quando informado, valida que
     * a doação existe.</p>
     *
     * <p>Default {@code status = DISPONIVEL} quando o request não especifica.</p>
     */
    @Transactional
    public EquipamentoResponse criarEquipamentoManual(EquipamentoRequest request, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);

        Doacao doacao = null;
        if (request.doacaoId() != null) {
            doacao = doacaoRepository.findById(request.doacaoId())
                    .orElseThrow(() -> new BusinessException(
                            "Doação não encontrada com ID: " + request.doacaoId()));
        }

        Equipamento equipamento = Equipamento.builder()
                .tipo(request.tipo())
                .descricao(request.descricao())
                .estadoConservacao(request.estadoConservacao())
                .status(request.status() != null ? request.status() : StatusEquipamento.DISPONIVEL)
                .doacao(doacao)
                .build();

        Equipamento salvo = equipamentoRepository.save(equipamento);

        registrarLog(admin, AcaoTipo.CRIAR_EQUIPAMENTO, salvo.getId(),
                "Equipamento '" + salvo.getTipo() + "' cadastrado manualmente"
                        + (doacao != null ? " (vinculado à doação #" + doacao.getId() + ")" : ""));

        return EquipamentoResponse.from(salvo);
    }

    /**
     * Atualiza um equipamento existente.
     *
     * <p>Regras de transição:</p>
     * <ul>
     *   <li>{@code tipo}, {@code descricao}, {@code estadoConservacao} sempre editáveis.</li>
     *   <li>{@code doacaoId} (vínculo) só pode ser alterado enquanto status = DISPONIVEL.
     *       Após reserva/entrega o vínculo trava para preservar histórico de origem.</li>
     *   <li>{@code status} pode ser corrigido pelo admin, mas mudanças válidas dependem
     *       do fluxo de inventário (DISPONIVEL→RESERVADO→ENTREGUE). Permitido aqui:
     *       só dentro de DISPONIVEL (ou seja, status no request deve ser null ou DISPONIVEL
     *       enquanto o equipamento estiver DISPONIVEL). Para reservar/entregar use os
     *       endpoints dedicados (atribuir/entregar).</li>
     * </ul>
     */
    @Transactional
    public EquipamentoResponse atualizarEquipamento(Integer id, EquipamentoRequest request, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);

        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Equipamento não encontrado com ID: " + id));

        boolean disponivel = equipamento.isDisponivel();

        // Vínculo com doação só pode mudar enquanto disponível
        Integer doacaoAtualId = equipamento.getDoacao() != null ? equipamento.getDoacao().getId() : null;
        Integer doacaoNovaId = request.doacaoId();
        boolean trocouDoacao = !java.util.Objects.equals(doacaoAtualId, doacaoNovaId);

        if (trocouDoacao && !disponivel) {
            throw new BusinessException(
                    "O vínculo com doação só pode ser alterado enquanto o equipamento está disponível. " +
                    "Status atual: " + equipamento.getStatus());
        }

        if (trocouDoacao) {
            Doacao novaDoacao = null;
            if (doacaoNovaId != null) {
                novaDoacao = doacaoRepository.findById(doacaoNovaId)
                        .orElseThrow(() -> new BusinessException(
                                "Doação não encontrada com ID: " + doacaoNovaId));
            }
            equipamento.setDoacao(novaDoacao);
        }

        // Status: só permite manter (null no request) ou ficar DISPONIVEL.
        // Mudanças para RESERVADO/ENTREGUE devem usar atribuirEquipamento/marcarComoEntregue.
        if (request.status() != null && request.status() != equipamento.getStatus()) {
            if (request.status() != StatusEquipamento.DISPONIVEL || !disponivel) {
                throw new BusinessException(
                        "Mudanças de status devem usar os endpoints de atribuição/entrega. " +
                        "Para reverter para DISPONIVEL, o equipamento deve já estar disponível.");
            }
        }

        equipamento.setTipo(request.tipo());
        equipamento.setDescricao(request.descricao());
        equipamento.setEstadoConservacao(request.estadoConservacao());

        Equipamento atualizado = equipamentoRepository.save(equipamento);

        registrarLog(admin, AcaoTipo.EDITAR_EQUIPAMENTO, atualizado.getId(),
                "Equipamento #" + atualizado.getId() + " editado"
                        + (trocouDoacao ? " (vínculo com doação alterado para " + doacaoNovaId + ")" : ""));

        return EquipamentoResponse.from(atualizado);
    }

    /**
     * Soft delete de um equipamento. Só permitido se status = DISPONIVEL —
     * equipamentos reservados ou entregues mantêm o rastro histórico.
     */
    @Transactional
    public void deletarEquipamento(Integer id, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);

        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Equipamento não encontrado com ID: " + id));

        if (!equipamento.isDisponivel()) {
            throw new BusinessException(
                    "Apenas equipamentos disponíveis podem ser excluídos. Status atual: " + equipamento.getStatus());
        }

        registrarLog(admin, AcaoTipo.DELETAR_EQUIPAMENTO, equipamento.getId(),
                "Equipamento '" + equipamento.getTipo() + "' (#" + equipamento.getId() + ") excluído");

        equipamentoRepository.delete(equipamento); // @SQLDelete soft-delete via deletedAt
    }

    /**
     * Cria um equipamento a partir de um item doado aprovado.
     *
     * @deprecated Substituído pelo cadastro manual via
     *             {@link #criarEquipamentoManual(EquipamentoRequest, Integer)}.
     *             O fluxo automático na aprovação de doações foi desligado para
     *             permitir auditoria manual (nem todo item declarado funciona,
     *             e um item pode gerar várias peças aproveitáveis).
     */
    @Deprecated
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
     * Mantido por compatibilidade — prefere {@link #listarEquipamentos(StatusEquipamento, EstadoConservacao, String, Integer)}.
     */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listarEquipamentos(StatusEquipamento status) {
        return listarEquipamentos(status, null, null, null);
    }

    /**
     * Lista equipamentos aplicando filtros combinados (todos opcionais).
     *
     * @param status        DISPONIVEL/RESERVADO/ENTREGUE
     * @param conservacao   NOVO/EXCELENTE/BOM/REGULAR/NECESSITA_REPARO
     * @param origem        {@code "COM_VINCULO"}, {@code "SEM_VINCULO"} ou {@code null}
     *                      (ignorado se {@code doacaoId} for passado)
     * @param doacaoId      filtra por uma doação específica; precede o filtro de origem
     */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listarEquipamentos(StatusEquipamento status,
                                                        EstadoConservacao conservacao,
                                                        String origem,
                                                        Integer doacaoId) {
        Boolean hasDoacao = null;
        // doacaoId tem precedência: se passado, ignora 'origem'
        if (doacaoId == null && origem != null && !origem.isBlank()) {
            String normalizado = origem.trim().toUpperCase();
            if ("COM_VINCULO".equals(normalizado)) {
                hasDoacao = Boolean.TRUE;
            } else if ("SEM_VINCULO".equals(normalizado)) {
                hasDoacao = Boolean.FALSE;
            } else {
                throw new BusinessException(
                        "Filtro 'origem' inválido. Use 'COM_VINCULO' ou 'SEM_VINCULO'.");
            }
        }

        return equipamentoRepository.findWithFilters(status, conservacao, hasDoacao, doacaoId)
                .stream()
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

    /**
     * Valida que o adminId corresponde a um usuário com role ADMIN ou SUPER_ADMIN.
     * Mantido local (em vez de delegar ao AdminService) para evitar dependência circular.
     */
    private Pessoa validarAdmin(Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado"));
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas administradores podem executar esta ação");
        }
        return admin;
    }

    private void registrarLog(Pessoa admin, AcaoTipo acao, Integer entidadeId, String descricao) {
        LogAcao log = LogAcao.builder()
                .admin(admin)
                .acao(acao)
                .entidade("Equipamento")
                .entidadeId(entidadeId)
                .descricao(descricao)
                .dataAcao(LocalDateTime.now())
                .build();
        logAcaoRepository.save(log);
    }
}
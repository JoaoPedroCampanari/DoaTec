package com.doatec.service;

import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.dto.request.RespostaSuporteRequest;
import com.doatec.dto.response.AdminDashboardResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.mapper.DoacaoMapper;
import com.doatec.mapper.PessoaMapper;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.mapper.SuporteMapper;
import com.doatec.model.account.AcaoTipo;
import com.doatec.model.account.LogAcao;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.donation.PreferenciaEntrega;
import com.doatec.model.notification.TipoNotificacao;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.repository.EquipamentoRepository;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.LogAcaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private SuporteFormularioRepository suporteRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private LogAcaoRepository logAcaoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private EmailService emailService;

    // ==================== DASHBOARD ====================

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalDoacoes = doacaoRepository.count();
        long totalSolicitacoesPendentes = solicitacaoRepository.countByStatus(StatusSolicitacao.EM_ANALISE);
        long totalTicketsAbertos = suporteRepository.countByStatus(StatusSuporte.ABERTO);
        long totalUsuariosAtivos = pessoaRepository.countByAtivoTrue();

        long doacoesAprovadas = doacaoRepository.countAprovadasHistoricas();
        long doacoesRejeitadas = doacaoRepository.countByStatus(StatusDoacao.RECUSADO);

        long solicitacoesAprovadas = solicitacaoRepository.countByStatus(StatusSolicitacao.APROVADA);
        long solicitacoesRejeitadas = solicitacaoRepository.countByStatus(StatusSolicitacao.REJEITADA);

        return AdminDashboardResponse.builder()
                .totalDoacoes(totalDoacoes)
                .totalSolicitacoesPendentes(totalSolicitacoesPendentes)
                .totalTicketsAbertos(totalTicketsAbertos)
                .totalUsuariosAtivos(totalUsuariosAtivos)
                .doacoesAprovadas(doacoesAprovadas)
                .doacoesRejeitadas(doacoesRejeitadas)
                .solicitacoesAprovadas(solicitacoesAprovadas)
                .solicitacoesRejeitadas(solicitacoesRejeitadas)
                .build();
    }

    // ==================== DOAÇÕES ====================

    @Transactional(readOnly = true)
    public Page<DoacaoResponse> listarDoacoes(StatusDoacao status, Pageable pageable) {
        Page<Doacao> doacoes = status != null
                ? (status == StatusDoacao.APROVADO ? doacaoRepository.findAprovadasHistoricas(pageable) : doacaoRepository.findByStatus(status, pageable))
                : doacaoRepository.findAll(pageable);

        return doacoes.map(DoacaoMapper::toResponse);
    }

    @Transactional
    public DoacaoResponse aprovarDoacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        StatusDoacao proximo = statusInicialAprovado(doacao);
        validarTransicaoDoacao(doacao, proximo);

        doacao.setStatus(proximo);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Equipamentos NÃO são mais criados automaticamente. O admin faz a triagem
        // física e cadastra manualmente em InventarioService.criarEquipamentoManual,
        // opcionalmente vinculando à doação. Nem todo item declarado funciona, e um
        // item pode gerar várias peças (HD/RAM/bateria de um mesmo notebook).

        // Notificar o doador
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Doação aceita",
                "Sua doação #" + doacao.getId() + " foi aprovada! Obrigado por contribuir com o DoaTec.",
                TipoNotificacao.DOACAO_APROVADA,
                doacao.getId(),
                "DOACAO"
        );

        registrarLog(admin, AcaoTipo.APROVAR_DOACAO, "Doacao", id, "Doação aprovada");

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    @Transactional
    public DoacaoResponse rejeitarDoacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        validarTransicaoDoacao(doacao, StatusDoacao.RECUSADO);

        doacao.setStatus(StatusDoacao.RECUSADO);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Notificar o doador
        String mensagem = "Infelizmente sua doação #" + doacao.getId() + " foi recusada.";
        if (request != null && request.observacao() != null) {
            mensagem += " Motivo: " + request.observacao();
        }
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Doação Recusada",
                mensagem,
                TipoNotificacao.DOACAO_REJEITADA,
                doacao.getId(),
                "DOACAO"
        );

        registrarLog(admin, AcaoTipo.REJEITAR_DOACAO, "Doacao", id, "Doação rejeitada");

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    @Transactional
    public DoacaoResponse alterarStatusDoacao(Integer id, StatusDoacao novoStatus, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        // Validar transição de status
        validarTransicaoDoacao(doacao, novoStatus);

        StatusDoacao statusAnterior = doacao.getStatus();
        doacao.setStatus(novoStatus);
        if (statusAnterior == StatusDoacao.EM_ANALISE) {
            doacao.setAdminAvaliador(admin);
            doacao.setDataAvaliacao(LocalDateTime.now());
        }
        if (request != null && request.observacao() != null && !request.observacao().isBlank()) {
            doacao.setObservacaoAdmin(request.observacao().trim());
        }

        // Criação automática de equipamentos desligada — admin faz triagem manual.
        // Ver InventarioService.criarEquipamentoManual.

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Notificar doador sobre mudança de status
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Status da Doação Atualizado",
                "Sua doação #" + doacao.getId() + " está com o status: " + rotuloStatusDoacao(novoStatus) + "." + (novoStatus == StatusDoacao.CANCELADA ? "" : " Combine os detalhes pelo chat."),
                novoStatus == StatusDoacao.RECUSADO ? TipoNotificacao.DOACAO_REJEITADA
                        : statusAnterior == StatusDoacao.EM_ANALISE ? TipoNotificacao.DOACAO_APROVADA
                        : TipoNotificacao.DOACAO_STATUS_ATUALIZADO,
                doacao.getId(),
                "DOACAO"
        );

        registrarLog(admin, AcaoTipo.ALTERAR_STATUS_DOACAO, "Doacao", id,
                "Status da doação alterado de " + statusAnterior.name() + " para " + novoStatus.name());

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    private StatusDoacao statusInicialAprovado(Doacao doacao) {
        if (doacao.getPreferenciaEntrega() == PreferenciaEntrega.PONTO_DE_COLETA) return StatusDoacao.AGUARDANDO_ENTREGA;
        if (doacao.getPreferenciaEntrega() == PreferenciaEntrega.SOLICITAR_RETIRADA) return StatusDoacao.COLETA_AGENDADA;
        throw new BusinessException("A modalidade de entrega da doação não foi informada.");
    }

    private void validarTransicaoDoacao(Doacao doacao, StatusDoacao novo) {
        StatusDoacao atual = doacao.getStatus();
        boolean entrega = doacao.getPreferenciaEntrega() == PreferenciaEntrega.PONTO_DE_COLETA;
        boolean coleta = doacao.getPreferenciaEntrega() == PreferenciaEntrega.SOLICITAR_RETIRADA;
        boolean valida = switch (atual) {
            case EM_ANALISE -> novo == StatusDoacao.RECUSADO || entrega && novo == StatusDoacao.AGUARDANDO_ENTREGA || coleta && novo == StatusDoacao.COLETA_AGENDADA;
            case APROVADO -> entrega && (novo == StatusDoacao.AGUARDANDO_ENTREGA || novo == StatusDoacao.ENTREGUE) || coleta && (novo == StatusDoacao.COLETA_AGENDADA || novo == StatusDoacao.RETIRADO);
            case AGUARDANDO_ENTREGA -> entrega && (novo == StatusDoacao.ENTREGUE || novo == StatusDoacao.CANCELADA);
            case COLETA_AGENDADA -> coleta && (novo == StatusDoacao.RETIRADO || novo == StatusDoacao.CANCELADA);
            default -> false;
        };
        if (!valida) throw new BusinessException("Transição de status inválida para esta modalidade de doação.");
    }

    private String rotuloStatusDoacao(StatusDoacao status) {
        return switch (status) {
            case EM_ANALISE -> "Em análise";
            case RECUSADO -> "Recusado";
            case AGUARDANDO_ENTREGA -> "Aguardando entrega";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
            case COLETA_AGENDADA -> "Coleta agendada";
            case RETIRADO -> "Retirado";
            case APROVADO -> "Aprovado";
        };
    }

    // ==================== SOLICITAÇÕES ====================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponse> listarSolicitacoes(StatusSolicitacao status, Pageable pageable) {
        Page<SolicitacaoHardware> solicitacoes = status != null
                ? solicitacaoRepository.findByStatus(status, pageable)
                : solicitacaoRepository.findAll(pageable);

        return solicitacoes.map(SolicitacaoMapper::toResponse);
    }

    private void validarTransicaoSolicitacao(StatusSolicitacao atual, StatusSolicitacao novo) {
        boolean transicaoValida = switch (atual) {
            case EM_ANALISE -> novo == StatusSolicitacao.APROVADA || novo == StatusSolicitacao.REJEITADA;
            case APROVADA -> novo == StatusSolicitacao.CONCLUIDA || novo == StatusSolicitacao.CANCELADA;
            case REJEITADA -> false;
            case CONCLUIDA, CANCELADA -> false;
        };

        if (!transicaoValida) {
            throw new BusinessException(
                    "Transição de status inválida para solicitação: " + atual + " -> " + novo);
        }
    }

    @Transactional
    public SolicitacaoResponse aprovarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        throw new BusinessException("Selecione pelo menos um equipamento para aprovar a solicitação.");
    }

    @Transactional
    public SolicitacaoResponse aprovarComEquipamentos(Integer id, Integer adminId, java.util.List<Integer> ids, String observacao) {
        return vincularEquipamentos(id, adminId, ids, observacao, true);
    }

    @Transactional
    public SolicitacaoResponse adicionarEquipamentos(Integer id, Integer adminId, java.util.List<Integer> ids, String observacao) {
        return vincularEquipamentos(id, adminId, ids, observacao, false);
    }

    private SolicitacaoResponse vincularEquipamentos(Integer id, Integer adminId, java.util.List<Integer> ids, String observacao, boolean aprovar) {
        if (ids == null || ids.isEmpty() || ids.size() > 100 || ids.stream().anyMatch(i -> i == null || i <= 0))
            throw new BusinessException("Selecione pelo menos um equipamento válido.");
        if (new java.util.HashSet<>(ids).size() != ids.size())
            throw new BusinessException("Não é possível selecionar o mesmo equipamento mais de uma vez.");
        SolicitacaoHardware solicitacao = solicitacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));
        Pessoa admin = validarAdmin(adminId);
        if (aprovar) validarTransicaoSolicitacao(solicitacao.getStatus(), StatusSolicitacao.APROVADA);
        else if (solicitacao.getStatus() != StatusSolicitacao.APROVADA)
            throw new BusinessException("Somente solicitações aprovadas podem receber equipamentos adicionais.");

        java.util.List<Equipamento> selecionados = new java.util.ArrayList<>();
        for (Integer equipamentoId : ids.stream().sorted().toList()) {
            Equipamento equipamento = equipamentoRepository.findByIdForUpdate(equipamentoId)
                    .orElseThrow(() -> new BusinessException("Equipamento #" + equipamentoId + " não encontrado."));
            if (equipamento.getStatus() != StatusEquipamento.DISPONIVEL)
                throw new BusinessException("O equipamento #" + equipamentoId + " não está mais disponível. Atualize a lista.");
            selecionados.add(equipamento);
        }
        for (Equipamento equipamento : selecionados) {
            equipamento.setStatus(StatusEquipamento.RESERVADO);
            equipamento.setSolicitacaoDestino(solicitacao);
            equipamento.setAlunoDestinatario(solicitacao.getAluno());
            equipamento.setDataAtribuicao(LocalDateTime.now());
            equipamentoRepository.save(equipamento);
        }
        if (aprovar) {
            solicitacao.setStatus(StatusSolicitacao.APROVADA);
            solicitacao.setAdminAvaliador(admin);
            solicitacao.setDataAvaliacao(LocalDateTime.now());
            if (observacao != null && !observacao.isBlank()) solicitacao.setObservacaoAdmin(observacao.trim());
            solicitacaoRepository.save(solicitacao);
            notificacaoService.criarNotificacao(solicitacao.getAluno().getId(), "Solicitação Aprovada",
                    "Sua solicitação #" + id + " foi aprovada e possui equipamento reservado. Acompanhe os próximos passos pelo chat.",
                    TipoNotificacao.SOLICITACAO_APROVADA, id, "SOLICITACAO");
        }
        registrarLog(admin, AcaoTipo.APROVAR_SOLICITACAO, "SolicitacaoHardware", id,
                (aprovar ? "Solicitação aprovada" : "Equipamentos adicionais atribuídos") + "; equipamentos: "
                        + ids.stream().sorted().map(String::valueOf).collect(java.util.stream.Collectors.joining(", ")));
        return SolicitacaoMapper.toResponse(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<com.doatec.dto.response.EquipamentoResponse> listarEquipamentosSolicitacao(Integer solicitacaoId) {
        if (!solicitacaoRepository.existsById(solicitacaoId))
            throw new BusinessException("Solicitação não encontrada.");
        return equipamentoRepository.findBySolicitacaoDestinoId(solicitacaoId).stream()
                .map(com.doatec.dto.response.EquipamentoResponse::from).toList();
    }

    @Transactional
    public void removerEquipamentoSolicitacao(Integer solicitacaoId, Integer equipamentoId, Integer adminId, boolean ultimoEsperado) {
        Pessoa admin = validarAdmin(adminId);
        SolicitacaoHardware solicitacao = solicitacaoRepository.findByIdForUpdate(solicitacaoId)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada."));
        if (solicitacao.getStatus() != StatusSolicitacao.APROVADA)
            throw new BusinessException("Só é possível remover equipamentos de solicitações aprovadas.");
        List<Equipamento> vinculados = equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(solicitacaoId);
        Equipamento equipamento = vinculados.stream().filter(e -> e.getId().equals(equipamentoId)).findFirst()
                .orElseThrow(() -> new BusinessException("Equipamento não está vinculado a esta solicitação."));
        if (equipamento.getStatus() != StatusEquipamento.RESERVADO)
            throw new BusinessException("Somente equipamentos reservados podem ser removidos; equipamentos entregues não podem ser removidos.");
        boolean voltouAnalise = vinculados.size() == 1;
        if (voltouAnalise != ultimoEsperado)
            throw new BusinessException("A lista de equipamentos mudou. Atualize e tente novamente.");
        equipamento.setStatus(StatusEquipamento.DISPONIVEL);
        equipamento.setSolicitacaoDestino(null);
        equipamento.setAlunoDestinatario(null);
        equipamento.setDataAtribuicao(null);
        equipamentoRepository.save(equipamento);
        if (voltouAnalise) {
            solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
            solicitacao.setAdminAvaliador(null);
            solicitacao.setDataAvaliacao(null);
            solicitacao.setObservacaoAdmin(null);
            solicitacaoRepository.save(solicitacao);
            notificacaoService.criarNotificacao(solicitacao.getAluno().getId(), "Solicitação em análise",
                    "Sua solicitação #" + solicitacaoId + " voltou para análise após a retirada do equipamento reservado.",
                    TipoNotificacao.SOLICITACAO_STATUS_ATUALIZADO, solicitacaoId, "SOLICITACAO");
        }
        registrarLog(admin, AcaoTipo.EDITAR_EQUIPAMENTO, "Equipamento", equipamentoId,
                "Equipamento removido da solicitação #" + solicitacaoId + (voltouAnalise ? "; solicitação voltou para análise" : ""));
    }

    @Transactional
    public SolicitacaoResponse rejeitarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), StatusSolicitacao.REJEITADA);

        solicitacao.setStatus(StatusSolicitacao.REJEITADA);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        // Notificar o aluno
        String mensagem = "Infelizmente sua solicitação #" + solicitacao.getId() + " foi rejeitada.";
        if (request != null && request.observacao() != null) {
            mensagem += " Motivo: " + request.observacao();
        }
        notificacaoService.criarNotificacao(
                solicitacao.getAluno().getId(),
                "Solicitação Rejeitada",
                mensagem,
                TipoNotificacao.SOLICITACAO_REJEITADA,
                solicitacao.getId(),
                "SOLICITACAO"
        );

        registrarLog(admin, AcaoTipo.REJEITAR_SOLICITACAO, "SolicitacaoHardware", id, "Solicitação rejeitada");

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    private void entregarEquipamentosDaSolicitacao(Integer solicitacaoId) {
        List<Equipamento> equipamentos = equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(solicitacaoId);
        if (equipamentos.stream().anyMatch(e -> e.getStatus() != StatusEquipamento.RESERVADO && e.getStatus() != StatusEquipamento.ENTREGUE))
            throw new BusinessException("Há equipamento vinculado com status inválido. Verifique o inventário antes de concluir.");
        LocalDateTime dataEntrega = LocalDateTime.now();
        for (Equipamento equipamento : equipamentos) {
            if (equipamento.getStatus() == StatusEquipamento.RESERVADO) {
                equipamento.setStatus(StatusEquipamento.ENTREGUE);
                equipamento.setDataEntrega(dataEntrega);
                equipamentoRepository.save(equipamento);
            }
        }
    }

    @Transactional
    public SolicitacaoResponse concluirSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), StatusSolicitacao.CONCLUIDA);
        entregarEquipamentosDaSolicitacao(id);

        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        notificacaoService.criarNotificacao(
                solicitacao.getAluno().getId(),
                "Solicitação Concluída",
                "Sua solicitação #" + solicitacao.getId() + " foi concluída com sucesso!",
                TipoNotificacao.SOLICITACAO_CONCLUIDA,
                solicitacao.getId(),
                "SOLICITACAO"
        );

        registrarLog(admin, AcaoTipo.CONCLUIR_SOLICITACAO, "SolicitacaoHardware", id, "Solicitação concluída");

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    @Transactional
    public SolicitacaoResponse alterarStatusSolicitacao(Integer id, StatusSolicitacao novoStatus, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), novoStatus);
        if (novoStatus == StatusSolicitacao.APROVADA)
            throw new BusinessException("Selecione pelo menos um equipamento para aprovar a solicitação.");

        if (novoStatus == StatusSolicitacao.CONCLUIDA) entregarEquipamentosDaSolicitacao(id);

        StatusSolicitacao statusAnterior = solicitacao.getStatus();
        List<Equipamento> equipamentosLiberados = java.util.List.of();
        if (novoStatus == StatusSolicitacao.CANCELADA) {
            List<Equipamento> vinculados = equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(id);
            if (vinculados.stream().anyMatch(e -> e.getStatus() == StatusEquipamento.ENTREGUE)) {
                throw new BusinessException("Não é possível cancelar: há equipamento já entregue nesta solicitação.");
            }
            equipamentosLiberados = vinculados;
            for (Equipamento equipamento : equipamentosLiberados) {
                equipamento.setStatus(StatusEquipamento.DISPONIVEL);
                equipamento.setSolicitacaoDestino(null);
                equipamento.setAlunoDestinatario(null);
                equipamento.setDataAtribuicao(null);
                equipamentoRepository.save(equipamento);
            }
        }
        solicitacao.setStatus(novoStatus);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        notificacaoService.criarNotificacao(
                solicitacao.getAluno().getId(),
                "Status da Solicitação Atualizado",
                "O status da sua solicitação #" + solicitacao.getId() + " foi alterado de " + statusAnterior.name() + " para " + novoStatus.name() + ".",
                novoStatus == StatusSolicitacao.REJEITADA ? TipoNotificacao.SOLICITACAO_REJEITADA
                        : novoStatus == StatusSolicitacao.CONCLUIDA ? TipoNotificacao.SOLICITACAO_CONCLUIDA
                        : TipoNotificacao.SOLICITACAO_STATUS_ATUALIZADO,
                solicitacao.getId(),
                "SOLICITACAO"
        );

        registrarLog(admin, AcaoTipo.ALTERAR_STATUS_SOLICITACAO, "SolicitacaoHardware", id,
                "Status da solicitação alterado de " + statusAnterior.name() + " para " + novoStatus.name()
                        + (equipamentosLiberados.isEmpty() ? "" : "; equipamentos liberados: " + equipamentosLiberados.stream().map(e -> String.valueOf(e.getId())).collect(java.util.stream.Collectors.joining(", "))));

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    // ==================== SUPORTE ====================

    @Transactional(readOnly = true)
    public Page<SuporteResponse> listarTickets(StatusSuporte status, Pageable pageable) {
        Page<SuporteFormulario> tickets = status != null
                ? suporteRepository.findByStatus(status, pageable)
                : suporteRepository.findAll(pageable);

        return tickets.map(SuporteMapper::toResponse);
    }

    @Transactional
    public SuporteResponse responderTicket(Integer id, Integer adminId, RespostaSuporteRequest request) {
        SuporteFormulario ticket = suporteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Ticket não encontrado com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        validarTicketAberto(ticket);
        if (ticket.getResposta() != null && !ticket.getResposta().isBlank())
            throw new BusinessException("Este ticket já foi respondido. Continue o atendimento pelo chat.");
        ticket.setResposta(request.resposta());
        ticket.setAdminResponsavel(admin);
        ticket.setStatus(StatusSuporte.EM_ANDAMENTO);
        ticket.setDataResolucao(null);

        SuporteFormulario ticketAtualizado = suporteRepository.save(ticket);

        registrarLog(admin, AcaoTipo.RESPONDER_SUPORTE, "SuporteFormulario", id, "Ticket respondido");

        // Enviar email de notificacao ao usuario
        try {
            emailService.enviarSuporteResposta(
                    ticket.getAutor().getEmail(),
                    ticket.getAutor().getNome(),
                    ticket.getAssunto(),
                    request.resposta()
            );
        } catch (Exception e) {
            // Log the error but don't fail the ticket response
            log.warn("Falha ao enviar email de resposta para ticket {}: {}", id, e.getMessage());
        }

        return SuporteMapper.toResponse(ticketAtualizado);
    }

    @Transactional
    public SuporteResponse atualizarStatusTicket(Integer id, StatusSuporte novoStatus) {
        SuporteFormulario ticket = suporteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Ticket não encontrado com ID: " + id));

        validarTicketAberto(ticket);
        if (ticket.getStatus() == StatusSuporte.ABERTO)
            throw new BusinessException("O ticket só passa para em andamento após a primeira resposta.");
        if (ticket.getStatus() != StatusSuporte.EM_ANDAMENTO ||
                (novoStatus != StatusSuporte.RESOLVIDO && novoStatus != StatusSuporte.FECHADO))
            throw new BusinessException("Um ticket em andamento só pode ser marcado como resolvido ou fechado.");
        ticket.setStatus(novoStatus);
        ticket.setDataResolucao(LocalDateTime.now());

        SuporteFormulario ticketAtualizado = suporteRepository.save(ticket);

        return SuporteMapper.toResponse(ticketAtualizado);
    }

    private void validarTicketAberto(SuporteFormulario ticket) {
        if (ticket.getStatus() == StatusSuporte.RESOLVIDO || ticket.getStatus() == StatusSuporte.FECHADO) {
            throw new BusinessException("Tickets resolvidos ou fechados não podem ser alterados.");
        }
    }

    // ==================== USUÁRIOS ====================

    @Transactional(readOnly = true)
    public Page<UsuarioAdminResponse> listarTodosUsuarios(Pageable pageable) {
        return pessoaRepository.findAll(pageable)
                .map(PessoaMapper::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuariosPorTipoPessoa(TipoPessoa tipoPessoa) {
        return pessoaRepository.findByTipo(tipoPessoa.getClasse()).stream()
                .map(PessoaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuariosPorRole(Role role) {
        return pessoaRepository.findByRole(role).stream()
                .map(PessoaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioAdminResponse alterarStatusUsuario(Integer id, Boolean ativo, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);

        if (id.equals(adminId)) {
            throw new BusinessException("Você não pode alterar seu próprio status.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        // Admins normais não podem alterar status de outros admins/super_admins
        if (pessoa.isAdmin() && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas Super Admins podem alterar status de outros administradores.");
        }

        pessoa.setAtivo(ativo);
        pessoa.markUpdated();
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        registrarLog(admin, ativo ? AcaoTipo.REATIVAR_USUARIO : AcaoTipo.DESATIVAR_USUARIO,
                "Pessoa", id, ativo ? "Usuário reativado" : "Usuário desativado");

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    @Transactional
    public UsuarioAdminResponse alterarRoleUsuario(Integer id, Role novaRole, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);
        if (admin.getRole() != Role.SUPER_ADMIN)
            throw new BusinessException("Apenas Super Admins podem alterar roles de usuários.");

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        pessoa.setRole(novaRole);
        pessoa.markUpdated();
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Pessoa validarAdmin(Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado com ID: " + adminId));

        if (!admin.isAdmin()) {
            throw new BusinessException("Apenas administradores podem realizar esta operação.");
        }

        return admin;
    }

    private void registrarLog(Pessoa admin, AcaoTipo acao, String entidade, Integer entidadeId, String descricao) {
        LogAcao log = LogAcao.builder()
                .admin(admin)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .descricao(descricao)
                .dataAcao(LocalDateTime.now())
                .build();

        logAcaoRepository.save(log);
    }

}
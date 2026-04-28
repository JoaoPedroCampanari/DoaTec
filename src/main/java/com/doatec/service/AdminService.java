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
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.notification.TipoNotificacao;
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
    private SuporteFormularioRepository suporteRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private LogAcaoRepository logAcaoRepository;

    @Autowired
    private InventarioService inventarioService;

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

        long doacoesAprovadas = doacaoRepository.countByStatus(StatusDoacao.FINALIZADO);
        long doacoesRejeitadas = doacaoRepository.countByStatus(StatusDoacao.REJEITADA);

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
                ? doacaoRepository.findByStatus(status, pageable)
                : doacaoRepository.findAll(pageable);

        return doacoes.map(DoacaoMapper::toResponse);
    }

    @Transactional
    public DoacaoResponse aprovarDoacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        validarTransicaoDoacao(doacao.getStatus(), StatusDoacao.FINALIZADO);

        doacao.setStatus(StatusDoacao.FINALIZADO);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Criar equipamentos no inventário para cada item doado
        for (ItemDoado item : doacao.getItens()) {
            EstadoConservacao estado = inferirEstadoConservacao(request);
            inventarioService.criarEquipamento(item, estado);
        }

        // Notificar o doador
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Doação Aprovada",
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
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        validarTransicaoDoacao(doacao.getStatus(), StatusDoacao.REJEITADA);

        doacao.setStatus(StatusDoacao.REJEITADA);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Notificar o doador
        String mensagem = "Infelizmente sua doação #" + doacao.getId() + " foi rejeitada.";
        if (request != null && request.observacao() != null) {
            mensagem += " Motivo: " + request.observacao();
        }
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Doação Rejeitada",
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
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        // Validar transição de status
        validarTransicaoDoacao(doacao.getStatus(), novoStatus);

        StatusDoacao statusAnterior = doacao.getStatus();
        doacao.setStatus(novoStatus);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        // Criar equipamentos no inventário SOMENTE quando transicionar para FINALIZADO
        if (novoStatus == StatusDoacao.FINALIZADO) {
            for (ItemDoado item : doacao.getItens()) {
                EstadoConservacao estado = inferirEstadoConservacao(request);
                inventarioService.criarEquipamento(item, estado);
            }
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        // Notificar doador sobre mudança de status
        notificacaoService.criarNotificacao(
                doacao.getDoador().getId(),
                "Status da Doação Atualizado",
                "O status da sua doação #" + doacao.getId() + " foi alterado de " + statusAnterior.name() + " para " + novoStatus.name() + ".",
                novoStatus == StatusDoacao.REJEITADA ? TipoNotificacao.DOACAO_REJEITADA
                        : novoStatus == StatusDoacao.FINALIZADO ? TipoNotificacao.DOACAO_APROVADA
                        : TipoNotificacao.DOACAO_STATUS_ATUALIZADO,
                doacao.getId(),
                "DOACAO"
        );

        registrarLog(admin, AcaoTipo.ALTERAR_STATUS_DOACAO, "Doacao", id,
                "Status da doação alterado de " + statusAnterior.name() + " para " + novoStatus.name());

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    private void validarTransicaoDoacao(StatusDoacao atual, StatusDoacao novo) {
        if (atual == novo) {
            throw new BusinessException("Doação já está com status " + atual.name());
        }
        if (atual == StatusDoacao.FINALIZADO) {
            throw new BusinessException("Doação com status " + atual.name() + " não pode ser alterada.");
        }

        boolean transicaoValida = switch (atual) {
            case EM_TRIAGEM -> novo == StatusDoacao.AGUARDANDO_COLETA || novo == StatusDoacao.REJEITADA;
            case AGUARDANDO_COLETA -> novo == StatusDoacao.RECEBIDO || novo == StatusDoacao.REJEITADA;
            case RECEBIDO -> novo == StatusDoacao.EM_ANALISE || novo == StatusDoacao.REJEITADA;
            case EM_ANALISE -> novo == StatusDoacao.FINALIZADO || novo == StatusDoacao.REJEITADA;
            case REJEITADA -> novo == StatusDoacao.EM_TRIAGEM;
            default -> false;
        };

        if (!transicaoValida) {
            throw new BusinessException("Transição de status inválida: " + atual.name() + " → " + novo.name());
        }
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
            case APROVADA -> novo == StatusSolicitacao.CONCLUIDA;
            case REJEITADA -> novo == StatusSolicitacao.EM_ANALISE;
            case CONCLUIDA -> false;
        };

        if (!transicaoValida) {
            throw new BusinessException(
                    "Transição de status inválida para solicitação: " + atual + " -> " + novo);
        }
    }

    @Transactional
    public SolicitacaoResponse aprovarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), StatusSolicitacao.APROVADA);

        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        // Notificar o aluno
        notificacaoService.criarNotificacao(
                solicitacao.getAluno().getId(),
                "Solicitação Aprovada",
                "Sua solicitação #" + solicitacao.getId() + " foi aprovada! Aguarde a disponibilidade de um equipamento compatível.",
                TipoNotificacao.SOLICITACAO_APROVADA,
                solicitacao.getId(),
                "SOLICITACAO"
        );

        registrarLog(admin, AcaoTipo.APROVAR_SOLICITACAO, "SolicitacaoHardware", id, "Solicitação aprovada");

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    @Transactional
    public SolicitacaoResponse rejeitarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
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

    @Transactional
    public SolicitacaoResponse concluirSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), StatusSolicitacao.CONCLUIDA);

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
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);
        validarTransicaoSolicitacao(solicitacao.getStatus(), novoStatus);

        StatusSolicitacao statusAnterior = solicitacao.getStatus();
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
                "Status da solicitação alterado de " + statusAnterior.name() + " para " + novoStatus.name());

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
        SuporteFormulario ticket = suporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ticket não encontrado com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        ticket.setResposta(request.resposta());
        ticket.setAdminResponsavel(admin);
        ticket.setStatus(StatusSuporte.RESOLVIDO);
        ticket.setDataResolucao(LocalDateTime.now());

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
        SuporteFormulario ticket = suporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ticket não encontrado com ID: " + id));

        ticket.setStatus(novoStatus);

        SuporteFormulario ticketAtualizado = suporteRepository.save(ticket);

        return SuporteMapper.toResponse(ticketAtualizado);
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
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        registrarLog(admin, ativo ? AcaoTipo.REATIVAR_USUARIO : AcaoTipo.DESATIVAR_USUARIO,
                "Pessoa", id, ativo ? "Usuário reativado" : "Usuário desativado");

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    @Transactional
    public UsuarioAdminResponse alterarRoleUsuario(Integer id, Role novaRole, Integer adminId) {
        Pessoa admin = validarAdmin(adminId);

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        // Admins normais só podem alterar role de USERs
        if (pessoa.isAdmin() && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas Super Admins podem alterar roles de outros administradores.");
        }

        // Admins normais não podem promover a ADMIN ou SUPER_ADMIN
        if ((novaRole == Role.ADMIN || novaRole == Role.SUPER_ADMIN) && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas Super Admins podem promover usuários a administradores.");
        }

        pessoa.setRole(novaRole);
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

    /**
     * Infere o estado de conservação do item baseado no request.
     * Por enquanto usa um valor padrão, mas pode ser expandido.
     */
    private EstadoConservacao inferirEstadoConservacao(AvaliacaoRequest request) {
        // Por enquanto, usa BOM como padrão.
        // No futuro, o request pode incluir campos para avaliar cada item.
        return EstadoConservacao.BOM;
    }
}
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
                "Sua doação #" + doacao.getId() + " está com o status: " + rotuloStatusDoacao(novoStatus) + ". Combine os detalhes pelo chat.",
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
            case AGUARDANDO_ENTREGA -> entrega && novo == StatusDoacao.ENTREGUE;
            case COLETA_AGENDADA -> coleta && novo == StatusDoacao.RETIRADO;
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
            case APROVADA -> novo == StatusSolicitacao.CONCLUIDA;
            case REJEITADA -> false;
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

        validarTicketAberto(ticket);
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

        validarTicketAberto(ticket);
        if (ticket.getStatus() == novoStatus) {
            throw new BusinessException("O ticket já está com este status.");
        }
        ticket.setStatus(novoStatus);
        if (novoStatus == StatusSuporte.RESOLVIDO || novoStatus == StatusSuporte.FECHADO) {
            ticket.setDataResolucao(LocalDateTime.now());
        }

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
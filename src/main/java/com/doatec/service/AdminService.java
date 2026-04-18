package com.doatec.service;

import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.dto.request.RespostaSuporteRequest;
import com.doatec.dto.response.AdminDashboardResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

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

    // ==================== DASHBOARD ====================

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalDoacoes = doacaoRepository.count();
        long totalSolicitacoesPendentes = solicitacaoRepository.findByStatus(StatusSolicitacao.EM_ANALISE).size();
        long totalTicketsAbertos = suporteRepository.findByStatus(StatusSuporte.ABERTO).size();
        long totalUsuariosAtivos = pessoaRepository.findByAtivoTrue().size();

        long doacoesAprovadas = doacaoRepository.findByStatus(StatusDoacao.FINALIZADO).size();
        long doacoesRejeitadas = doacaoRepository.findByStatus(StatusDoacao.AGUARDANDO_COLETA).size();

        long solicitacoesAprovadas = solicitacaoRepository.findByStatus(StatusSolicitacao.APROVADA).size();
        long solicitacoesRejeitadas = solicitacaoRepository.findByStatus(StatusSolicitacao.REJEITADA).size();

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
    public List<DoacaoResponse> listarDoacoes(StatusDoacao status) {
        List<Doacao> doacoes = status != null
                ? doacaoRepository.findByStatus(status)
                : doacaoRepository.findAll();

        return doacoes.stream()
                .map(DoacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoacaoResponse aprovarDoacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        doacao.setStatus(StatusDoacao.FINALIZADO);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        registrarLog(admin, AcaoTipo.APROVAR_DOACAO, "Doacao", id, "Doação aprovada");

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    @Transactional
    public DoacaoResponse rejeitarDoacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        doacao.setStatus(StatusDoacao.AGUARDANDO_COLETA);
        doacao.setAdminAvaliador(admin);
        doacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            doacao.setObservacaoAdmin(request.observacao());
        }

        Doacao doacaoAtualizada = doacaoRepository.save(doacao);

        registrarLog(admin, AcaoTipo.REJEITAR_DOACAO, "Doacao", id, "Doação rejeitada");

        return DoacaoMapper.toResponse(doacaoAtualizada);
    }

    // ==================== SOLICITAÇÕES ====================

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarSolicitacoes(StatusSolicitacao status) {
        List<SolicitacaoHardware> solicitacoes = status != null
                ? solicitacaoRepository.findByStatus(status)
                : solicitacaoRepository.findAll();

        return solicitacoes.stream()
                .map(SolicitacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitacaoResponse aprovarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        registrarLog(admin, AcaoTipo.APROVAR_SOLICITACAO, "SolicitacaoHardware", id, "Solicitação aprovada");

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    @Transactional
    public SolicitacaoResponse rejeitarSolicitacao(Integer id, Integer adminId, AvaliacaoRequest request) {
        SolicitacaoHardware solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        solicitacao.setStatus(StatusSolicitacao.REJEITADA);
        solicitacao.setAdminAvaliador(admin);
        solicitacao.setDataAvaliacao(LocalDateTime.now());
        if (request != null && request.observacao() != null) {
            solicitacao.setObservacaoAdmin(request.observacao());
        }

        SolicitacaoHardware solicitacaoAtualizada = solicitacaoRepository.save(solicitacao);

        registrarLog(admin, AcaoTipo.REJEITAR_SOLICITACAO, "SolicitacaoHardware", id, "Solicitação rejeitada");

        return SolicitacaoMapper.toResponse(solicitacaoAtualizada);
    }

    // ==================== SUPORTE ====================

    @Transactional(readOnly = true)
    public List<SuporteResponse> listarTickets(StatusSuporte status) {
        List<SuporteFormulario> tickets = status != null
                ? suporteRepository.findByStatus(status)
                : suporteRepository.findAll();

        return tickets.stream()
                .map(SuporteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SuporteResponse responderTicket(Integer id, Integer adminId, RespostaSuporteRequest request) {
        SuporteFormulario ticket = suporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));

        Pessoa admin = validarAdmin(adminId);

        ticket.setResposta(request.resposta());
        ticket.setAdminResponsavel(admin);
        ticket.setStatus(StatusSuporte.RESOLVIDO);
        ticket.setDataResolucao(LocalDateTime.now());

        SuporteFormulario ticketAtualizado = suporteRepository.save(ticket);

        registrarLog(admin, AcaoTipo.RESPONDER_SUPORTE, "SuporteFormulario", id, "Ticket respondido");

        return SuporteMapper.toResponse(ticketAtualizado);
    }

    @Transactional
    public SuporteResponse atualizarStatusTicket(Integer id, StatusSuporte novoStatus) {
        SuporteFormulario ticket = suporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));

        ticket.setStatus(novoStatus);

        SuporteFormulario ticketAtualizado = suporteRepository.save(ticket);

        return SuporteMapper.toResponse(ticketAtualizado);
    }

    // ==================== USUÁRIOS ====================

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarTodosUsuarios() {
        return pessoaRepository.findAll().stream()
                .map(PessoaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuariosPorTipoPessoa(TipoPessoa tipoPessoa) {
        return pessoaRepository.findByTipoPessoa(tipoPessoa).stream()
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

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

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
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

        pessoa.setRole(novaRole);
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Pessoa validarAdmin(Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado com ID: " + adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Apenas administradores podem realizar esta operação.");
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
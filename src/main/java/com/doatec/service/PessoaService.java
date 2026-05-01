package com.doatec.service;

import com.doatec.dto.request.*;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.*;
import com.doatec.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private DoadorPFRepository doadorPFRepository;

    @Autowired
    private DoadorPJRepository doadorPJRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Pessoa findById(Integer id) {
        return pessoaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Pessoa> findAll() {
        return pessoaRepository.findAll();
    }

    @Transactional
    public void deleteById(Integer id) {
        pessoaRepository.deleteById(id);
    }

    public Pessoa autenticar(LoginRequest loginRequest) {
        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(loginRequest.email());

        if (pessoaOptional.isEmpty()) {
            return null;
        }

        Pessoa pessoa = pessoaOptional.get();

        if (!pessoa.getAtivo()) {
            return null;
        }

        if (passwordEncoder.matches(loginRequest.senha(), pessoa.getSenha())) {
            return pessoa;
        } else {
            return null;
        }
    }

    // ==================== MÉTODOS ESPECIALIZADOS DE REGISTRO ====================

    /**
     * Valida campos obrigatorios de endereco.
     */
    private void validarEndereco(String cep, String logradouro, String numero, String bairro, String cidade, String estado) {
        if (cep == null || cep.isBlank()) {
            throw new BusinessException("O CEP é obrigatório.");
        }
        if (logradouro == null || logradouro.isBlank()) {
            throw new BusinessException("O logradouro é obrigatório.");
        }
        if (numero == null || numero.isBlank()) {
            throw new BusinessException("O número do endereço é obrigatório.");
        }
        if (bairro == null || bairro.isBlank()) {
            throw new BusinessException("O bairro é obrigatório.");
        }
        if (cidade == null || cidade.isBlank()) {
            throw new BusinessException("A cidade é obrigatória.");
        }
        if (estado == null || estado.isBlank()) {
            throw new BusinessException("O estado é obrigatório.");
        }
    }

    /**
     * Registra um novo aluno.
     */
    @Transactional
    public Aluno registrarAluno(AlunoRegistroRequest request) {
        // Validar se email já existe
        if (pessoaRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("O email informado já está cadastrado.");
        }

        // Validar se RA já existe
        if (alunoRepository.existsByRa(request.ra())) {
            throw new BusinessException("O RA informado já está cadastrado.");
        }

        // Validar endereco
        validarEndereco(request.cep(), request.logradouro(), request.numero(), request.bairro(), request.cidade(), request.estado());

        Aluno novoAluno = PessoaMapper.toAluno(request);
        novoAluno.setSenha(passwordEncoder.encode(request.senha()));

        return alunoRepository.save(novoAluno);
    }

    /**
     * Registra um novo doador pessoa física.
     */
    @Transactional
    public DoadorPF registrarDoadorPF(DoadorPFRegistroRequest request) {
        // Validar se email já existe
        if (pessoaRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("O email informado já está cadastrado.");
        }

        // Validar se CPF já existe
        if (doadorPFRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("O CPF informado já está cadastrado.");
        }

        // Validar endereco
        validarEndereco(request.cep(), request.logradouro(), request.numero(), request.bairro(), request.cidade(), request.estado());

        DoadorPF novoDoador = PessoaMapper.toDoadorPF(request);
        novoDoador.setSenha(passwordEncoder.encode(request.senha()));

        return doadorPFRepository.save(novoDoador);
    }

    /**
     * Registra um novo doador pessoa jurídica.
     */
    @Transactional
    public DoadorPJ registrarDoadorPJ(DoadorPJRegistroRequest request) {
        // Validar se email já existe
        if (pessoaRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("O email informado já está cadastrado.");
        }

        // Validar se CNPJ já existe
        if (doadorPJRepository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("O CNPJ informado já está cadastrado.");
        }

        // Validar endereco
        validarEndereco(request.cep(), request.logradouro(), request.numero(), request.bairro(), request.cidade(), request.estado());

        DoadorPJ novoDoador = PessoaMapper.toDoadorPJ(request);
        novoDoador.setSenha(passwordEncoder.encode(request.senha()));

        return doadorPJRepository.save(novoDoador);
    }

    /**
     * Endpoint legado para registro genérico.
     * Mantido para compatibilidade com o frontend atual.
     * @deprecated Usar métodos especializados: registrarAluno, registrarDoadorPF, registrarDoadorPJ
     */
    @Deprecated
    @Transactional
    public Pessoa registrarPessoa(RegistroRequest registroRequest) {
        // Validar se email já existe
        if (pessoaRepository.findByEmail(registroRequest.email()).isPresent()) {
            throw new BusinessException("O email informado já está cadastrado.");
        }

        // Validar se documento já existe
        if (registroRequest.documento() != null && !registroRequest.documento().isBlank()) {
            Optional<Pessoa> pessoaComDocumento = pessoaRepository.findByDocumento(registroRequest.documento());
            if (pessoaComDocumento.isPresent()) {
                throw new BusinessException("O documento " + registroRequest.documento() + " já está cadastrado.");
            }
        } else {
            throw new BusinessException("O documento de identificação é obrigatório.");
        }

        // Validar tipo de pessoa
        TipoPessoa tipoPessoa;
        try {
            tipoPessoa = TipoPessoa.valueOf(registroRequest.tipoPessoa().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de pessoa inválido: " + registroRequest.tipoPessoa() +
                    ". Valores válidos: DOADOR_PF, DOADOR_PJ, ALUNO");
        }

        Pessoa novaPessoa = PessoaMapper.toPessoa(registroRequest);
        novaPessoa.setSenha(passwordEncoder.encode(registroRequest.senha()));

        return pessoaRepository.save(novaPessoa);
    }

    @Transactional
    public Pessoa updatePessoaProfile(Integer id, PessoaUpdateRequest dto) {
        Pessoa pessoaExistente = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        if (dto.email() != null && !dto.email().isBlank() && !dto.email().equals(pessoaExistente.getEmail())) {
            if (pessoaRepository.existsByEmailAndIdNot(dto.email(), id)) {
                throw new BusinessException("Novo email já está cadastrado para outro usuário.");
            }
            pessoaExistente.setEmail(dto.email());
        }

        if (dto.senha() != null && !dto.senha().isBlank()) {
            pessoaExistente.setSenha(passwordEncoder.encode(dto.senha()));
        }

        if (dto.endereco() != null) {
            pessoaExistente.setEndereco(dto.endereco());
        }
        if (dto.logradouro() != null) {
            pessoaExistente.setLogradouro(dto.logradouro());
        }
        if (dto.numero() != null) {
            pessoaExistente.setNumero(dto.numero());
        }
        if (dto.bairro() != null) {
            pessoaExistente.setBairro(dto.bairro());
        }
        if (dto.cidade() != null) {
            pessoaExistente.setCidade(dto.cidade());
        }
        if (dto.estado() != null) {
            pessoaExistente.setEstado(dto.estado());
        }
        if (dto.telefone() != null) {
            pessoaExistente.setTelefone(dto.telefone());
        }

        pessoaExistente.markUpdated();
        return pessoaRepository.save(pessoaExistente);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarTodosUsuarios() {
        return pessoaRepository.findAll().stream()
                .map(PessoaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuariosPorTipoPessoa(TipoPessoa tipoPessoa) {
        // Usar query com TYPE() para filtrar por subclasse
        return switch (tipoPessoa) {
            case ALUNO -> alunoRepository.findAll().stream()
                    .map(PessoaMapper::toAdminResponse)
                    .collect(Collectors.toList());
            case DOADOR_PF -> doadorPFRepository.findAll().stream()
                    .map(PessoaMapper::toAdminResponse)
                    .collect(Collectors.toList());
            case DOADOR_PJ -> doadorPJRepository.findAll().stream()
                    .map(PessoaMapper::toAdminResponse)
                    .collect(Collectors.toList());
        };
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuariosPorRole(Role role) {
        return pessoaRepository.findByRole(role).stream()
                .map(PessoaMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioAdminResponse alterarStatusUsuario(Integer id, Boolean ativo, Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado"));

        if (!admin.isAdmin()) {
            throw new BusinessException("Apenas administradores podem alterar status de usuários.");
        }

        if (id.equals(adminId)) {
            throw new BusinessException("Você não pode alterar seu próprio status.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        // Admins normais não podem alterar status de outros admins
        if (pessoa.isAdmin() && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas Super Admins podem alterar status de outros administradores.");
        }

        pessoa.setAtivo(ativo);
        pessoa.markUpdated();
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    @Transactional
    public UsuarioAdminResponse alterarRoleUsuario(Integer id, Role novaRole, Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("Admin não encontrado"));

        if (!admin.isAdmin()) {
            throw new BusinessException("Apenas administradores podem alterar roles de usuários.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        // Admins normais não podem alterar role de outros admins
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
}
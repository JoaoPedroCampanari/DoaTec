package com.doatec.service;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import com.doatec.repository.PessoaRepository;
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

    @Transactional
    public Pessoa registrarPessoa(RegistroRequest registroRequest) {
        // Validar se email já existe
        if (pessoaRepository.findByEmail(registroRequest.email()).isPresent()) {
            throw new RuntimeException("O email informado já está cadastrado.");
        }

        // Validar se documento já existe
        if (registroRequest.documento() != null && !registroRequest.documento().isBlank()) {
            Optional<Pessoa> pessoaComDocumento = pessoaRepository.findByDocumento(registroRequest.documento());
            if (pessoaComDocumento.isPresent()) {
                throw new RuntimeException("O documento " + registroRequest.documento() + " já está cadastrado.");
            }
        } else {
            throw new RuntimeException("O documento de identificação é obrigatório.");
        }

        // Validar tipo de pessoa
        TipoPessoa tipoPessoa;
        try {
            tipoPessoa = TipoPessoa.valueOf(registroRequest.tipoPessoa().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de pessoa inválido: " + registroRequest.tipoPessoa() +
                    ". Valores válidos: DOADOR_PF, DOADOR_PJ, ALUNO");
        }

        Pessoa novaPessoa = PessoaMapper.toPessoa(registroRequest);
        novaPessoa.setSenha(passwordEncoder.encode(registroRequest.senha()));

        return pessoaRepository.save(novaPessoa);
    }

    @Transactional
    public Pessoa updatePessoaProfile(Integer id, PessoaUpdateRequest dto) {
        Pessoa pessoaExistente = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

        if (dto.email() != null && !dto.email().isBlank() && !dto.email().equals(pessoaExistente.getEmail())) {
            if (pessoaRepository.findByEmail(dto.email()).isPresent()) {
                throw new RuntimeException("Novo email já está cadastrado para outro usuário.");
            }
            pessoaExistente.setEmail(dto.email());
        }

        if (dto.senha() != null && !dto.senha().isBlank()) {
            pessoaExistente.setSenha(passwordEncoder.encode(dto.senha()));
        }

        if (dto.endereco() != null) {
            pessoaExistente.setEndereco(dto.endereco());
        }
        if (dto.telefone() != null) {
            pessoaExistente.setTelefone(dto.telefone());
        }

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
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Apenas administradores podem alterar status de usuários.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

        pessoa.setAtivo(ativo);
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }

    @Transactional
    public UsuarioAdminResponse alterarRoleUsuario(Integer id, Role novaRole, Integer adminId) {
        Pessoa admin = pessoaRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Apenas administradores podem alterar roles de usuários.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

        pessoa.setRole(novaRole);
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);

        return PessoaMapper.toAdminResponse(pessoaAtualizada);
    }
}
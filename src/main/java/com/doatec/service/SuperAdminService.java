package com.doatec.service;

import com.doatec.dto.request.CriarAdminRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.*;
import com.doatec.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuperAdminService {

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminService(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Pessoa validarSuperAdmin(Integer superAdminId) {
        Pessoa superAdmin = pessoaRepository.findById(superAdminId)
                .orElseThrow(() -> new BusinessException("Super Admin não encontrado"));
        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas Super Admins podem realizar esta operação.");
        }
        return superAdmin;
    }

    public Page<UsuarioAdminResponse> listarAdmins(Pageable pageable) {
        return pessoaRepository.findByRoleIn(java.util.List.of(Role.ADMIN, Role.SUPER_ADMIN), pageable)
                .map(PessoaMapper::toAdminResponse);
    }

    @Transactional
    public UsuarioAdminResponse criarAdmin(CriarAdminRequest request, Integer superAdminId) {
        validarSuperAdmin(superAdminId);

        if (pessoaRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado: " + request.email());
        }

        if (pessoaRepository.findByDocumento(request.documento()).isPresent()) {
            throw new BusinessException("Documento já cadastrado: " + request.documento());
        }

        Role role = Role.valueOf(request.role().toUpperCase());
        if (role == Role.SUPER_ADMIN) {
            throw new BusinessException("Não é possível criar outro Super Admin.");
        }
        if (role != Role.ADMIN) {
            throw new BusinessException("Use este endpoint apenas para criar administradores.");
        }

        TipoPessoa tipoPessoa = TipoPessoa.valueOf(request.tipoPessoa().toUpperCase());
        String senha = request.senha() != null ? passwordEncoder.encode(request.senha()) : passwordEncoder.encode("Admin@123");

        Pessoa pessoa = switch (tipoPessoa) {
            case DOADOR_PF -> DoadorPF.builder()
                    .nome(request.nome())
                    .email(request.email())
                    .senha(senha)
                    .cpf(request.documento())
                    .role(role)
                    .ativo(true)
                    .build();
            case DOADOR_PJ -> DoadorPJ.builder()
                    .nome(request.nome())
                    .email(request.email())
                    .senha(senha)
                    .cnpj(request.documento())
                    .razaoSocial(request.nome())
                    .role(role)
                    .ativo(true)
                    .build();
            case ALUNO -> Aluno.builder()
                    .nome(request.nome())
                    .email(request.email())
                    .senha(senha)
                    .ra(request.documento())
                    .role(role)
                    .ativo(true)
                    .build();
        };

        pessoaRepository.save(pessoa);
        return PessoaMapper.toAdminResponse(pessoa);
    }

    @Transactional
    public UsuarioAdminResponse rebaixarAdmin(Integer id, Integer superAdminId) {
        validarSuperAdmin(superAdminId);

        if (id.equals(superAdminId)) {
            throw new BusinessException("Você não pode rebaixar a si mesmo.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        if (pessoa.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessException("Não é possível rebaixar um Super Admin.");
        }

        if (pessoa.getRole() != Role.ADMIN) {
            throw new BusinessException("Apenas administradores podem ser rebaixados.");
        }

        pessoa.setRole(Role.USER);
        pessoa.markUpdated();
        return PessoaMapper.toAdminResponse(pessoaRepository.save(pessoa));
    }

    @Transactional
    public void excluirAdmin(Integer id, Integer superAdminId) {
        validarSuperAdmin(superAdminId);

        if (id.equals(superAdminId)) {
            throw new BusinessException("Você não pode excluir a si mesmo.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        if (pessoa.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessException("Não é possível excluir um Super Admin.");
        }

        if (pessoa.getRole() != Role.ADMIN) {
            throw new BusinessException("Apenas administradores podem ser excluídos por este endpoint.");
        }

        pessoa.setAtivo(false);
        pessoa.setRole(Role.USER);
        pessoa.markUpdated();
        pessoaRepository.save(pessoa);
    }

    @Transactional
    public UsuarioAdminResponse alterarRoleAdmin(Integer id, Role novaRole, Integer superAdminId) {
        validarSuperAdmin(superAdminId);

        if (id.equals(superAdminId)) {
            throw new BusinessException("Você não pode alterar sua própria role.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        if (pessoa.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessException("Não é possível alterar a role de um Super Admin.");
        }

        if (pessoa.getRole() != Role.ADMIN) {
            throw new BusinessException("Apenas administradores podem ter a role alterada por este endpoint.");
        }

        if (novaRole == Role.SUPER_ADMIN) {
            throw new BusinessException("Não é possível promover a Super Admin.");
        }

        pessoa.setRole(novaRole);
        pessoa.markUpdated();
        return PessoaMapper.toAdminResponse(pessoaRepository.save(pessoa));
    }

    @Transactional
    public UsuarioAdminResponse alterarStatusAdmin(Integer id, Boolean ativo, Integer superAdminId) {
        validarSuperAdmin(superAdminId);

        if (id.equals(superAdminId)) {
            throw new BusinessException("Você não pode alterar seu próprio status.");
        }

        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pessoa não encontrada com ID: " + id));

        if (pessoa.getRole() == Role.SUPER_ADMIN) {
            // Proteger último SUPER_ADMIN
            if (!ativo && pessoaRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
                throw new BusinessException("Não é possível desativar o último Super Admin.");
            }
        }

        if (pessoa.getRole() != Role.ADMIN && pessoa.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Apenas administradores podem ter o status alterado por este endpoint.");
        }

        pessoa.setAtivo(ativo);
        pessoa.markUpdated();
        return PessoaMapper.toAdminResponse(pessoaRepository.save(pessoa));
    }
}

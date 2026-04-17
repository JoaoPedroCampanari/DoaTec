package com.doatec.service;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

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
        if (pessoa.getSenha().equals(loginRequest.senha())) {
            return pessoa;
        } else {
            return null;
        }
    }

    @Transactional
    public Pessoa registrarPessoa(RegistroRequest registroRequest) {
        if (pessoaRepository.findByEmail(registroRequest.email()).isPresent()) {
            throw new RuntimeException("O email informado já está cadastrado.");
        }

        if (registroRequest.identidade() != null && !registroRequest.identidade().isBlank()) {
            Optional<Pessoa> pessoaComDocumento = pessoaRepository.findByDocumento(registroRequest.identidade());
            if (pessoaComDocumento.isPresent()) {
                throw new RuntimeException("O documento " + registroRequest.identidade() + " já está cadastrado.");
            }
        } else {
            if (registroRequest.tipoUsuario().equals("DOADOR_PF") ||
                    registroRequest.tipoUsuario().equals("DOADOR_PJ") ||
                    registroRequest.tipoUsuario().equals("ALUNO")) {
                throw new RuntimeException("O documento de identificação é obrigatório para este tipo de usuário.");
            }
        }

        TipoUsuario tipoUsuarioEnum;
        try {
            tipoUsuarioEnum = TipoUsuario.valueOf(registroRequest.tipoUsuario().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de usuário inválido: " + registroRequest.tipoUsuario());
        }

        Pessoa novaPessoa = PessoaMapper.toPessoa(registroRequest);

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
            pessoaExistente.setSenha(dto.senha());
        }

        if (dto.endereco() != null) {
            pessoaExistente.setEndereco(dto.endereco());
        }
        if (dto.telefone() != null) {
            pessoaExistente.setTelefone(dto.telefone());
        }

        return pessoaRepository.save(pessoaExistente);
    }
}
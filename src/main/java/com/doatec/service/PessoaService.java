package com.doatec.service;

import com.doatec.dtos.LoginDto;
import com.doatec.dtos.PessoaUpdateDto;
import com.doatec.dtos.RegistroDto;
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

    public Pessoa autenticar(LoginDto loginDto) {
        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(loginDto.getEmail());

        if (pessoaOptional.isEmpty()) {
            return null;
        }

        Pessoa pessoa = pessoaOptional.get();
        if (pessoa.getSenha().equals(loginDto.getSenha())) {
            return pessoa;
        } else {
            return null;
        }
    }

    @Transactional
    public Pessoa registrarPessoa(RegistroDto registroDto){
        if (pessoaRepository.findByEmail(registroDto.getEmail()).isPresent()) {
            throw new RuntimeException("O email informado já está cadastrado.");
        }

        if (registroDto.getIdentidade() != null && !registroDto.getIdentidade().isBlank()) {
            Optional<Pessoa> pessoaComDocumento = pessoaRepository.findByDocumento(registroDto.getIdentidade());
            if (pessoaComDocumento.isPresent()) {
                throw new RuntimeException("O documento " + registroDto.getIdentidade() + " já está cadastrado.");
            }
        } else {
            if (registroDto.getTipoUsuario().equals("DOADOR_PF") ||
                    registroDto.getTipoUsuario().equals("DOADOR_PJ") ||
                    registroDto.getTipoUsuario().equals("ALUNO")) {
                throw new RuntimeException("O documento de identificação é obrigatório para este tipo de usuário.");
            }
        }

        TipoUsuario tipoUsuarioEnum;
        try {
            tipoUsuarioEnum = TipoUsuario.valueOf(registroDto.getTipoUsuario().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de usuário inválido: " + registroDto.getTipoUsuario());
        }

        Pessoa novaPessoa = new Pessoa(
                registroDto.getNome(),
                registroDto.getEmail(),
                registroDto.getSenha(),
                registroDto.getEndereco(),
                "",
                registroDto.getIdentidade(),
                tipoUsuarioEnum
        );

        return pessoaRepository.save(novaPessoa);
    }

    @Transactional
    public Pessoa updatePessoaProfile(Integer id, PessoaUpdateDto dto) { // ID agora é Integer
        Pessoa pessoaExistente = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));

        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(pessoaExistente.getEmail())) {
            if (pessoaRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Novo email já está cadastrado para outro usuário.");
            }
            pessoaExistente.setEmail(dto.getEmail());
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            pessoaExistente.setSenha(dto.getSenha());
        }

        if (dto.getEndereco() != null) {
            pessoaExistente.setEndereco(dto.getEndereco());
        }
        if (dto.getTelefone() != null) {
            pessoaExistente.setTelefone(dto.getTelefone());
        }

        return pessoaRepository.save(pessoaExistente);
    }
}
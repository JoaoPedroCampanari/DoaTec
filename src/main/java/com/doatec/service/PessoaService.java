package com.doatec.service;

import com.doatec.dtos.LoginDto;
import com.doatec.dtos.RegistroDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.PessoaFisica;
import com.doatec.model.account.PessoaJuridica;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.TipoUsuario;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional(readOnly = true)
    public Pessoa findById(UUID id) {
        return pessoaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Pessoa> findAll() {
        return pessoaRepository.findAll();
    }

    @Transactional
    public void deleteById(java.util.UUID id) {
        pessoaRepository.deleteById(id);
    }

    public boolean verificarCredenciais(LoginDto loginDto) {
        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(loginDto.getEmail());

        if (pessoaOptional.isEmpty()) {
            return false;
        }

        Pessoa pessoa = pessoaOptional.get();
        return pessoa.getSenha().equals(loginDto.getSenha());
    }

    @Transactional
    public Pessoa registrarPessoa(RegistroDto registroDto){
        Pessoa novaPessoa;
        TipoUsuario tipoUsuarioEnum;

        if (registroDto.getTipoUsuario().equals("pf")) {
            tipoUsuarioEnum = TipoUsuario.DOADOR_PF;
            novaPessoa = new PessoaFisica(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "",
                    tipoUsuarioEnum,
                    registroDto.getIdentidade()
            );
        }
        else if (registroDto.getTipoUsuario().equals("pj")) {
            tipoUsuarioEnum = TipoUsuario.DOADOR_PJ;
            novaPessoa = new PessoaJuridica(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "",
                    tipoUsuarioEnum,
                    registroDto.getIdentidade()
            );
        }
        else {
            tipoUsuarioEnum = TipoUsuario.ALUNO;
            Aluno aluno = new Aluno(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "",
                    tipoUsuarioEnum,
                    registroDto.getIdentidade()
            );

            aluno.setJustificativa("");
            aluno.setPreferenciaEquipamento("");
            novaPessoa = aluno;
        }

        return pessoaRepository.save(novaPessoa);
    }
}
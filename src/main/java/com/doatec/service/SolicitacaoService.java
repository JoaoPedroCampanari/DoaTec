package com.doatec.service;

import com.doatec.dtos.SolicitacaoDto;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SolicitacaoService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Transactional
    public SolicitacaoHardware criarSolicitacao(SolicitacaoDto dto) {

        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(dto.getEmail());

        if (pessoaOptional.isEmpty()) {
            throw new RuntimeException("Aluno com email " + dto.getEmail() + " não está cadastrado.");
        }

        Pessoa pessoaEncontrada = pessoaOptional.get();

        if (!(pessoaEncontrada.getSenha().equals(dto.getSenha()))){
            throw new RuntimeException("Senha incorreta!");
        }

        if (!(pessoaEncontrada instanceof Aluno)) {
            throw new RuntimeException("O email " + dto.getEmail() + " já está cadastrado, mas não pertence a um aluno.");
        }

        Aluno aluno = (Aluno) pessoaEncontrada;

        if (!(aluno.getRa().equals(dto.getRa()))){
            throw new RuntimeException("O RA " + dto.getRa() + " Não é válido!");
        }

        aluno.setJustificativa(dto.getJustificativa());
        aluno.setPreferenciaEquipamento(dto.getPreferenciaEquipamento());


        SolicitacaoHardware novaSolicitacao = new SolicitacaoHardware(aluno);

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}

package com.doatec.service;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitacaoService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Transactional(readOnly = true)
    public List<SolicitacaoHardware> findSolicitacoesByAlunoId(Integer alunoId) {
        return solicitacaoHardwareRepository.findByAlunoId(alunoId);
    }

    @Transactional
    public SolicitacaoHardware criarSolicitacao(SolicitacaoRequest dto) {

        Pessoa pessoaEncontrada = pessoaRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário com email " + dto.email() + " não está cadastrado."));

        if (pessoaEncontrada.getTipo() != TipoUsuario.ALUNO) {
            throw new RuntimeException("O email " + dto.email() + " pertence a um usuário que não é aluno.");
        }

        if (!pessoaEncontrada.getNome().equals(dto.nome())) {
            throw new RuntimeException("Nome incorreto!");
        }
        if (!pessoaEncontrada.getSenha().equals(dto.senha())) {
            throw new RuntimeException("Senha incorreta!");
        }
        if (!pessoaEncontrada.getDocumento().equals(dto.ra())) {
            throw new RuntimeException("O RA " + dto.ra() + " não é válido para este aluno.");
        }

        SolicitacaoHardware novaSolicitacao = SolicitacaoMapper.toSolicitacao(dto, pessoaEncontrada);

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}
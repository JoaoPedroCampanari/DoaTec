package com.doatec.service;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> findSolicitacoesByAlunoId(Integer alunoId) {
        return solicitacaoHardwareRepository.findByAlunoId(alunoId).stream()
                .map(SolicitacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitacaoHardware criarSolicitacao(SolicitacaoRequest dto) {

        Pessoa pessoaEncontrada = pessoaRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("Usuário com email " + dto.email() + " não está cadastrado."));

        // Validar se é aluno usando instanceof
        if (!(pessoaEncontrada instanceof Aluno)) {
            throw new BusinessException("O email " + dto.email() + " pertence a um usuário que não é aluno. Apenas alunos podem fazer solicitações de hardware.");
        }

        if (!pessoaEncontrada.getNome().equals(dto.nome())) {
            throw new BusinessException("Nome incorreto!");
        }

        if (!passwordEncoder.matches(dto.senha(), pessoaEncontrada.getSenha())) {
            throw new BusinessException("Senha incorreta!");
        }

        if (!pessoaEncontrada.getDocumento().equals(dto.ra())) {
            throw new BusinessException("O RA " + dto.ra() + " não é válido para este aluno.");
        }

        SolicitacaoHardware novaSolicitacao = SolicitacaoMapper.toSolicitacao(dto, pessoaEncontrada);

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}
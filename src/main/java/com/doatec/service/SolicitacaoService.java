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

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> findSolicitacoesByAlunoId(Integer alunoId) {
        return solicitacaoHardwareRepository.findByAlunoId(alunoId).stream()
                .map(SolicitacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitacaoHardware criarSolicitacao(String authenticatedEmail, SolicitacaoRequest dto) {

        Pessoa pessoaEncontrada = pessoaRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (!(pessoaEncontrada instanceof Aluno)) {
            throw new BusinessException("Apenas alunos podem fazer solicitações de hardware.");
        }

        if (!pessoaEncontrada.getDocumento().equals(dto.ra())) {
            throw new BusinessException("O RA " + dto.ra() + " não é válido para este aluno.");
        }

        SolicitacaoHardware novaSolicitacao = SolicitacaoMapper.toSolicitacao(dto, pessoaEncontrada);

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}

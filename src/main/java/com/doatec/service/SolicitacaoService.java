package com.doatec.service;

import com.doatec.dtos.SolicitacaoDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public SolicitacaoHardware criarSolicitacao(SolicitacaoDto dto) {

        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(dto.getEmail());

        if (pessoaOptional.isEmpty()) {
            throw new RuntimeException("Usuário com email " + dto.getEmail() + " não está cadastrado.");
        }

        Pessoa pessoaEncontrada = pessoaOptional.get();

        if (pessoaEncontrada.getTipo() != TipoUsuario.ALUNO) {
            throw new RuntimeException("O email " + dto.getEmail() + " pertence a um usuário que não é aluno.");
        }

        if (!pessoaEncontrada.getNome().equals(dto.getNome())) {
            throw new RuntimeException("Nome incorreto!");
        }
        if (!pessoaEncontrada.getSenha().equals(dto.getSenha())) {
            throw new RuntimeException("Senha incorreta!");
        }
        if (!pessoaEncontrada.getDocumento().equals(dto.getRa())) {
            throw new RuntimeException("O RA " + dto.getRa() + " não é válido para este aluno.");
        }

        SolicitacaoHardware novaSolicitacao = new SolicitacaoHardware(
                pessoaEncontrada,
                dto.getJustificativa(),
                dto.getPreferenciaEquipamento()
        );

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}
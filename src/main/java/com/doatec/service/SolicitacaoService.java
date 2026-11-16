package com.doatec.service;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento das solicitações de equipamentos.
 */
@Service
public class SolicitacaoService {
    @Autowired private ArquivoService arquivoService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    /**
     * Lista as solicitações de um aluno.
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> findSolicitacoesByAlunoId(Integer alunoId) {
        return solicitacaoHardwareRepository.findByAlunoIdOrderByDataSolicitacaoDescIdDesc(alunoId).stream()
                .map(SolicitacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Registra uma solicitação de equipamento.
     */
    @Transactional
    public SolicitacaoHardware criarSolicitacao(String authenticatedEmail, SolicitacaoRequest dto) {

        Pessoa pessoaEncontrada = pessoaRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (pessoaEncontrada.getRole() == com.doatec.model.account.Role.SUPER_ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Superadministradores não podem realizar esta ação.");
        }

        arquivoService.validarVinculo(dto.urlComprovante(), pessoaEncontrada);

        if (!(pessoaEncontrada instanceof Aluno)) {
            throw new BusinessException("Apenas alunos podem fazer solicitações de hardware.");
        }

        if (!pessoaEncontrada.getDocumento().equals(dto.ra())) {
            throw new BusinessException("O RA " + dto.ra() + " não é válido para este aluno.");
        }

        SolicitacaoHardware novaSolicitacao = SolicitacaoMapper.toSolicitacao(dto, pessoaEncontrada);
        novaSolicitacao.setNomeComprovante(arquivoService.nomeOriginal(dto.urlComprovante()));

        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

    /**
     * Exclui uma solicitação do aluno informado.
     */
    @Transactional
    public void excluirSolicitacao(Integer id, String email) {
        SolicitacaoHardware solicitacao = solicitacaoHardwareRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada."));
        if (!solicitacao.getAluno().getEmail().equals(email)) {
            throw new BusinessException("Você só pode excluir suas próprias solicitações.");
        }
        if (solicitacao.getStatus() != StatusSolicitacao.EM_ANALISE) {
            throw new BusinessException("Só é possível excluir solicitações em análise.");
        }
        solicitacaoHardwareRepository.delete(solicitacao);
    }

}

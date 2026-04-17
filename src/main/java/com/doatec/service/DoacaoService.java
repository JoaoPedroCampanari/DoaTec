package com.doatec.service;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DashboardStatsResponse;
import com.doatec.mapper.DoacaoMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import com.doatec.model.donation.Doacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class DoacaoService {

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional(readOnly = true)
    public List<Doacao> findDoacoesByDoadorId(Integer doadorId) {
        return doacaoRepository.findByDoadorId(doadorId);
    }

    @Transactional
    public Doacao registrarDoacao(DoacaoRequest dto) {
        Optional<Pessoa> doadorOptional = pessoaRepository.findByEmail(dto.email());

        if (doadorOptional.isEmpty()) {
            throw new RuntimeException("Doador com email " + dto.email() + " não está cadastrado. Por favor, registre-se primeiro.");
        }

        Pessoa doador = doadorOptional.get();

        if (dto.tipoDocumento().equals("cpf")) {
            if (doador.getTipo() != TipoUsuario.DOADOR_PF) {
                throw new RuntimeException("O email " + dto.email() + " pertence a um usuário que não é Pessoa Física.");
            }
            if (!doador.getDocumento().equals(dto.numeroDocumento())) {
                throw new RuntimeException("O CPF informado não corresponde ao CPF cadastrado para este email.");
            }
        } else if (dto.tipoDocumento().equals("cnpj")) {
            if (doador.getTipo() != TipoUsuario.DOADOR_PJ) {
                throw new RuntimeException("O email " + dto.email() + " pertence a um usuário que não é Pessoa Jurídica.");
            }
            if (!doador.getDocumento().equals(dto.numeroDocumento())) {
                throw new RuntimeException("O CNPJ informado não corresponde ao CNPJ cadastrado para este email.");
            }
        } else if (dto.tipoDocumento().equals("aluno")) {
            if (doador.getTipo() != TipoUsuario.ALUNO) {
                throw new RuntimeException("O email " + dto.email() + " pertence a um usuário que não é Aluno. Se deseja doar como aluno, seu tipo de cadastro deve ser 'ALUNO'.");
            }
            if (!doador.getDocumento().equals(dto.numeroDocumento())) {
                throw new RuntimeException("O RA informado não corresponde ao RA cadastrado para este email.");
            }
        } else {
            throw new RuntimeException("Tipo de documento inválido para a doação.");
        }

        if (!doador.getNome().equals(dto.nome())) {
            throw new RuntimeException("Nome incorreto!");
        }

        if (dto.telefone() != null && !dto.telefone().isBlank()) {
            doador.setTelefone(dto.telefone());
            pessoaRepository.save(doador);
        }

        Doacao novaDoacao = DoacaoMapper.toDoacao(dto, doador);

        return doacaoRepository.save(novaDoacao);
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long total = doacaoRepository.count();
        Optional<Doacao> lastDonationOpt = doacaoRepository.findTopByOrderByDataDoacaoDesc();

        LocalDate lastDate = lastDonationOpt.map(Doacao::getDataDoacao).orElse(null);

        return new DashboardStatsResponse(total, lastDate);
    }
}
package com.doatec.service;

import com.doatec.dtos.DoacaoDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
// Removido import java.util.UUID; // Não precisamos mais gerar UUIDs

@Service
public class DoacaoService {

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public Doacao registrarDoacao(DoacaoDto dto) {
        Optional<Pessoa> doadorOptional = pessoaRepository.findByEmail(dto.getEmail());

        if (doadorOptional.isEmpty()) {
            throw new RuntimeException("Doador com email " + dto.getEmail() + " não está cadastrado. Por favor, registre-se primeiro.");
        }

        Pessoa doador = doadorOptional.get();

        if (dto.getTipoDocumento().equals("cpf")) {
            if (doador.getTipo() != TipoUsuario.DOADOR_PF) {
                throw new RuntimeException("O email " + dto.getEmail() + " pertence a um usuário que não é Pessoa Física.");
            }
            if (!doador.getDocumento().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CPF informado não corresponde ao CPF cadastrado para este email.");
            }
        } else if (dto.getTipoDocumento().equals("cnpj")) {
            if (doador.getTipo() != TipoUsuario.DOADOR_PJ) {
                throw new RuntimeException("O email " + dto.getEmail() + " pertence a um usuário que não é Pessoa Jurídica.");
            }
            if (!doador.getDocumento().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CNPJ informado não corresponde ao CNPJ cadastrado para este email.");
            }
        } else {
            throw new RuntimeException("Tipo de documento inválido para a doação.");
        }

        if (!doador.getNome().equals(dto.getNome())){
            throw new RuntimeException("Nome incorreto!");
        }

        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()){
            doador.setTelefone(dto.getTelefone());
            pessoaRepository.save(doador);
        }

        // IDs são gerados automaticamente pelo banco de dados
        Doacao novaDoacao = new Doacao(doador, StatusDoacao.EM_ANALISE, dto.getPreferenciaEntrega());

        ItemDoado itemDoado = new ItemDoado(novaDoacao, dto.getTipoItem(), dto.getDescricaoItem());

        novaDoacao.getItens().add(itemDoado);

        return doacaoRepository.save(novaDoacao);
    }
}
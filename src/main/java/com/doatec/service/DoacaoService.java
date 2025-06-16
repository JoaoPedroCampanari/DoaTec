package com.doatec.service;


import com.doatec.dtos.DoacaoDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.PessoaFisica;
import com.doatec.model.account.PessoaJuridica;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


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
            // 1. Verifica se o doador encontrado é realmente uma PessoaFisica
            if (!(doador instanceof PessoaFisica)) {
                throw new RuntimeException("O usuário encontrado com este email não é uma Pessoa Física.");
            }
            // 2. Faz a conversão segura
            PessoaFisica doadorPf = (PessoaFisica) doador;
            // 3. Valida se o CPF do banco é igual ao CPF fornecido no formulário
            if (!doadorPf.getCpf().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CPF informado não corresponde ao CPF cadastrado para este email.");
            }
        } else if (dto.getTipoDocumento().equals("cnpj")) {
            // 1. Verifica se o doador encontrado é realmente uma PessoaJuridica
            if (!(doador instanceof PessoaJuridica)) {
                throw new RuntimeException("O usuário encontrado com este email não é uma Pessoa Jurídica.");
            }
            // 2. Faz a conversão segura
            PessoaJuridica doadorPj = (PessoaJuridica) doador;
            // 3. Valida se o CNPJ do banco é igual ao CNPJ fornecido no formulário
            if (!doadorPj.getCnpj().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CNPJ informado não corresponde ao CNPJ cadastrado para este email.");
            }
        }

        Doacao novaDoacao = new Doacao();

        if (!(doador.getNome().equals(dto.getNome()))){
            throw new RuntimeException("Nome incorreta!");
        }


        if (dto.getTelefone() != null){
            doador.setTelefone(dto.getTelefone());
        }
        novaDoacao.setDoador(doador);
        novaDoacao.setPreferenciaEntrega(dto.getPreferenciaEntrega());
        novaDoacao.setStatus(StatusDoacao.EM_ANALISE);

        ItemDoado itemDoado = new ItemDoado();
        itemDoado.setDoacao(novaDoacao);
        itemDoado.setTipoItem(dto.getTipoItem());
        itemDoado.setDescricao(dto.getDescricaoItem());

        novaDoacao.getItens().add(itemDoado);

        return doacaoRepository.save(novaDoacao);
    }
}

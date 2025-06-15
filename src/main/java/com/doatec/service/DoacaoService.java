package com.doatec.service;


import com.doatec.dtos.DoacaoDto;
import com.doatec.model.account.Pessoa;
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
        Doacao novaDoacao = new Doacao();
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

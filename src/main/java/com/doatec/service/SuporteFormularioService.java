package com.doatec.service;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.mapper.SuporteFormularioMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SuporteFormularioService {

    @Autowired
    private SuporteFormularioRepository suporteRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public SuporteFormulario criarTicket(SuporteFormularioRequest dto) {
        Pessoa autor = pessoaRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Nenhuma conta encontrada com o email fornecido. Apenas usuários cadastrados podem abrir tickets de suporte."));

        if (!dto.nome().equals(autor.getNome())) {
            throw new RuntimeException("Nome inválido!");
        }

        SuporteFormulario novoTicket = SuporteFormularioMapper.toSuporteFormulario(dto, autor);

        return suporteRepository.save(novoTicket);
    }
}
package com.doatec.service;

import com.doatec.dtos.SuporteFormularioDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class SuporteFormularioService {

    @Autowired
    private SuporteFormularioRepository suporteRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public SuporteFormulario criarTicket(SuporteFormularioDto dto) {
        Optional<Pessoa> autorOptional = pessoaRepository.findByEmail(dto.getEmail());

        if (autorOptional.isEmpty()) {
            throw new RuntimeException("Nenhuma conta encontrada com o email fornecido. Apenas usuários cadastrados podem abrir tickets de suporte.");
        }

        Pessoa autor = autorOptional.get();

        if (!dto.getNome().equals(autor.getNome())){
            throw new RuntimeException("Nome inválido!");
        }

        SuporteFormulario novoTicket = new SuporteFormulario(autor, dto.getAssunto(), dto.getMensagem(), "ABERTO", LocalDateTime.now());

        return suporteRepository.save(novoTicket);
    }
}
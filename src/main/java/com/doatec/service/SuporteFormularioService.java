package com.doatec.service;

import com.doatec.dtos.SuporteFormularioDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SuporteFormularioService {

    @Autowired
    private SuporteFormularioRepository suporteRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public SuporteFormulario criarTicket(SuporteFormularioDto dto) {
        // 1. Procura por um usuário com o email fornecido.
        Optional<Pessoa> autorOptional = pessoaRepository.findByEmail(dto.getEmail());

        // 2. Se o autor não for encontrado, lança um erro e interrompe o processo.
        if (autorOptional.isEmpty()) {
            throw new RuntimeException("Nenhuma conta encontrada com o email fornecido. Apenas usuários cadastrados podem abrir tickets de suporte.");
        }

        // 3. Pega o autor encontrado.
        Pessoa autor = autorOptional.get();

        // 4. Cria a nova instância do ticket de suporte.
        SuporteFormulario novoTicket = new SuporteFormulario();

        // 5. Vincula o autor e preenche os outros dados.
        novoTicket.setAutor(autor);
        novoTicket.setAssunto(dto.getAssunto());
        novoTicket.setMensagem(dto.getMensagem());
        novoTicket.setStatus("ABERTO");

        // 6. Salva o novo ticket no banco.
        return suporteRepository.save(novoTicket);
    }
}
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

/**
 * Classe de serviço que gerencia a lógica de negócio para a criação de tickets de suporte.
 */
@Service
public class SuporteFormularioService {

    // Repositório para operações de acesso a dados de SuporteFormulario.
    @Autowired
    private SuporteFormularioRepository suporteRepository;

    // Repositório para operações de acesso a dados de Pessoa, para buscar o autor do ticket.
    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public SuporteFormulario criarTicket(SuporteFormularioDto dto) {
        // 1. Procura por um usuário cadastrado com o email fornecido no DTO.
        Optional<Pessoa> autorOptional = pessoaRepository.findByEmail(dto.getEmail());

        // 2. Se nenhum usuário for encontrado, lança uma exceção, pois apenas usuários registrados podem criar tickets.
        if (autorOptional.isEmpty()) {
            throw new RuntimeException("Nenhuma conta encontrada com o email fornecido. Apenas usuários cadastrados podem abrir tickets de suporte.");
        }

        // 3. Se o usuário for encontrado, obtém o objeto Pessoa.
        Pessoa autor = autorOptional.get();

        // 4. Valida se o nome fornecido no formulário corresponde ao nome do usuário cadastrado.
        if (!dto.getNome().equals(autor.getNome())){
            throw new RuntimeException("Nome inválido!");
        }

        // 5. Cria uma nova instância da entidade SuporteFormulario.
        SuporteFormulario novoTicket = new SuporteFormulario();

        // 6. Define os atributos do novo ticket.
        novoTicket.setAutor(autor); // Vincula o ticket ao autor encontrado.
        novoTicket.setAssunto(dto.getAssunto()); // Define o assunto da mensagem.
        novoTicket.setMensagem(dto.getMensagem()); // Define o corpo da mensagem.
        novoTicket.setStatus("ABERTO"); // Define um status inicial padrão para o ticket.

        // 7. Salva o novo ticket no banco de dados e o retorna.
        return suporteRepository.save(novoTicket);
    }
}
package com.doatec.service;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.mapper.SuporteMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

        SuporteFormulario novoTicket = SuporteMapper.toSuporteFormulario(dto, autor);

        return suporteRepository.save(novoTicket);
    }

    @Transactional(readOnly = true)
    public List<SuporteResponse> getTicketsByAutorId(Integer autorId) {
        return suporteRepository.findByAutorId(autorId).stream()
                .map(SuporteMapper::toResponse)
                .toList();
    }

    @Transactional
    public void excluirTicket(Integer ticketId, Integer autorId) {
        SuporteFormulario ticket = suporteRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado"));

        if (!ticket.getAutor().getId().equals(autorId)) {
            throw new RuntimeException("Sem permissão para excluir este ticket");
        }

        if (ticket.getStatus() != StatusSuporte.ABERTO) {
            throw new RuntimeException("Só é possível excluir tickets com status ABERTO");
        }

        suporteRepository.deleteById(ticketId);
    }
}
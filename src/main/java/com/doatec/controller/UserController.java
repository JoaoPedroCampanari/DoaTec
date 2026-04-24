package com.doatec.controller;

import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.DoacaoMapper;
import com.doatec.mapper.PessoaMapper;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import com.doatec.service.DoacaoService;
import com.doatec.service.PessoaService;
import com.doatec.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private DoacaoService doacaoService;

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Autowired
    private SuporteFormularioRepository suporteFormularioRepository;

    /**
     * Endpoint para verificar se o usuário está autenticado.
     * Retorna os dados do usuário logado ou 401 se não estiver autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserLoginResponse> getCurrentUser(@AuthenticationPrincipal User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pessoa pessoa = pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        long totalDoacoes = doacaoRepository.countByDoadorId(pessoa.getId());
        long totalSolicitacoes = solicitacaoHardwareRepository.countByAlunoId(pessoa.getId());
        long totalTicketsSuporte = suporteFormularioRepository.countByAutorId(pessoa.getId());

        return ResponseEntity.ok(PessoaMapper.toResponse(pessoa, totalDoacoes, totalSolicitacoes, totalTicketsSuporte));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserLoginResponse> getUserById(@PathVariable Integer id) {
        Pessoa pessoa = pessoaService.findById(id);
        if (pessoa != null) {
            long totalDoacoes = doacaoRepository.countByDoadorId(pessoa.getId());
            long totalSolicitacoes = solicitacaoHardwareRepository.countByAlunoId(pessoa.getId());
            long totalTicketsSuporte = suporteFormularioRepository.countByAutorId(pessoa.getId());
            UserLoginResponse responseDto = PessoaMapper.toResponse(pessoa, totalDoacoes, totalSolicitacoes, totalTicketsSuporte);
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @Valid @RequestBody PessoaUpdateRequest dto) {
        try {
            Pessoa pessoaAtualizada = pessoaService.updatePessoaProfile(id, dto);
            return ResponseEntity.ok("Perfil atualizado com sucesso para " + pessoaAtualizada.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar perfil: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/donations")
    public ResponseEntity<List<DoacaoResponse>> getUserDonations(@PathVariable Integer id) {
        List<DoacaoResponse> doacoes = doacaoService.findDoacoesByDoadorId(id);
        return ResponseEntity.ok(doacoes);
    }

    @GetMapping("/{id}/solicitacoes")
    public ResponseEntity<List<SolicitacaoResponse>> getUserSolicitacoes(@PathVariable Integer id) {
        List<SolicitacaoResponse> solicitacoes = solicitacaoService.findSolicitacoesByAlunoId(id);
        return ResponseEntity.ok(solicitacoes);
    }
}
package com.doatec.controller;

import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.dto.response.SugestaoMatchingResponse;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento do inventário de equipamentos.
 * Todos os endpoints requerem permissão de ADMIN.
 */
@RestController
@RequestMapping("/api/admin/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    /**
     * Lista todos os equipamentos, opcionalmente filtrados por status.
     */
    @GetMapping
    public ResponseEntity<List<EquipamentoResponse>> listarEquipamentos(
            @RequestParam(required = false) StatusEquipamento status) {
        List<EquipamentoResponse> equipamentos = inventarioService.listarEquipamentos(status);
        return ResponseEntity.ok(equipamentos);
    }

    /**
     * Busca um equipamento por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponse> buscarEquipamento(@PathVariable Integer id) {
        EquipamentoResponse equipamento = inventarioService.buscarPorId(id);
        return ResponseEntity.ok(equipamento);
    }

    /**
     * Busca equipamentos disponíveis que correspondam a uma preferência.
     */
    @GetMapping("/disponiveis")
    public ResponseEntity<List<EquipamentoResponse>> buscarDisponiveis(
            @RequestParam(required = false) String preferencia) {
        List<EquipamentoResponse> equipamentos = inventarioService.buscarDisponiveisPorPreferencia(preferencia);
        return ResponseEntity.ok(equipamentos);
    }

    /**
     * Gera sugestões de matching para uma solicitação.
     * Retorna equipamentos disponíveis compatíveis com a preferência do aluno.
     */
    @GetMapping("/sugestoes/{solicitacaoId}")
    public ResponseEntity<SugestaoMatchingResponse> sugerirMatchings(
            @PathVariable Integer solicitacaoId) {
        SugestaoMatchingResponse sugestoes = inventarioService.sugerirMatchings(solicitacaoId);
        return ResponseEntity.ok(sugestoes);
    }

    /**
     * Atribui um equipamento a uma solicitação aprovada.
     * O equipamento muda de status para RESERVADO.
     */
    @PostMapping("/{equipamentoId}/atribuir/{solicitacaoId}")
    public ResponseEntity<EquipamentoResponse> atribuirEquipamento(
            @PathVariable Integer equipamentoId,
            @PathVariable Integer solicitacaoId,
            @AuthenticationPrincipal User userDetails) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        EquipamentoResponse equipamento = inventarioService.atribuirEquipamento(
                equipamentoId, solicitacaoId, adminId);
        return ResponseEntity.ok(equipamento);
    }

    /**
     * Marca um equipamento como entregue ao aluno.
     * O equipamento muda de status de RESERVADO para ENTREGUE.
     */
    @PutMapping("/{id}/entregar")
    public ResponseEntity<EquipamentoResponse> marcarComoEntregue(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        EquipamentoResponse equipamento = inventarioService.marcarComoEntregue(id, adminId);
        return ResponseEntity.ok(equipamento);
    }

    @Autowired
    private PessoaRepository pessoaRepository;

    private Integer getAuthenticatedAdminId(User userDetails) {
        return pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                .getId();
    }
}
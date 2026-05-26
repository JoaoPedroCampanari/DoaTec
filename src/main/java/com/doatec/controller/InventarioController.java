package com.doatec.controller;

import com.doatec.dto.request.EquipamentoRequest;
import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * Lista equipamentos com filtros combinados (todos opcionais).
     *
     * <ul>
     *   <li>{@code status} — DISPONIVEL, RESERVADO ou ENTREGUE</li>
     *   <li>{@code conservacao} — NOVO, EXCELENTE, BOM, REGULAR ou NECESSITA_REPARO</li>
     *   <li>{@code origem} — "COM_VINCULO" (vinculados a doação) ou "SEM_VINCULO" (avulsos);
     *       ignorado se {@code doacaoId} for passado</li>
     *   <li>{@code doacaoId} — filtra equipamentos vinculados a uma doação específica</li>
     *   <li>{@code q} — busca textual livre, case-insensitive, em tipo OU descrição</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<List<EquipamentoResponse>> listarEquipamentos(
            @RequestParam(required = false) StatusEquipamento status,
            @RequestParam(required = false) EstadoConservacao conservacao,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) Integer doacaoId,
            @RequestParam(required = false) String q) {
        List<EquipamentoResponse> equipamentos =
                inventarioService.listarEquipamentos(status, conservacao, origem, doacaoId, q);
        return ResponseEntity.ok(equipamentos);
    }

    /**
     * Cadastra manualmente um equipamento no inventário.
     * Usado após auditoria física da doação (admin testa o que funciona).
     * Vínculo com {@code doacaoId} é opcional.
     */
    @PostMapping
    public ResponseEntity<EquipamentoResponse> criarEquipamento(
            @RequestBody @Valid EquipamentoRequest request,
            @AuthenticationPrincipal User userDetails) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        EquipamentoResponse equipamento = inventarioService.criarEquipamentoManual(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamento);
    }

    /**
     * Atualiza um equipamento existente. O vínculo com doação só pode ser
     * alterado enquanto o equipamento está DISPONIVEL.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponse> atualizarEquipamento(
            @PathVariable Integer id,
            @RequestBody @Valid EquipamentoRequest request,
            @AuthenticationPrincipal User userDetails) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        EquipamentoResponse equipamento = inventarioService.atualizarEquipamento(id, request, adminId);
        return ResponseEntity.ok(equipamento);
    }

    /**
     * Soft delete de equipamento. Só permitido se status = DISPONIVEL.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEquipamento(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        inventarioService.deletarEquipamento(id, adminId);
        return ResponseEntity.noContent().build();
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
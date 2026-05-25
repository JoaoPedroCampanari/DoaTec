package com.doatec.dto.request;

import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request para criação manual e atualização de equipamentos no inventário.
 *
 * <p>O admin preenche este DTO ao auditar uma doação física (ou cadastrar
 * uma peça avulsa que nunca veio pelo site).</p>
 *
 * <ul>
 *   <li>{@code doacaoId} é opcional — equipamentos podem ser cadastrados
 *       sem vínculo com nenhuma doação registrada.</li>
 *   <li>{@code status} é opcional na criação (default DISPONIVEL); usado em
 *       atualização quando o admin precisa corrigir o estado.</li>
 * </ul>
 */
@Builder
public record EquipamentoRequest(

    @NotBlank(message = "O tipo do equipamento é obrigatório")
    @Size(max = 100, message = "O tipo deve ter no máximo 100 caracteres")
    String tipo,

    @NotBlank(message = "A descrição é obrigatória")
    String descricao,

    @NotNull(message = "O estado de conservação é obrigatório")
    EstadoConservacao estadoConservacao,

    /** Vínculo opcional com uma doação existente. */
    Integer doacaoId,

    /** Default DISPONIVEL quando null. */
    StatusEquipamento status

) {}

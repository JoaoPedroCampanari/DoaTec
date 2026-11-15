package com.doatec.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
public record VinculoEquipamentosRequest(
        @NotEmpty(message = "Selecione pelo menos um equipamento")
        @Size(max = 100, message = "Selecione no máximo 100 equipamentos")
        List<@NotNull @Positive Integer> equipamentos,
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres") String observacao
) {}

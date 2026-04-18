package com.doatec.dto.response;

import lombok.Builder;

@Builder
public record AdminDashboardResponse(
    long totalDoacoes,
    long totalSolicitacoesPendentes,
    long totalTicketsAbertos,
    long totalUsuariosAtivos,
    long doacoesAprovadas,
    long doacoesRejeitadas,
    long solicitacoesAprovadas,
    long solicitacoesRejeitadas
) {}
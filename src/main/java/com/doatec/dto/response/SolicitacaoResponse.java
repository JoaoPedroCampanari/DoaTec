package com.doatec.dto.response;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record SolicitacaoResponse(
    Integer id,
    String alunoNome,
    String alunoEmail,
    String alunoRa,
    LocalDate dataSolicitacao,
    String status,
    String justificativa,
    String preferenciaEquipamento,
    String urlComprovante,
    String nomeComprovante,
    String observacaoAdmin,
    String adminAvaliadorNome,
    LocalDateTime dataAvaliacao
) {
    public static SolicitacaoResponse from(SolicitacaoHardware solicitacao) {
        return SolicitacaoResponse.builder()
                .id(solicitacao.getId())
                .alunoNome(solicitacao.getAluno() != null ? solicitacao.getAluno().getNome() : null)
                .alunoEmail(solicitacao.getAluno() != null ? solicitacao.getAluno().getEmail() : null)
                .alunoRa(solicitacao.getAluno() != null ? solicitacao.getAluno().getDocumento() : null)
                .dataSolicitacao(solicitacao.getDataSolicitacao())
                .status(solicitacao.getStatus().name())
                .justificativa(solicitacao.getJustificativa())
                .preferenciaEquipamento(solicitacao.getPreferenciaEquipamento())
                .urlComprovante(solicitacao.getUrlComprovante())
                .nomeComprovante(solicitacao.getNomeComprovante())
                .observacaoAdmin(solicitacao.getObservacaoAdmin())
                .adminAvaliadorNome(solicitacao.getAdminAvaliador() != null ? solicitacao.getAdminAvaliador().getNome() : null)
                .dataAvaliacao(solicitacao.getDataAvaliacao())
                .build();
    }
}
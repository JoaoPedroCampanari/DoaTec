package com.doatec.model.donation;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Preserves the original database codes and reads historical logistics states as approved. */
@Converter
public class StatusDoacaoConverter implements AttributeConverter<StatusDoacao, String> {
    @Override
    public String convertToDatabaseColumn(StatusDoacao status) {
        if (status == null) return null;
        return switch (status) {
            case EM_ANALISE -> "EM_TRIAGEM";
            case APROVADO -> "FINALIZADO";
            case RECUSADO -> "REJEITADA";
            default -> status.name();
        };
    }

    @Override
    public StatusDoacao convertToEntityAttribute(String value) {
        if (value == null) return null;
        return switch (value) {
            case "EM_TRIAGEM", "EM_ANALISE", "AGENDANDO_COLETA" -> StatusDoacao.EM_ANALISE;
            case "AGUARDANDO_COLETA", "RECEBIDO", "FINALIZADO", "APROVADO" -> StatusDoacao.APROVADO;
            case "REJEITADA", "RECUSADO" -> StatusDoacao.RECUSADO;
            default -> StatusDoacao.valueOf(value);
        };
    }
}

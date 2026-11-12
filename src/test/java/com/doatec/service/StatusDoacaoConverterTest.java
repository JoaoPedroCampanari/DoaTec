package com.doatec.service;

import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.donation.StatusDoacaoConverter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusDoacaoConverterTest {
    @Test void converteCodigosHistoricosSemAlterarDados() {
        var converter = new StatusDoacaoConverter();
        assertEquals(StatusDoacao.EM_ANALISE, converter.convertToEntityAttribute("EM_TRIAGEM"));
        assertEquals(StatusDoacao.EM_ANALISE, converter.convertToEntityAttribute("AGENDANDO_COLETA"));
        for (String valor : new String[]{"AGUARDANDO_COLETA", "RECEBIDO", "FINALIZADO"})
            assertEquals(StatusDoacao.APROVADO, converter.convertToEntityAttribute(valor));
        assertEquals(StatusDoacao.RECUSADO, converter.convertToEntityAttribute("REJEITADA"));
        for (StatusDoacao status : StatusDoacao.values())
            assertEquals(status, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(status)));
    }
}

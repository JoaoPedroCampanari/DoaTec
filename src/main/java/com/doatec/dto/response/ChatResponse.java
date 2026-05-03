package com.doatec.dto.response;

import java.time.LocalDateTime;

public record ChatResponse(
    Integer id,
    String conteudo,
    LocalDateTime dataEnvio,
    Integer remetenteId,
    String remetenteNome,
    String remetenteRole
) {}

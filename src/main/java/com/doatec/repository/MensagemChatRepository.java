package com.doatec.repository;

import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Integer> {
    List<MensagemChat> findByContextoAndReferenciaIdOrderByDataEnvioAsc(ContextoChat contexto, Integer referenciaId);
}

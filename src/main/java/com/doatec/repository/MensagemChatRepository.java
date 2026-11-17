package com.doatec.repository;

import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import com.doatec.model.account.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Integer> {
    List<MensagemChat> findByContextoAndReferenciaIdOrderByDataEnvioAsc(ContextoChat contexto, Integer referenciaId);

    @Query("SELECT DISTINCT m.contexto, m.referenciaId FROM MensagemChat m WHERE m.remetente.role = :role AND (m.lidaPelaEquipe IS NULL OR m.lidaPelaEquipe = false)")
    List<Object[]> buscarConversasNaoLidas(@Param("role") Role role);

    @Modifying
    @Query("UPDATE MensagemChat m SET m.lidaPelaEquipe = true WHERE m.contexto = :contexto AND m.referenciaId = :referenciaId AND m.id <= :ultimaMensagemId AND m.remetente.role = :role AND (m.lidaPelaEquipe IS NULL OR m.lidaPelaEquipe = false)")
    int marcarLidasPelaEquipe(@Param("contexto") ContextoChat contexto, @Param("referenciaId") Integer referenciaId,
                              @Param("ultimaMensagemId") Integer ultimaMensagemId, @Param("role") Role role);
}

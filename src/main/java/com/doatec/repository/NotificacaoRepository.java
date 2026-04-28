package com.doatec.repository;

import com.doatec.model.notification.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Notificacao.
 */
@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    /**
     * Lista todas as notificações de um usuário, ordenadas por data de criação.
     */
    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(Integer destinatarioId);

    /**
     * Lista notificações não lidas de um usuário, ordenadas por data de criação.
     */
    List<Notificacao> findByDestinatarioIdAndLidaFalseOrderByDataCriacaoDesc(Integer destinatarioId);

    /**
     * Conta notificações não lidas de um usuário.
     */
    @Query("SELECT COUNT(n) FROM Notificacao n WHERE n.destinatario.id = :destinatarioId AND n.lida = false")
    Long countNaoLidasByDestinatarioId(@Param("destinatarioId") Integer destinatarioId);

    /**
     * Marca todas as notificações de um usuário como lidas.
     */
    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true, n.dataLeitura = CURRENT_TIMESTAMP WHERE n.destinatario.id = :destinatarioId AND n.lida = false")
    void marcarTodasComoLidas(@Param("destinatarioId") Integer destinatarioId);

    /**
     * Lista as últimas N notificações de um usuário.
     */
    @Query("SELECT n FROM Notificacao n WHERE n.destinatario.id = :destinatarioId ORDER BY n.dataCriacao DESC LIMIT :limit")
    List<Notificacao> findTopByDestinatarioId(@Param("destinatarioId") Integer destinatarioId, @Param("limit") int limit);

    /**
     * Busca notificação por ID se pertencer ao destinatário informado (proteção IDOR).
     */
    Optional<Notificacao> findByIdAndDestinatarioId(Integer notificacaoId, Integer destinatarioId);
}
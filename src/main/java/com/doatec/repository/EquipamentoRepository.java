package com.doatec.repository;

import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para a entidade Equipamento.
 */
@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Integer> {

    List<Equipamento> findByStatus(StatusEquipamento status);

    List<Equipamento> findByStatusAndTipoContainingIgnoreCase(StatusEquipamento status, String tipo);

    List<Equipamento> findByTipoContainingIgnoreCase(String tipo);

    @Query("SELECT e FROM Equipamento e WHERE e.status = :status")
    List<Equipamento> findByStatusEnum(@Param("status") StatusEquipamento status);

    /**
     * Busca equipamentos disponíveis que correspondam à preferência do aluno.
     * A busca é case-insensitive e parcial.
     */
    @Query("SELECT e FROM Equipamento e WHERE e.status = 'DISPONIVEL' AND LOWER(e.tipo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Equipamento> findDisponiveisByKeyword(@Param("keyword") String keyword);

    /**
     * Busca equipamentos reservados para uma solicitação específica.
     */
    List<Equipamento> findBySolicitacaoDestinoId(Integer solicitacaoId);

    /**
     * Busca equipamentos atribuídos a um aluno específico.
     */
    List<Equipamento> findByAlunoDestinatarioId(Integer alunoId);

    /**
     * Busca com filtros combinados (todos opcionais). Cada filtro só
     * restringe se for não-null. Filtro {@code hasDoacao}: true = só com
     * vínculo; false = só sem vínculo; null = ignorar. {@code doacaoId}
     * tem precedência sobre {@code hasDoacao}. {@code q} faz LIKE
     * case-insensitive em tipo OU descrição.
     */
    @Query("""
        SELECT e FROM Equipamento e
        WHERE (:status IS NULL OR e.status = :status)
          AND (:conservacao IS NULL OR e.estadoConservacao = :conservacao)
          AND (:doacaoId IS NULL OR e.doacao.id = :doacaoId)
          AND (
                :hasDoacao IS NULL
                OR (:hasDoacao = TRUE AND e.doacao IS NOT NULL)
                OR (:hasDoacao = FALSE AND e.doacao IS NULL)
              )
          AND (
                CAST(:q AS string) IS NULL
                OR LOWER(e.tipo) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
                OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
              )
        ORDER BY e.dataEntradaInventario DESC
    """)
    List<Equipamento> findWithFilters(
            @Param("status") StatusEquipamento status,
            @Param("conservacao") EstadoConservacao conservacao,
            @Param("hasDoacao") Boolean hasDoacao,
            @Param("doacaoId") Integer doacaoId,
            @Param("q") String q
    );
}
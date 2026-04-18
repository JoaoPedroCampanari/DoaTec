package com.doatec.repository;

import com.doatec.model.inventory.Equipamento;
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
}
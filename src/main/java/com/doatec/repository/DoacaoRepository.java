package com.doatec.repository;

import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doacao d WHERE d.id = :id")
    Optional<Doacao> findByIdForUpdate(@Param("id") Integer id);
    List<Doacao> findByDoadorId(Integer doadorId);
    List<Doacao> findByDoadorIdOrderByDataDoacaoDescIdDesc(Integer doadorId);
    List<Doacao> findByStatus(StatusDoacao status);
    List<Doacao> findByDoadorIdAndStatus(Integer doadorId, StatusDoacao status);
    Page<Doacao> findByStatus(StatusDoacao status, Pageable pageable);
    Optional<Doacao> findTopByOrderByDataDoacaoDesc();
    long countByStatus(StatusDoacao status);
    @Query(value = "SELECT * FROM doacao WHERE deleted_at IS NULL AND status IN ('AGUARDANDO_COLETA', 'RECEBIDO', 'FINALIZADO', 'APROVADO', 'AGUARDANDO_ENTREGA', 'ENTREGUE', 'AGENDANDO_COLETA', 'COLETA_AGENDADA', 'RETIRADO')", countQuery = "SELECT count(*) FROM doacao WHERE deleted_at IS NULL AND status IN ('AGUARDANDO_COLETA', 'RECEBIDO', 'FINALIZADO', 'APROVADO', 'AGUARDANDO_ENTREGA', 'ENTREGUE', 'AGENDANDO_COLETA', 'COLETA_AGENDADA', 'RETIRADO')", nativeQuery = true)
    Page<Doacao> findAprovadasHistoricas(Pageable pageable);
    @Query(value = "SELECT * FROM doacao WHERE deleted_at IS NULL AND status IN ('AGUARDANDO_COLETA', 'RECEBIDO', 'FINALIZADO', 'APROVADO', 'AGUARDANDO_ENTREGA', 'ENTREGUE', 'AGENDANDO_COLETA', 'COLETA_AGENDADA', 'RETIRADO')", nativeQuery = true)
    List<Doacao> findAprovadasHistoricas();
    @Query(value = "SELECT * FROM doacao WHERE deleted_at IS NULL AND doador_id = :doadorId AND status IN ('AGUARDANDO_COLETA', 'RECEBIDO', 'FINALIZADO', 'APROVADO', 'AGUARDANDO_ENTREGA', 'ENTREGUE', 'AGENDANDO_COLETA', 'COLETA_AGENDADA', 'RETIRADO')", nativeQuery = true)
    List<Doacao> findAprovadasHistoricasDoDoador(@Param("doadorId") Integer doadorId);
    @Query(value = "SELECT count(*) FROM doacao WHERE deleted_at IS NULL AND status IN ('AGUARDANDO_COLETA', 'RECEBIDO', 'FINALIZADO', 'APROVADO', 'AGUARDANDO_ENTREGA', 'ENTREGUE', 'AGENDANDO_COLETA', 'COLETA_AGENDADA', 'RETIRADO')", nativeQuery = true)
    long countAprovadasHistoricas();
    long countByDoadorId(Integer doadorId);
}
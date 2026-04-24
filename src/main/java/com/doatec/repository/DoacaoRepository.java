package com.doatec.repository;

import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Integer> {
    List<Doacao> findByDoadorId(Integer doadorId);
    List<Doacao> findByStatus(StatusDoacao status);
    List<Doacao> findByDoadorIdAndStatus(Integer doadorId, StatusDoacao status);
    Page<Doacao> findByStatus(StatusDoacao status, Pageable pageable);
    Optional<Doacao> findTopByOrderByDataDoacaoDesc();
    long countByStatus(StatusDoacao status);
    long countByDoadorId(Integer doadorId);
}
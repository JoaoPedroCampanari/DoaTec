package com.doatec.repository;

import com.doatec.model.donation.Doacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Integer> {
    List<Doacao> findByDoadorId(Integer doadorId);

    Optional<Doacao> findTopByOrderByDataDoacaoDesc();
}
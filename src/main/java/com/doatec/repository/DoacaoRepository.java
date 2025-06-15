package com.doatec.repository;

import com.doatec.model.donation.Doacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, UUID> {

}

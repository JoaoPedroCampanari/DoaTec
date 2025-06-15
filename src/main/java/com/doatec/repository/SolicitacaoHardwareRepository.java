package com.doatec.repository;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SolicitacaoHardwareRepository extends JpaRepository<SolicitacaoHardware, UUID> {
}

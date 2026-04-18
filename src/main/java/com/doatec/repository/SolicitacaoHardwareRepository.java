package com.doatec.repository;

import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoHardwareRepository extends JpaRepository<SolicitacaoHardware, Integer> {
    List<SolicitacaoHardware> findByAlunoId(Integer alunoId);
    List<SolicitacaoHardware> findByStatus(StatusSolicitacao status);
}
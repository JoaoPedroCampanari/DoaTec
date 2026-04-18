package com.doatec.repository;

import com.doatec.model.account.LogAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAcaoRepository extends JpaRepository<LogAcao, Integer> {

    List<LogAcao> findByAdminId(Integer adminId);

    List<LogAcao> findByEntidadeOrderByDataAcaoDesc(String entidade);

    List<LogAcao> findByEntidadeIdOrderByDataAcaoDesc(Integer entidadeId);
}
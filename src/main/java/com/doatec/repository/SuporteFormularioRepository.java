package com.doatec.repository;

import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuporteFormularioRepository extends JpaRepository<SuporteFormulario, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM SuporteFormulario t WHERE t.id = :id")
    java.util.Optional<SuporteFormulario> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);

    List<SuporteFormulario> findByAutorId(Integer autorId);
    List<SuporteFormulario> findByAutorIdOrderByDataCriacaoDescIdDesc(Integer autorId);
    List<SuporteFormulario> findByStatus(StatusSuporte status);
    Page<SuporteFormulario> findByStatus(StatusSuporte status, Pageable pageable);
    long countByStatus(StatusSuporte status);
    long countByAutorId(Integer autorId);
}